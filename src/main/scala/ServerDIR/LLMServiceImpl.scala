package ServerDIR

import ServerDIR.grpc.llm_service.{ConversationRequest, ConversationResponse, LLMServiceGrpc}
import org.slf4j.LoggerFactory

import scala.concurrent.Future

class LLMServiceImpl(lambdaGrpcClient: LambdaGrpcClient)
  extends LLMServiceGrpc.LLMService {

  private val logger = LoggerFactory.getLogger(getClass)

  override def streamConversation(request: ConversationRequest): Future[ConversationResponse] = {
    logger.info(s"Processing gRPC conversation request: ${request.prompt}")
    lambdaGrpcClient.invoke(request)
  }
}