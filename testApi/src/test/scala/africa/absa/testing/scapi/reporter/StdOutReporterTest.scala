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

package africa.absa.testing.scapi.reporter

import africa.absa.testing.scapi.AssertionException
import africa.absa.testing.scapi.model.{SuiteResult, SuiteResultType}
import munit.FunSuite

import java.io.ByteArrayOutputStream
import scala.util.{Failure, Success}

class StdOutReporterTest extends FunSuite {

  val successTestResults: List[SuiteResult] = List(
    SuiteResult(SuiteResultType.TEST_SUITE,
      suiteName = "Suite 1",
      name = "Test 1",
      result = Success(()),
      duration = Some(100L),
      categories = Some("Category 1")),
    SuiteResult(SuiteResultType.TEST_SUITE,
      suiteName = "Suite 1",
      name = "Test 2",
      result = Success(()),
      duration = Some(200L),
      categories = Some("Category 2")
    ),
    SuiteResult(SuiteResultType.TEST_SUITE,
      suiteName = "Suite 2",
      name = "Test 1",
      result = Success(()),
      duration = Some(50L),
      categories = Some("Category 3"))
  )

  val mixedSuccessTestResults: List[SuiteResult] = List(
    SuiteResult(SuiteResultType.TEST_SUITE,
      suiteName = "Suite 1",
      name = "Test 1",
      result = Success(()),
      duration = Some(100L),
      categories = Some("Category 1")),
    SuiteResult(SuiteResultType.TEST_SUITE,
      suiteName = "Suite 1",
      name = "Test 2",
      result = Failure(AssertionException("Error message")),
      duration = Some(200L),
      categories = Some("Category 2")),
    SuiteResult(SuiteResultType.TEST_SUITE,
      suiteName = "Suite 2",
      name = "Test 1",
      result = Success(()),
      duration = Some(50L),
      categories = Some("Category 3"))
  )

  /*
    printReport
   */
  test("empty results") {
    val baos = new ByteArrayOutputStream()

    Console.withOut(baos) {
      StdOutReporter.printReport(List.empty)
    }

    // Get the output as a string
    val output = baos.toString

    // Assertions
    assertEquals(true, clue(output.contains("Simple Text Report")))
    assertEquals(true, clue(output.contains("Number of tests run: 0")))
    assertEquals(true, clue(output.contains("Number of successful tests: 0")))
    assertEquals(true, clue(output.contains("Number of failed tests: 0")))
    assertEquals(true, clue(output.contains("End Report")))
  }

  test("full results with failed".only) {
    /*
      "Full":
        min 1 Success test
        min 1 Failed test
        min 2 Suites
        min 1 suites with min 2 tests
     */

    val baos = new ByteArrayOutputStream()

    Console.withOut(baos) {
      StdOutReporter.printReport(mixedSuccessTestResults)
    }

    // Get the output as a string
    val output = baos.toString

    // Assertions
    // report header & tail
    assertEquals(true, clue(output.contains("Simple Text Report")))
    assertEquals(true, clue(output.contains("Number of tests run: 3")))
    assertEquals(true, clue(output.contains("Number of successful tests: 2")))
    assertEquals(true, clue(output.contains("Number of failed tests: 1")))
    assertEquals(true, clue(output.contains("End Report")))

    // suite summary
    assertEquals(true, clue(output.contains("Suite: Suite 1, Total tests: 2, Successful: 1, Failed: 1")))
    assertEquals(true, clue(output.contains("Suite: Suite 2, Total tests: 1, Successful: 1, Failed: 0")))

    // summary of all tests
    val updatedOutput = output.replace(" ", "")
    assertEquals(true, clue(updatedOutput.contains("|Suite1|Test1|100|Success|Category1|")))
    assertEquals(true, clue(updatedOutput.contains("|Suite1|Test2|200|Failure|Category2|")))
    assertEquals(true, clue(updatedOutput.contains("|Suite2|Test1|50|Success|Category3|")))

    // error from detail
    assertEquals(true, clue(output.contains("Assertion failed: Error message")))
  }

  test("results all success") {
    val baos = new ByteArrayOutputStream()

    Console.withOut(baos) {
      StdOutReporter.printReport(successTestResults)
    }

    // Get the output as a string
    val output = baos.toString

    // Assertions
    // report header & tail
    assertEquals(true, clue(output.contains("Simple Text Report")))
    assertEquals(true, clue(output.contains("Number of tests run: 3")))
    assertEquals(true, clue(output.contains("Number of successful tests: 3")))
    assertEquals(true, clue(output.contains("Number of failed tests: 0")))
    assertEquals(true, clue(output.contains("End Report")))

    // suite summary
    assertEquals(true, clue(output.contains("Suite: Suite 1, Total tests: 2, Successful: 2, Failed: 0")))
    assertEquals(true, clue(output.contains("Suite: Suite 2, Total tests: 1, Successful: 1, Failed: 0")))

    // summary of all tests
    val updatedOutput = output.replace(" ", "")
    assertEquals(true, clue(updatedOutput.contains("|Suite1|Test1|100|Success|Category1|")))
    assertEquals(true, clue(updatedOutput.contains("|Suite1|Test2|200|Success|Category2|")))
    assertEquals(true, clue(updatedOutput.contains("|Suite2|Test1|50|Success|Category3|")))
  }
}
