package ServerDIR

import com.typesafe.config.Config
import io.grpc.ManagedChannel
import io.grpc.netty.NettyChannelBuilder
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.InvokeRequest
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import scala.concurrent.{ExecutionContext, Future}
import spray.json._
import DefaultJsonProtocol._
import java.util.Base64
import ServerDIR.grpc.llm_service.{ConversationRequest, ConversationResponse, CompletionResult}
import org.slf4j.LoggerFactory

class LambdaGrpcClient(config: Config)(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)

  // Updated case classes with proper structure
  case class BedrockRequest(
                             inputText: String,
                             modelId: String,
                             maxTokenCount: Int,
                             temperature: Float,
                             topP: Float,
                             contentType: String = "application/json"  // Added default contentType
                           )

  case class BedrockResult(tokenCount: Int, outputText: String, completionReason: String)
  case class BedrockResponse(inputTextTokenCount: Int, results: Seq[BedrockResult])
  case class LambdaResponse(
                             statusCode: Int,
                             body: String,
                             headers: Map[String, String] = Map(
                               "Content-Type" -> "application/json",
                               "Access-Control-Allow-Origin" -> "*"
                             )
                           )

  // JSON formats
  implicit val bedrockRequestFormat: RootJsonFormat[BedrockRequest] = jsonFormat6(BedrockRequest)
  implicit val bedrockResultFormat: RootJsonFormat[BedrockResult] = jsonFormat3(BedrockResult)
  implicit val bedrockResponseFormat: RootJsonFormat[BedrockResponse] = jsonFormat2(BedrockResponse)
  implicit val lambdaResponseFormat: RootJsonFormat[LambdaResponse] = jsonFormat3(LambdaResponse)

  def invoke(request: ConversationRequest): Future[ConversationResponse] = {
    Future {
      val bedrockRequest = BedrockRequest(
        inputText = request.prompt,
        modelId = request.modelId,
        maxTokenCount = request.maxTokenCount,
        temperature = request.temperature,
        topP = request.topP
      )

      logger.info(s"Invoking Lambda function: $functionName with request: ${bedrockRequest.toJson}")

      val invokeRequest = InvokeRequest.builder()
        .functionName(functionName)
        .payload(SdkBytes.fromUtf8String(bedrockRequest.toJson.compactPrint))
        .build()

      val result = lambdaClient.invoke(invokeRequest)
      val response = new String(result.payload().asByteArray(), "UTF-8")

      logger.debug(s"Received Lambda response: $response")

      try {
        val lambdaResponse = response.parseJson.convertTo[LambdaResponse]
        if (lambdaResponse.statusCode == 200) {
          val bedrockResponse = lambdaResponse.body.parseJson.convertTo[BedrockResponse]

          ConversationResponse(
            inputTextTokenCount = bedrockResponse.inputTextTokenCount,
            results = bedrockResponse.results.map { result =>
              CompletionResult(
                tokenCount = result.tokenCount,
                outputText = result.outputText,
                completionReason = result.completionReason
              )
            }.toSeq
          )
        } else {
          throw new Exception(s"Lambda returned status code ${lambdaResponse.statusCode}")
        }
      } catch {
        case e: Exception =>
          logger.error(s"Error processing Lambda response: ${e.getMessage}", e)
          ConversationResponse(
            inputTextTokenCount = 0,
            results = Seq(CompletionResult(
              tokenCount = 0,
              outputText = s"Error processing request: ${e.getMessage}",
              completionReason = "ERROR"
            ))
          )
      }
    }
  }

  private val lambdaClient = LambdaClient.builder()
    .region(Region.of(config.getString("aws.region")))
    .build()

  private val functionName = config.getString("aws.lambda-function")
}