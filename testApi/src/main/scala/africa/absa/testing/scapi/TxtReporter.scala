package africa.absa.testing.scapi

object TxtReporter {
  def printReport(testResults: Set[TestResults]): Unit = {
    def createFormattedLine(line: Option[String] = None, maxChars: Int = 80, repeatChar: Char = '*'): String =
      line match {
        case Some(text) => s"${repeatChar.toString * ((maxChars - text.length - 2) / 2)} $text ${repeatChar.toString * ((maxChars - text.length - 2) / 2)}"
        case None => repeatChar.toString * maxChars
      }


    // Calculate the max lengths
    val maxSuiteLength = testResults.map(_.suite.length).max + 2
    val maxTestLength = testResults.map(_.test.length).max + 2
    val maxTestCategoriesLength = testResults.map(_.categories.length).max + 2
    val maxChars = 33 + maxSuiteLength + maxTestLength + maxTestCategoriesLength

    def printTableRowSplitter(): Unit = println(s"| ${"-" * maxSuiteLength} | ${"-" * maxTestLength} | ${"-" * 13} | ${"-" * 7} | ${"-" * maxTestCategoriesLength} |")
    def printFormattedLineHeader(): Unit = println(createFormattedLine(maxChars = maxChars))
    def printFormattedLineNoHeader(): Unit = println(createFormattedLine(repeatChar = '-', maxChars = maxChars))
    def printHeader(title: String): Unit = {
      printFormattedLineHeader()
      println(createFormattedLine(Some(title), maxChars = maxChars))
      printFormattedLineHeader()
    }
    def printInnerHeader(title: String): Unit = {
      println()
      printFormattedLineNoHeader()
      println(s"$title:")
      printFormattedLineNoHeader()
    }

    printHeader("Simple Text Report")

    val successCount = testResults.count(_.status == TestResults.Success)
    val failureCount = testResults.size - successCount

    println(s"Number of tests run: ${testResults.size}")
    println(s"Number of successful tests: $successCount")
    println(s"Number of failed tests: $failureCount")

    val suiteSummary = testResults.groupBy(_.suite).map {
      case (suiteName, results) => (suiteName, results.size, results.count(_.status == TestResults.Success))
    }

    printInnerHeader("Suites Summary")
    suiteSummary.foreach {
      case (suiteName, total, successCount) =>
        println(s"Suite: $suiteName, Total tests: $total, Successful: $successCount, Failed: ${total - successCount}")
    }

    printInnerHeader("Summary of all tests")
    println(s"| %-${maxSuiteLength}s | %-${maxTestLength}s | %-13s | %-7s | %-${maxTestCategoriesLength}s | ".format("Suite Name", "Test Name", "Duration (ms)", "Status", "Categories"))
    printTableRowSplitter()
    val resultsList = testResults.toList.sortBy(_.suite)
    resultsList.zipWithIndex.foreach { case (result, index) =>
      val duration = result.duration.map(_.toString).getOrElse("NA")
      println(s"| %-${maxSuiteLength}s | %-${maxTestLength}s | %13s | %-7s | %-${maxTestCategoriesLength}s | ".format(result.suite, result.test, duration, result.status, result.categories))

      // Check if the index + 1 is divisible by 4 (since index is 0-based)
      if ((index + 1) % 3 == 0) printTableRowSplitter()
    }

    if (failureCount > 0) {
      printInnerHeader("Details of failed tests")
      testResults.filter(_.status == TestResults.Failure).toList.sortBy(_.test).foreach { result =>
        println(s"Suite: ${result.suite}")
        println(s"Test: ${result.test}")
        println(s"Error: ${result.errMessage.getOrElse("No details available")}")
        println(s"Duration: ${result.duration.getOrElse("NA")} ms")
        println(s"Category: ${result.categories}")
        println()
      }
    }

    printHeader("End Report")
  }
}
