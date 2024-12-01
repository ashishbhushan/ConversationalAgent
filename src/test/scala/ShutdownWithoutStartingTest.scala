import ServerDIR.LLMServer
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ShutdownWithoutStartingTest extends AnyFlatSpec with Matchers {
  "shutdown" should "handle being called when the server is not running" in {
    val server = new LLMServer()
    noException should be thrownBy server.shutdown()
  }
}