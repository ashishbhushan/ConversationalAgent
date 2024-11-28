package ServerDIR

import scala.concurrent.{ExecutionContext, Future}
import ServerDIR.grpc.llm_service.LLMServiceGrpc
import ServerDIR.grpc.llm_service.{ConversationRequest, ConversationResponse}
import ClientDIR.OllamaClient
import org.slf4j.LoggerFactory

class LLMServiceImpl(ollamaClient: OllamaClient, lambdaGrpcClient: LambdaGrpcClient)
                    (implicit ec: ExecutionContext)
  extends LLMServiceGrpc.LLMService {

  private val logger = LoggerFactory.getLogger(getClass)

  override def streamConversation(request: ConversationRequest): Future[ConversationResponse] = {
    logger.info(s"Processing gRPC conversation request: ${request.prompt}")
    lambdaGrpcClient.invoke(request)
  }
}