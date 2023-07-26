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

package africa.absa.testing.scapi.utils

import africa.absa.testing.scapi.data.SuiteResults
import africa.absa.testing.scapi.reporter.TxtReporter
import munit.FunSuite

import java.io.ByteArrayOutputStream

class TxtReporterTest extends FunSuite {

  /*
    printReport
   */
  test("empty results") {
    val baos = new ByteArrayOutputStream()

    Console.withOut(baos) {
      TxtReporter.printReport(Set.empty)
    }

    // Get the output as a string
    val output = baos.toString

    // Assertions
    assertEquals(clue(output.contains("Simple Text Report")), true)
    assertEquals(clue(output.contains("Number of tests run: 0")), true)
    assertEquals(clue(output.contains("Number of successful tests: 0")), true)
    assertEquals(clue(output.contains("Number of failed tests: 0")), true)
    assertEquals(clue(output.contains("End Report")), true)
  }

  test("full results with failed") {
    /*
      "Full":
        min 1 Success test
        min 1 Failed test
        min 2 Suites
        min 1 suites with min 2 tests
     */
    val testResults = Set(
      SuiteResults.successTest("Suite 1", "Test 1", Some(100L), "Category 1"),
      SuiteResults.failureTest("Suite 1", "Test 2", Some(200L), "Category 2", "Error message"),
      SuiteResults.successTest("Suite 2", "Test 1", Some(50L), "Category 3")
    )

    val baos = new ByteArrayOutputStream()

    Console.withOut(baos) {
      TxtReporter.printReport(testResults)
    }

    // Get the output as a string
    val output = baos.toString

    // Assertions
    // report header & tail
    assertEquals(clue(output.contains("Simple Text Report")), true)
    assertEquals(clue(output.contains("Number of tests run: 3")), true)
    assertEquals(clue(output.contains("Number of successful tests: 2")), true)
    assertEquals(clue(output.contains("Number of failed tests: 1")), true)
    assertEquals(clue(output.contains("End Report")), true)

    // suite summary
    assertEquals(clue(output.contains("Suite: Suite 1, Total tests: 2, Successful: 1, Failed: 1")), true)
    assertEquals(clue(output.contains("Suite: Suite 2, Total tests: 1, Successful: 1, Failed: 0")), true)

    // summary of all tests
    assertEquals(clue(output.contains("| Suite 1   | Test 1   |           100 | Success | Category 1   |")), true)
    assertEquals(clue(output.contains("| Suite 1   | Test 2   |           200 | Failure | Category 2   |")), true)
    assertEquals(clue(output.contains("| Suite 2   | Test 1   |            50 | Success | Category 3   |")), true)

    // error from detail
    assertEquals(clue(output.contains("Error: Error message")), true)
  }
  test("results all success") {
    val testResults = Set(
      SuiteResults.successTest("Suite 1", "Test 1", Some(100L), "Category 1"),
      SuiteResults.successTest("Suite 1", "Test 2", Some(200L), "Category 2"),
      SuiteResults.successTest("Suite 2", "Test 1", Some(50L), "Category 3")
    )

    val baos = new ByteArrayOutputStream()

    Console.withOut(baos) {
      TxtReporter.printReport(testResults)
    }

    // Get the output as a string
    val output = baos.toString

    // Assertions
    // report header & tail
    assertEquals(clue(output.contains("Simple Text Report")), true)
    assertEquals(clue(output.contains("Number of tests run: 3")), true)
    assertEquals(clue(output.contains("Number of successful tests: 3")), true)
    assertEquals(clue(output.contains("Number of failed tests: 0")), true)
    assertEquals(clue(output.contains("End Report")), true)

    // suite summary
    assertEquals(clue(output.contains("Suite: Suite 1, Total tests: 2, Successful: 2, Failed: 0")), true)
    assertEquals(clue(output.contains("Suite: Suite 2, Total tests: 1, Successful: 1, Failed: 0")), true)

    // summary of all tests
    assertEquals(clue(output.contains("| Suite 1   | Test 1   |           100 | Success | Category 1   |")), true)
    assertEquals(clue(output.contains("| Suite 1   | Test 2   |           200 | Success | Category 2   |")), true)
    assertEquals(clue(output.contains("| Suite 2   | Test 1   |            50 | Success | Category 3   |")), true)
  }
}
