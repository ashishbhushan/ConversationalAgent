import ServerDIR.LLMServer
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class LLMServerSpec extends AsyncFlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll {
  implicit val ec: ExecutionContext = ExecutionContext.global

  private var server: LLMServer = _

  override def beforeAll(): Unit = {
    server = new LLMServer()
  }

  override def afterAll(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  "LLMServer" should "start and stop correctly" in {
    Future {
      val startedServer = server.startServer()
      startedServer should not be null

      // Verify server is running
      startedServer.isShutdown should be(false)

      // Test shutdown
      server.shutdown()
      startedServer.isShutdown should be(true)

      succeed
    }
  }

  it should "handle multiple start-stop cycles" in {
    Future {
      for (_ <- 1 to 3) {
        val startedServer = server.startServer()
        startedServer should not be null
        startedServer.isShutdown should be(false)

        server.shutdown()
        startedServer.isShutdown should be(true)
      }
      succeed
    }
  }
}