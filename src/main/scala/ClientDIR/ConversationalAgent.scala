package ClientDIR

import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.utils.Options
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import scala.util.{Success, Failure}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import java.io.{File, PrintWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ConversationalAgent extends App {
  private val logger = LoggerFactory.getLogger(getClass)
  private val config = ConfigFactory.load()

  // Template types for different conversation styles
  sealed trait ConversationTemplate {
    def generateNextQuery(response: String): String
  }

  case object InquisitiveTemplate extends ConversationTemplate {
    def generateNextQuery(response: String): String =
      s"What are your thoughts on this perspective: '$response'? Can you elaborate on specific aspects?"
  }

  case object AnalyticalTemplate extends ConversationTemplate {
    def generateNextQuery(response: String): String =
      s"Let's analyze this statement: '$response'. What are the key insights and implications?"
  }

  case object EmpatheticTemplate extends ConversationTemplate {
    def generateNextQuery(response: String): String =
      s"From an emotional perspective, how do you interpret this response: '$response'?"
  }

  class ConversationExperiment(template: ConversationTemplate, experimentName: String) {
    private val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
    private val outputFile = new File(s"experiments/${experimentName}_${timestamp}.txt")
    private val writer = new PrintWriter(outputFile)

    private def log(message: String): Unit = {
      writer.println(message)
      writer.flush()
      println(message)
    }

    def runExperiment(initialQuery: String, maxTurns: Int = 5): Unit = {
      implicit val system = ActorSystem(Behaviors.empty, "conversation-experiment")
      implicit val ec = system.executionContext

      val client = new ConversationalClient(
        config.getString("server.host"),
        config.getInt("grpc.port")
      )

      log(s"=== Experiment: $experimentName ===")
      log(s"Template: ${template.getClass.getSimpleName}")
      log(s"Initial Query: $initialQuery\n")

      try {
        var currentQuery = initialQuery
        for (turn <- 1 to maxTurns if currentQuery.nonEmpty) {
          log(s"\n=== Turn $turn ===")
          log(s"Current Query: $currentQuery")

          // Get response from cloud service
          val cloudResponse = Await.result(
            client.startConversation(currentQuery).map(_.headOption.map(_.bedrockResponse).getOrElse("")),
            30.seconds
          )
          log(s"Cloud Response: $cloudResponse")

          // Generate next query using template and Ollama
          val ollamaAPI = new OllamaAPI(config.getString("ollama.host"))
          ollamaAPI.setRequestTimeoutSeconds(config.getInt("ollama.request-timeout-seconds"))

          val templateQuery = template.generateNextQuery(cloudResponse)
          log(s"Template Generated Query: $templateQuery")

          val ollamaResponse = ollamaAPI.generate(
            config.getString("ollama.model"),
            templateQuery,
            false,
            new Options(new java.util.HashMap[String, Object]())
          )

          val nextQuery = Option(ollamaResponse)
            .map(_.getResponse)
            .map(_.trim)
            .getOrElse("")

          log(s"Ollama Generated Next Query: $nextQuery")
          currentQuery = nextQuery
        }
      } catch {
        case e: Exception =>
          log(s"\nError during experiment: ${e.getMessage}")
          e.printStackTrace(writer)
      } finally {
        writer.close()
        client.shutdown()
        system.terminate()
      }
    }
  }

  if (args.isEmpty) {
    println("Please provide an initial query as a command line argument")
    System.exit(1)
  }

  // Create experiments directory if it doesn't exist
  new File("experiments").mkdirs()

  // Run experiments with different templates
  val initialQuery = args.mkString(" ")
  val templates = List(
    (InquisitiveTemplate, "inquisitive"),
    (AnalyticalTemplate, "analytical"),
    (EmpatheticTemplate, "empathetic")
  )

  templates.foreach { case (template, name) =>
    println(s"\nStarting experiment with $name template...")
    val experiment = new ConversationExperiment(template, name)
    experiment.runExperiment(initialQuery)
  }
}