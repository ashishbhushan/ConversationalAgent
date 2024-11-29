package ClientDIR

import scala.concurrent.{ExecutionContext, Future}
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer
import ServerDIR.grpc.llm_service.ConversationRequest
import io.grpc.ManagedChannel
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import ServerDIR.grpc.llm_service.LLMServiceGrpc
import com.typesafe.config.ConfigFactory

case class ConversationTurn(
                             cycleNumber: Int,
                             bedrockInput: String,
                             bedrockResponse: String,
                             ollamaInput: String,
                             ollamaResponse: String
                           )

class ConversationalClient(grpcHost: String, grpcPort: Int)(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val config = ConfigFactory.load()

  private val channel: ManagedChannel = NettyChannelBuilder
    .forAddress(grpcHost, grpcPort)
    .usePlaintext()
    .build()

  private val stub = LLMServiceGrpc.stub(channel)
  private val ollamaClient = new OllamaClient()

  private def cleanText(text: String): String = {
    text.replaceAll("\n", " ")
      .replaceAll("\\s+", " ")
      .replaceAll("\"", "'")
      .trim
  }

  def startConversation(initialPrompt: String): Future[Seq[ConversationTurn]] = {
    val turns = ArrayBuffer[ConversationTurn]()
    var currentPrompt = initialPrompt
    var cycle = 1
    val maxCycles = config.getInt("llm.max-cycles")

    def createNextPrompt(response: String, isOllama: Boolean): String = {
      val cleanedResponse = cleanText(response)
      if (isOllama) {
        s"""Do you have any comments on "$cleanedResponse"?"""
      } else {
        s"""How can you respond to "$cleanedResponse"?"""
      }
    }

    def processTurn(): Future[Option[ConversationTurn]] = {
      if (cycle > maxCycles) {
        logger.info(s"Reached maximum cycles ($maxCycles), ending conversation")
        Future.successful(None)
      } else {
        val request = ConversationRequest(
          prompt = currentPrompt,
          modelId = config.getString("llm.model-id"),
          maxTokenCount = config.getInt("llm.max-token-count"),
          temperature = config.getString("llm.temperature").toFloat,
          topP = config.getString("llm.top-p").toFloat
        )

        logger.info(s"Processing turn $cycle with prompt: $currentPrompt")

        for {
          bedrockResponse <- stub.streamConversation(request)
          bedrockText = cleanText(bedrockResponse.results.headOption.map(_.outputText).getOrElse(""))
          ollamaPrompt = createNextPrompt(bedrockText, isOllama = false)
          ollamaResponse <- ollamaClient.generate(ollamaPrompt)
        } yield {
          if (bedrockText.nonEmpty) {
            val turn = ConversationTurn(
              cycleNumber = cycle,
              bedrockInput = cleanText(currentPrompt),
              bedrockResponse = bedrockText,
              ollamaInput = ollamaPrompt,
              ollamaResponse = cleanText(ollamaResponse)
            )
            turns += turn
            cycle += 1
            currentPrompt = createNextPrompt(ollamaResponse, isOllama = true)
            Some(turn)
          } else None
        }
      }.recoverWith { case e: Exception =>
        logger.error(s"Error in conversation cycle $cycle", e)
        Future.successful(None)
      }
    }

    def processAllTurns(): Future[Seq[ConversationTurn]] = {
      processTurn().flatMap {
        case Some(_) => processAllTurns()
        case None => Future.successful(turns.toSeq)
      }
    }

    processAllTurns()
  }

  def shutdown(): Unit = {
    channel.shutdown()
  }
}