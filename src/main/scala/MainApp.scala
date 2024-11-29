import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model._
import akka.util.Timeout
import spray.json._
import DefaultJsonProtocol._
import ClientDIR.{ConversationApp, ConversationTurn}
import ServerDIR.LLMServer
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object MainApp extends App {
  private val logger = LoggerFactory.getLogger(getClass)

  // Get server host from environment variable or system property, fallback to config
  private val serverHost = sys.env.getOrElse("SERVER_HOST",
    sys.props.getOrElse("SERVER_HOST",
      ConfigFactory.load().getString("server.host")
    )
  )

  private val config = ConfigFactory.load()

  logger.info(s"Starting server on host: $serverHost")

  // Message types for idle management
  private sealed trait IdleMessage
  private case object InitialTimeout extends IdleMessage
  private case object RequestReceived extends IdleMessage
  private case object CheckIdleTimeout extends IdleMessage

  // JSON formats for HTTP responses
  case class ConversationRequest(prompt: String)
  case class ConversationResponse(turns: Seq[ConversationTurn])

  implicit val conversationTurnFormat: RootJsonFormat[ConversationTurn] = jsonFormat5(ConversationTurn)
  implicit val conversationRequestFormat: RootJsonFormat[ConversationRequest] = jsonFormat1(ConversationRequest)
  implicit val conversationResponseFormat: RootJsonFormat[ConversationResponse] = jsonFormat1(ConversationResponse)

  private val initialTimeoutDuration = 1.minute
  private val idleTimeoutDuration = 5.minutes

  private val idleTimeoutBehavior = Behaviors.setup[IdleMessage] { context =>
    var lastRequestTime = System.currentTimeMillis()
    var initialTimeoutScheduled = true
    var checkTimeoutCancellable = Option.empty[akka.actor.Cancellable]

    // Schedule initial timeout check
    context.scheduleOnce(initialTimeoutDuration, context.self, InitialTimeout)

    // Schedule periodic idle checks
    def scheduleNextCheck(): Unit = {
      checkTimeoutCancellable = Some(
        context.system.scheduler.scheduleOnce(
          1.minute,
          () => context.self ! CheckIdleTimeout
        )(context.system.executionContext)
      )
    }

    scheduleNextCheck()

    Behaviors.receiveMessage {
      case InitialTimeout =>
        if (initialTimeoutScheduled) {
          logger.info("No initial request received within 1 minute, shutting down server...")
          shutdown()
        }
        Behaviors.same

      case RequestReceived =>
        initialTimeoutScheduled = false
        lastRequestTime = System.currentTimeMillis()
        logger.debug("Request received, resetting idle timeout")
        Behaviors.same

      case CheckIdleTimeout =>
        val timeSinceLastRequest = System.currentTimeMillis() - lastRequestTime
        if (!initialTimeoutScheduled && timeSinceLastRequest > idleTimeoutDuration.toMillis) {
          logger.info(s"No requests received for ${idleTimeoutDuration.toMinutes} minutes, shutting down server...")
          shutdown()
        } else {
          scheduleNextCheck()
        }
        Behaviors.same
    }
  }

  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "main-system")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  // Create idle timeout actor
  private val idleTimeoutActor = system.systemActorOf(idleTimeoutBehavior, "idle-timeout-actor")

  // Start LLM Server
  private val llmServer = new LLMServer()
  llmServer.startServer()

  // Initialize ConversationApp
  private val conversationApp = new ConversationApp()(executionContext)

  private val requestTimeout = Duration.fromNanos(config.getDuration("akka.http.server.request-timeout").toNanos)
  implicit val routeTimeout: Timeout = Timeout(requestTimeout)

  private def shutdown(): Unit = {
    logger.info("Initiating server shutdown due to inactivity...")
    llmServer.shutdown()
    system.terminate()
    logger.info("Server shutdown complete")
  }

  private val route: Route = {
    pathPrefix("chat") {
      post {
        withRequestTimeout(requestTimeout) {
          entity(as[String]) { body =>
            extractUri { uri =>
              logger.info(s"Received request at ${uri.toString()}")
              idleTimeoutActor ! RequestReceived

              complete {
                try {
                  val request = body.parseJson.convertTo[ConversationRequest]
                  logger.info(s"Starting new conversation with prompt: ${request.prompt}")

                  conversationApp.startNewConversation(request.prompt).map { turns =>
                    turns.foreach { turn =>
                      logger.info(s"Cycle ${turn.cycleNumber} completed")
                      logger.debug(s"Bedrock Input: ${turn.bedrockInput}")
                      logger.debug(s"Bedrock Response: ${turn.bedrockResponse}")
                      logger.debug(s"Ollama Input: ${turn.ollamaInput}")
                      logger.debug(s"Ollama Response: ${turn.ollamaResponse}")
                    }

                    HttpResponse(
                      status = StatusCodes.OK,
                      entity = HttpEntity(
                        ContentTypes.`application/json`,
                        ConversationResponse(turns).toJson.prettyPrint
                      )
                    )
                  }.recover {
                    case e: Exception =>
                      logger.error("Error in conversation", e)
                      HttpResponse(
                        status = StatusCodes.InternalServerError,
                        entity = s"Conversation failed: ${e.getMessage}"
                      )
                  }
                } catch {
                  case e: Exception =>
                    logger.error("Error processing request", e)
                    HttpResponse(
                      status = StatusCodes.BadRequest,
                      entity = s"Invalid request: ${e.getMessage}"
                    )
                }
              }
            }
          }
        }
      }
    }
  }

  private val httpBinding = Http()
    .newServerAt(
      serverHost,  // Using the resolved server host
      config.getInt("server.port")
    )
    .bind(route)

  httpBinding.onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      logger.info("LLM Conversation Server Started")
      logger.info(s"Server URL: https://${address.getHostString}:${address.getPort}/chat")
      logger.info("Usage Example:")
      logger.info("""curl -X POST http://localhost:8081/chat -H "Content-Type: application/json" -d '{"prompt": "your question here"}'""")

    case Failure(ex) =>
      logger.error("Failed to start server", ex)
      logger.error("Server startup failed. Check logs for details.")
      system.terminate()
  }

  sys.addShutdownHook {
    logger.info("Initiating server shutdown...")
    llmServer.shutdown()
    system.terminate()
    logger.info("Server shutdown complete")
  }
}