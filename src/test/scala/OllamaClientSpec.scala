import ClientDIR.OllamaClient
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Seconds, Span}
import scala.concurrent.{ExecutionContext, Future}

class OllamaClientSpec extends AsyncFlatSpec with Matchers with ScalaFutures {
  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(30, Seconds))

  class TestOllamaClient extends OllamaClient() {
    override protected def makeRequest(input: String, attempt: Int = 0, maxRetries: Int = 3): Future[String] = {
      Future.successful(
        if (input.isEmpty) "Please provide more context."
        else "This is a test response."
      )
    }
  }

  "OllamaClient" should "handle valid input" in {
    val client = new TestOllamaClient()

    client.generate("Tell me about AI").map { response =>
      assert(response.nonEmpty)
      assert(response == "This is a test response.")
      succeed
    }
  }

  it should "handle empty input" in {
    val client = new TestOllamaClient()

    client.generate("").map { response =>
      assert(response == "Please provide more context.")
      succeed
    }
  }
}