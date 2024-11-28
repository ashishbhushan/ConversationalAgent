package ClientDIR

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory
import scala.concurrent.Await
import scala.concurrent.duration._
import java.io.{File, PrintWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ConversationApp extends App {
  private val logger = LoggerFactory.getLogger(getClass)
  private val config = ConfigFactory.load()

  if (args.length == 0) {
    println("❌ Error: Please provide an initial prompt as a command line argument")
    System.exit(1)
  }

  implicit val system = ActorSystem(Behaviors.empty, "conversation-system")
  implicit val ec = system.executionContext

  private val conversationsDir = new File("conversations")
  if (!conversationsDir.exists()) {
    conversationsDir.mkdir()
  }

  private val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
  private val filename = s"conversations/conversation_$timestamp.txt"
  private val writer = new PrintWriter(new File(filename))

  private def writeToFile(content: String): Unit = {
    writer.println(content)
    writer.flush()
  }

  println("🤖 Initializing Conversational AI System")
  val client = new ConversationalClient(
    config.getString("server.host"),
    config.getInt("grpc.port")
  )

  val initialPrompt = args.mkString(" ")

  def cleanup(): Unit = {
    writer.close()
    client.shutdown()
    system.terminate()
  }

  sys.addShutdownHook {
    println("\n👋 Shutting down gracefully...")
    cleanup()
  }

  try {
    println(s"\n👤 User Prompt: $initialPrompt")
    println("\n🔄 Starting conversation cycle...")

    writeToFile("=== AI Conversation Log ===")
    writeToFile(s"Timestamp: ${LocalDateTime.now}")
    writeToFile(s"Initial Query: $initialPrompt\n")

    val conversationFuture = client.startConversation(initialPrompt)

    Await.result(conversationFuture, 5.minutes).foreach { turn =>
      // Console output
      println(s"\n=== 🔄 Cycle ${turn.cycleNumber} ===")
      println(s"💭 Input to Bedrock: ${turn.bedrockInput}")
      println(s"🤖 Bedrock: ${turn.bedrockResponse}")
      println(s"💭 Input to Ollama: ${turn.ollamaInput}")
      println(s"🤖 Ollama: ${turn.ollamaResponse}")

      // File output
      writeToFile(s"=== Cycle ${turn.cycleNumber} ===")
      writeToFile(s"Input to Bedrock: ${turn.bedrockInput}")
      writeToFile(s"Bedrock Response: ${turn.bedrockResponse}")
      writeToFile(s"Input to Ollama: ${turn.ollamaInput}")
      writeToFile(s"Ollama Response: ${turn.ollamaResponse}")
      writeToFile("")
    }

    println(s"\n✅ Conversation completed successfully")
    println(s"📝 Conversation saved to: $filename")

  } catch {
    case ex: Exception =>
      val errorMsg = s"Error in conversation: ${ex.getMessage}"
      logger.error(errorMsg, ex)
      println(s"\n❌ $errorMsg")
      writeToFile(s"\n❌ $errorMsg")
  } finally {
    writeToFile("\n=== Conversation End ===")
    cleanup()
  }
}