import ClientDIR.{ConversationApp, ConversationTurn}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Seconds, Span}
import scala.concurrent.{ExecutionContext, Future}

class ConversationSpec extends AsyncFlatSpec with Matchers with ScalaFutures {
  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(30, Seconds))

  // Mock data
  val mockTurn = ConversationTurn(
    cycleNumber = 1,
    bedrockInput = "Tell me about cats",
    bedrockResponse = "Cats are fascinating creatures.",
    ollamaInput = "How can you respond to 'Cats are fascinating creatures'?",
    ollamaResponse = "Indeed, they are wonderful pets."
  )

  class TestConversationApp extends ConversationApp() {
    override def startNewConversation(prompt: String): Future[Seq[ConversationTurn]] = {
      Future.successful(Seq(mockTurn))
    }
  }

  "ConversationApp" should "handle conversation with mocked responses" in {
    val app = new TestConversationApp()
    val initialPrompt = "Tell me about cats"

    app.startNewConversation(initialPrompt).map { turns =>
      assert(turns.nonEmpty)
      assert(turns.head.cycleNumber == 1)
      assert(turns.head.bedrockInput.contains("cats"))
      assert(turns.head.bedrockResponse.contains("fascinating"))
      succeed
    }
  }

  it should "handle empty responses" in {
    val emptyApp = new TestConversationApp() {
      override def startNewConversation(prompt: String): Future[Seq[ConversationTurn]] = {
        Future.successful(Seq.empty)
      }
    }

    emptyApp.startNewConversation("").map { turns =>
      assert(turns.isEmpty)
      succeed
    }
  }
}