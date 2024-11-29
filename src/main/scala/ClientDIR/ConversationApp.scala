package ClientDIR

import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory
import java.io.{File, PrintWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, Future}

class ConversationApp(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val config = ConfigFactory.load()

  private val conversationsDir = new File("conversations")
  if (!conversationsDir.exists()) {
    conversationsDir.mkdir()
  }

  // Move file creation inside the conversation handling
  private def createNewConversation(): (PrintWriter, String) = {
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
    val filename = s"conversations/conversation_$timestamp.txt"
    (new PrintWriter(new File(filename)), filename)
  }

  def startNewConversation(initialPrompt: String): Future[Seq[ConversationTurn]] = {
    val (currentWriter, filename) = createNewConversation()
    logger.info(s"Starting new conversation with prompt: $initialPrompt")
    logger.info(s"Recording conversation to: $filename")

    def writeToFile(content: String): Unit = {
      currentWriter.println(content)
      currentWriter.flush()
    }

    val client = new ConversationalClient(
      config.getString("server.host"),
      config.getInt("grpc.port")
    )

    println(s"\nStarting new conversation with prompt: $initialPrompt")
    println(s"Recording to: $filename")

    writeToFile("=== AI Conversation Log ===")
    writeToFile(s"Timestamp: ${LocalDateTime.now}")
    writeToFile(s"Initial Query: $initialPrompt\n")

    client.startConversation(initialPrompt)
      .map { turns =>
        turns.foreach { turn =>
          logger.info(s"Cycle ${turn.cycleNumber} completed")
          logger.debug(s"Bedrock input: ${turn.bedrockInput}")
          logger.debug(s"Bedrock response: ${turn.bedrockResponse}")
          logger.debug(s"Ollama input: ${turn.ollamaInput}")
          logger.debug(s"Ollama response: ${turn.ollamaResponse}")

          // File output remains same as it's for record keeping
          writeToFile(s"=== Cycle ${turn.cycleNumber} ===")
          writeToFile(s"Input to Bedrock: ${turn.bedrockInput}")
          writeToFile(s"Bedrock Response: ${turn.bedrockResponse}")
          writeToFile(s"Input to Ollama: ${turn.ollamaInput}")
          writeToFile(s"Ollama Response: ${turn.ollamaResponse}")
          writeToFile("")
        }

        logger.info(s"Conversation completed and saved to: $filename")
        currentWriter.close()
        client.shutdown()
        turns
      }
      .recoverWith { case e: Exception =>
        val errorMsg = s"Error in conversation: ${e.getMessage}"
        logger.error(errorMsg, e)
        writeToFile(s"\n❌ $errorMsg")
        writeToFile("\n=== Conversation End with Error ===")
        currentWriter.close()
        client.shutdown()
        Future.failed(e)
      }
  }
}