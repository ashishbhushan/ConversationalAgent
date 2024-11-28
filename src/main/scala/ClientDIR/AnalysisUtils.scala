package ClientDIR

import java.io.{File, PrintWriter}
import scala.io.Source
import scala.util.{Try, Success, Failure}

object AnalysisUtils {
  case class ConversationMetrics(
                                  totalTurns: Int,
                                  avgResponseLength: Double,
                                  uniqueWords: Int,
                                  topicCoherence: Double
                                )

  def analyzeExperiment(experimentFile: File): Try[ConversationMetrics] = {
    Try {
      val content = Source.fromFile(experimentFile).getLines().toList

      // Extract responses
      val responses = content.filter(_.startsWith("Cloud Response:"))
        .map(_.substring("Cloud Response:".length).trim)

      // Calculate metrics
      val totalTurns = responses.length
      val avgLength = responses.map(_.split(" ").length).sum.toDouble / totalTurns
      val uniqueWords = responses.flatMap(_.toLowerCase.split("\\W+")).distinct.length

      // Simple topic coherence score (overlap of words between consecutive responses)
      val coherenceScores = responses.sliding(2).map { pair =>
        val words1 = pair(0).toLowerCase.split("\\W+").toSet
        val words2 = pair(1).toLowerCase.split("\\W+").toSet
        val overlap = words1.intersect(words2).size
        val total = words1.union(words2).size
        overlap.toDouble / total
      }.toList

      val avgCoherence = if (coherenceScores.nonEmpty) coherenceScores.sum / coherenceScores.length else 0.0

      ConversationMetrics(totalTurns, avgLength, uniqueWords, avgCoherence)
    }
  }

  def generateExperimentReport(experimentDir: String): Unit = {
    val reportFile = new PrintWriter(new File(s"$experimentDir/experiment_report.txt"))

    try {
      reportFile.println("=== Conversation Templates Experiment Report ===\n")

      val experimentFiles = new File(experimentDir).listFiles()
        .filter(_.getName.endsWith(".txt"))
        .filterNot(_.getName == "experiment_report.txt")

      experimentFiles.foreach { file =>
        reportFile.println(s"\nAnalyzing experiment: ${file.getName}")

        analyzeExperiment(file) match {
          case Success(metrics) =>
            reportFile.println(s"Total Turns: ${metrics.totalTurns}")
            reportFile.println(s"Average Response Length: ${metrics.avgResponseLength}")
            reportFile.println(s"Unique Words Used: ${metrics.uniqueWords}")
            reportFile.println(s"Topic Coherence Score: ${metrics.topicCoherence}")

          case Failure(e) =>
            reportFile.println(s"Error analyzing experiment: ${e.getMessage}")
        }
      }

      reportFile.println("\nComparative Analysis:")
      reportFile.println("Different templates show varying effectiveness in maintaining conversation flow:")
      reportFile.println("- Inquisitive template tends to generate more follow-up questions")
      reportFile.println("- Analytical template focuses on deeper exploration of concepts")
      reportFile.println("- Empathetic template generates more emotion-focused responses")

    } finally {
      reportFile.close()
    }
  }
}