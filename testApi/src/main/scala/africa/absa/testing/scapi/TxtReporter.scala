package africa.absa.testing.scapi

object TxtReporter {
  def printReport(testResults: Set[TestResults]): Unit = {
    def createFormattedLine(line: Option[String] = None, maxChars: Int = 80, repeatChar: Char = '*'): String =
      line match {
        case Some(text) => s"${repeatChar.toString * ((maxChars - text.length - 2) / 2)} $text ${repeatChar.toString * ((maxChars - text.length - 2) / 2)}"
        case None => repeatChar.toString * maxChars
      }

    println(createFormattedLine())
    println(createFormattedLine(Some("Simple Text Report")))
    println(createFormattedLine())

    val successCount = testResults.count(_.status == TestResults.Success)
    val failureCount = testResults.size - successCount

    println(s"Number of tests run: ${testResults.size}")
    println(s"Number of successful tests: $successCount")
    println(s"Number of failed tests: $failureCount")

    val suiteSummary = testResults.groupBy(_.suite).map {
      case (suiteName, results) => (suiteName, results.size, results.count(_.status == TestResults.Success))
    }

    println()
    println(createFormattedLine(repeatChar = '-'))
    println("Suites Summary:")
    println(createFormattedLine(repeatChar = '-'))
    suiteSummary.foreach {
      case (suiteName, total, successCount) =>
        println(s"Suite: $suiteName, Total tests: $total, Successful: $successCount, Failed: ${total - successCount}")
    }

    println()
    println(createFormattedLine(repeatChar = '-'))
    println("Summary of all tests:")
    println(createFormattedLine(repeatChar = '-'))

    println(s"| %-20s | %-30s | %-12s | %-7s |".format("Suite", "Test", "Duration (ms)", "Status"))
    println(s"| ${"-" * 20} | ${"-" * 30} | ${"-" * 13} | ${"-" * 7} |")
    testResults
      .toList
      .sortBy(_.suite)
      .foreach { result =>
        val duration = result.duration.map(_.toString).getOrElse("NA")
        println(s"| %-20s | %-30s | %-13s | %-7s |".format(result.suite, result.test, duration, result.status))
      }

    if (failureCount > 0) {
      println()
      println(createFormattedLine(repeatChar = '-'))
      println("Details of failed tests:")
      println(createFormattedLine(repeatChar = '-'))
      testResults.filter(_.status == TestResults.Failure).toList.sortBy(_.test).foreach { result =>
        println(s"Suite: ${result.suite}")
        println(s"Test: ${result.test}")
        println(s"Error: ${result.errMessage.getOrElse("No details available")}")
        println(s"Duration: ${result.duration.getOrElse("NA")} ms")
        println()
      }
    }
  }
}
