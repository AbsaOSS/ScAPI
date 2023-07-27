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

import africa.absa.testing.scapi.{TestResults, StdOutReporter}
import munit.FunSuite

import java.io.ByteArrayOutputStream

class StdOutReporterTest extends FunSuite {

  /*
    printReport
   */
  test("empty results") {
    val baos = new ByteArrayOutputStream()

    Console.withOut(baos) {
      StdOutReporter.printReport(Set.empty)
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
      TestResults(suiteName = "Suite 1", testName = "Test 1", status = TestResults.Success, duration = Some(100L), categories = Some("Category 1")),
      TestResults(suiteName = "Suite 1", testName = "Test 2", status = TestResults.Failure, duration = Some(200L), categories = Some("Category 2"), errMessage = Some("Error message")),
      TestResults.withBooleanStatus(suiteName = "Suite 2", testName = "Test 1", status = true, duration = Some(50L), categories = Some("Category 3"))
    )

    val baos = new ByteArrayOutputStream()

    Console.withOut(baos) {
      StdOutReporter.printReport(testResults)
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
    assertEquals(clue(output.contains("| Suite 1    | Test 1    |           100 | Success | Category 1    |")), true)
    assertEquals(clue(output.contains("| Suite 1    | Test 2    |           200 | Failure | Category 2    |")), true)
    assertEquals(clue(output.contains("| Suite 2    | Test 1    |            50 | Success | Category 3    |")), true)

    // error from detail
    assertEquals(clue(output.contains("Error: Error message")), true)
  }
  test("results all success") {
    val testResults = Set(
      TestResults(suiteName = "Suite 1", testName = "Test 1", status = TestResults.Success, duration = Some(100L), categories = Some("Category 1")),
      TestResults(suiteName = "Suite 1", testName = "Test 2", status = TestResults.Success, duration = Some(200L), categories = Some("Category 2")),
      TestResults(suiteName = "Suite 2", testName = "Test 1", status = TestResults.Success, duration = Some(50L), categories = Some("Category 3"))
    )

    val baos = new ByteArrayOutputStream()

    Console.withOut(baos) {
      StdOutReporter.printReport(testResults)
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
    assertEquals(clue(output.contains("| Suite 1    | Test 1    |           100 | Success | Category 1    |")), true)
    assertEquals(clue(output.contains("| Suite 1    | Test 2    |           200 | Success | Category 2    |")), true)
    assertEquals(clue(output.contains("| Suite 2    | Test 1    |            50 | Success | Category 3    |")), true)
  }
}
