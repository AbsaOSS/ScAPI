/*
 * Copyright 2023 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package africa.absa.testing.scapi

object StdOutReporter {
  def printReport(testResults: Set[TestResults]): Unit = {
    def createFormattedLine(line: Option[String] = None, maxChars: Int = 80, repeatChar: Char = '*'): String =
      line match {
        case Some(text) => s"${repeatChar.toString * ((maxChars - text.length - 2) / 2)} $text ${repeatChar.toString * ((maxChars - text.length - 2) / 2)}"
        case None => repeatChar.toString * maxChars
      }

    // Calculate the max lengths
    val maxSuiteLength = if (testResults.isEmpty) 10 else testResults.map(_.suiteName.length).max + 3
    val maxTestLength = if (testResults.isEmpty) 10 else testResults.map(_.testName.length).max + 3
    val maxTestCategoriesLength = if (testResults.isEmpty) 10 else math.max(testResults.map(_.categories.getOrElse("").length).max + 3, 10)
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

    if (testResults.nonEmpty) {
      val suiteSummary = testResults.groupBy(_.suiteName).map {
        case (suiteName, results) => (suiteName, results.size, results.count(_.status == TestResults.Success))
      }

      printInnerHeader("Suites Summary")
      suiteSummary.foreach {
        case (suiteName, total, successCount) =>
          println(s"Suite: $suiteName, Total tests: $total, Successful: $successCount, Failed: ${total - successCount}")
      }

      printInnerHeader("Summary of all tests")
      printTableRowSplitter()
      println(s"| %-${maxSuiteLength}s | %-${maxTestLength}s | %-13s | %-7s | %-${maxTestCategoriesLength}s | ".format("Suite Name", "Test Name", "Duration (ms)", "Status", "Categories"))
      printTableRowSplitter()
      val resultsList = testResults.toList.sortBy(_.suiteName)
      resultsList.zipWithIndex.foreach { case (result, index) =>
        val duration = result.duration.map(_.toString).getOrElse("NA")
        println(s"| %-${maxSuiteLength}s | %-${maxTestLength}s | %13s | %-7s | %-${maxTestCategoriesLength}s | ".format(result.suiteName, result.testName, duration, result.status, result.categories.getOrElse("")))

        // Check if the index + 1 is divisible by 4 (since index is 0-based)
        if ((index + 1) % 3 == 0) printTableRowSplitter()
      }

      if (failureCount > 0) {
        printInnerHeader("Details of failed tests")
        testResults.filter(_.status == TestResults.Failure).toList.sortBy(_.testName).foreach { result =>
          println(s"Suite: ${result.suiteName}")
          println(s"Test: ${result.testName}")
          println(s"Error: ${result.errMessage.getOrElse("No details available")}")
          println(s"Duration: ${result.duration.getOrElse("NA")} ms")
          println(s"Category: ${result.categories.getOrElse("")}")
          println()
        }
      }
    }

    printHeader("End Report")
  }
}
