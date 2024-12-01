import ServerDIR.LambdaGrpcClient
import ServerDIR.grpc.llm_service.{ConversationRequest, ConversationResponse}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

class LambdaGrpcClientSpec extends AsyncFlatSpec with Matchers with ScalaFutures {
  implicit val ec: ExecutionContext = ExecutionContext.global
  val config: Config = ConfigFactory.load()

  "LambdaGrpcClient" should "handle conversation requests and return valid responses" in {
    val client = new LambdaGrpcClient(ConfigFactory.load())
    val request = ConversationRequest(
      prompt = "what is your name",
      modelId = config.getString("llm.model-id"),
      maxTokenCount = config.getInt("llm.max-token-count"),
      temperature = config.getString("llm.temperature").toFloat,
      topP = config.getString("llm.top-p").toFloat
    )

    val futureResult = client.invoke(request)

    futureResult.map { response =>
      response.isInstanceOf[ConversationResponse] should be(true)
      response.results should not be empty
      response.results.head.outputText should not be empty
      response.results.head.completionReason should not be empty
    }
  }

  it should "handle error cases gracefully" in {
    val client = new LambdaGrpcClient(ConfigFactory.load())
    val request = ConversationRequest(
      prompt = "",
      modelId = config.getString("llm.model-id"),
      maxTokenCount = -1,
      temperature = -1f,
      topP = -1f
    )

    val futureResult = client.invoke(request)

    futureResult.map { response =>
      response.results should not be empty
      response.results.head.outputText should include("Error")
      response.results.head.completionReason should be("ERROR")
    }
  }
}