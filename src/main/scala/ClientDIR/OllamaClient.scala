package ClientDIR

import io.github.ollama4j.OllamaAPI
import io.github.ollama4j.utils.Options
import org.slf4j.LoggerFactory
import scala.concurrent.{ExecutionContext, Future}
import java.util.HashMap
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}

class OllamaClient()(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val config = ConfigFactory.load()

  private val api = {
    val ollamaAPI = new OllamaAPI(config.getString("ollama.host"))
    ollamaAPI.setRequestTimeoutSeconds(config.getInt("ollama.timeout"))
    ollamaAPI
  }

  private def configureOptions(): HashMap[String, Object] = {
    val options = new HashMap[String, Object]()

    // Convert String to Float for temperature
    options.put("temperature",
      Float.box(config.getString("ollama.temperature").toFloat))

    // Convert String to Integer for num_predict
    options.put("num_predict",
      Integer.valueOf(config.getString("ollama.num-predict")))

    // Convert String to Integer for timeout
    options.put("timeout",
      Integer.valueOf(config.getString("ollama.timeout")))

    options
  }

  private def cleanResponse(response: String): String = {
    response
      .replaceAll("(?i)certainly!?\\s*", "")
      .replaceAll("(?i)here'?s?\\s+(?:what|how)\\s+", "")
      .replaceAll("(?i)let'?s?\\s+(?:see|explore)\\s*", "")
      .replaceAll("(?i)based on (?:your|the) (?:statement|response|question)[,:]?\\s*", "")
      .replaceAll("(?i)interesting(?:ly)?[,!]?\\s*", "")
      .replaceAll("(?i)ah,?\\s+(?:yes|i see)!?\\s*", "") // Added common Ollama prefix
      .replaceAll("(?i)well,?\\s*", "") // Added common prefix
      .replaceAll("\\s+", " ")
      .trim match {
      case s if s.isEmpty => "Please continue the discussion."
      case s if !s.endsWith(".") && !s.endsWith("!") && !s.endsWith("?") => s + "."
      case s => s
    }
  }

  private def makeRequest(input: String, attempt: Int = 0, maxRetries: Int = 3): Future[String] = {
    if (attempt >= maxRetries) {
      logger.error(s"All $maxRetries attempts failed")
      Future.successful("I apologize, but I'm having trouble processing your request. Please try again.")
    } else {
      Future {
        Try {
          api.generate(
            config.getString("ollama.model"),
            input,
            false,
            new Options(configureOptions())
          )
        }
      }.flatMap {
        case Success(result) =>
          Future.successful(
            Option(result)
              .map(_.getResponse)
              .filter(_.nonEmpty)
              .map(cleanResponse)
              .getOrElse("Please continue with the discussion.")
          )
        case Failure(e) =>
          logger.warn(s"Attempt ${attempt + 1} failed: ${e.getMessage}")
          Thread.sleep(1000 * (attempt + 1)) // Exponential backoff
          makeRequest(input, attempt + 1, maxRetries)
      }.recoverWith { case e: Exception =>
        logger.warn(s"Error in attempt ${attempt + 1}: ${e.getMessage}")
        if (attempt < maxRetries - 1) {
          Thread.sleep(1000 * (attempt + 1))
          makeRequest(input, attempt + 1, maxRetries)
        } else {
          Future.successful("I apologize, but I'm having trouble processing your request. Please try again.")
        }
      }
    }
  }

  def generate(input: String): Future[String] = {
    if (input.isEmpty) {
      Future.successful("Please provide more context.")
    } else {
      logger.info(s"Sending request to Ollama with input length: ${input.length}")
      makeRequest(input)
    }
  }
}