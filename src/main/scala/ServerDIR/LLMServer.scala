package ServerDIR

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import ClientDIR.OllamaClient
import io.grpc.{Server, ServerBuilder}
import io.grpc.netty.NettyServerBuilder
import ServerDIR.grpc.llm_service.LLMServiceGrpc

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration._

object LLMServer {
  private val logger = LoggerFactory.getLogger(getClass)
  private val config = ConfigFactory.load()

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "llm-server")
    implicit val executionContext: ExecutionContext = system.executionContext

    val host = config.getString("server.host")
    val grpcPort = config.getInt("grpc.port")
    val maxMessageSize = config.getInt("grpc.max-message-size")
    val metadataSize = config.getInt("grpc.metadata-size")

    val ollamaClient = new OllamaClient()
    val lambdaClient = new LambdaGrpcClient(config)
    val grpcService = new LLMServiceImpl(ollamaClient, lambdaClient)

    val grpcServer: Server = NettyServerBuilder
      .forPort(grpcPort)
      .addService(LLMServiceGrpc.bindService(grpcService, executionContext))
      .maxInboundMessageSize(maxMessageSize)
      .maxInboundMetadataSize(metadataSize)
      .build()
      .start()

    logger.info(s"gRPC Server started on port $grpcPort")

    sys.addShutdownHook {
      logger.info("Shutting down server...")
      try {
        grpcServer.shutdown()
        val terminated = grpcServer.awaitTermination(10, TimeUnit.SECONDS)
        if (!terminated) {
          logger.warn("Server didn't terminate in time, forcing shutdown")
          grpcServer.shutdownNow()
        }
      } catch {
        case ex: Exception =>
          logger.error("Error during server shutdown", ex)
      }

      try {
        system.terminate()
        Await.result(system.whenTerminated, 10.seconds)
      } catch {
        case ex: Exception =>
          logger.error("Error during actor system termination", ex)
      }
      logger.info("Shutdown complete")
    }

    grpcServer.awaitTermination()
  }
}