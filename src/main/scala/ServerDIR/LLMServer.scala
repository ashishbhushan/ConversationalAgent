package ServerDIR

import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory
import io.grpc.Server
import io.grpc.netty.NettyServerBuilder
import ServerDIR.grpc.llm_service.LLMServiceGrpc
import scala.concurrent.ExecutionContext
import ClientDIR.OllamaClient
import java.util.concurrent.TimeUnit

class LLMServer {
  private val logger = LoggerFactory.getLogger(getClass)
  private val config = ConfigFactory.load()
  private var grpcServer: Option[Server] = None

  def startServer()(implicit executionContext: ExecutionContext): Server = {
    val grpcPort = config.getInt("grpc.port")
    val maxMessageSize = config.getInt("grpc.max-message-size")
    val metadataSize = config.getInt("grpc.metadata-size")

    val ollamaClient = new OllamaClient()
    val lambdaClient = new LambdaGrpcClient(config)
    val grpcService = new LLMServiceImpl(lambdaClient)

    val server = NettyServerBuilder
      .forPort(grpcPort)
      .addService(LLMServiceGrpc.bindService(grpcService, executionContext))
      .maxInboundMessageSize(maxMessageSize)
      .maxInboundMetadataSize(metadataSize)
      .build()
      .start()

    grpcServer = Some(server)
    logger.info(s"gRPC Server started on port $grpcPort")
    server
  }

  def shutdown(): Unit = {
    logger.info("Shutting down gRPC server...")
    grpcServer.foreach { server =>
      try {
        server.shutdown()
        val terminated = server.awaitTermination(10, TimeUnit.SECONDS)
        if (!terminated) {
          logger.warn("Server didn't terminate in time, forcing shutdown")
          server.shutdownNow()
        }
      } catch {
        case ex: Exception =>
          logger.error("Error during server shutdown", ex)
      }
    }
    logger.info("gRPC Server shutdown complete")
  }
}