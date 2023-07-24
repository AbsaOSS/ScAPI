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

package africa.absa.testing.scapi.json

import africa.absa.testing.scapi.logging.functions.Scribe
import africa.absa.testing.scapi.{ProjectLoadFailed, UndefinedConstantsInProperties}
import munit.FunSuite
import scribe.format.Formatter

class SuiteFactoryTest extends FunSuite {
  implicit val testLoggingFunctions: TestScribe = new TestScribe(SuiteFactory.getClass.toString)

  class TestScribe(logOrigin: String, logLevel: String = Scribe.INFO, formatter: Option[Formatter] = None) extends Scribe(logOrigin, logLevel, formatter) {
    var logs = List.empty[String]

    override def error(message: String, t: Throwable = None.orNull): Unit = {
      logs = message :: logs
      super.error(message)
    }
  }

  /*
    fromFile
   */
  // Note: positive way covered in ScAPIRunnerJobTest.class

  test("fromFile - report fails - undefined constant") {
    val constants: Map[String, String] = Map.empty
    val properties: Map[String, String] = Map.empty
    val environment: Environment = Environment(constants, properties)
    val testRootPath: String = getClass.getResource("/project_with_issues").getPath

    val caught = intercept[ProjectLoadFailed] {
      SuiteFactory.fromFiles(environment, testRootPath, "(.*)", "json")
    }

    assert(caught.isInstanceOf[ProjectLoadFailed])
    assert(testLoggingFunctions.logs.head.contains("Undefined constant(s): 'constants.no_provided' in ''Header' action."))
    assert(testLoggingFunctions.logs(1).contains("Not all suites loaded. Failed suites:"))
  }

  /*
    loadJsonSuite
   */
  // Note: positive way covered in ScAPIRunnerJobTest.class
  // Note: negative way covered during tests of fromFile

  /*
    loadJsonSuiteConstants
   */
  test("loadJsonSuite - constants loaded") {
    val suiteFilePath = getClass.getResource("/test_project/suites/gui-controller").getPath
    val suiteName = "getUserCurrent"
    val properties: Map[String, String] = Map("env.bearerToken" -> "token#value")
    val expected: Map[String, String] = Map(
      "constants.header_auth" -> "Authorization",
      "constants.content_type" -> "application/json",
      "constants.header_bearer_token" -> "Bearer token#value",
      "constants.unique-key-name-2" -> "value"
    )

    val actual: Map[String, String] = SuiteFactory.loadJsonSuiteConstants(suiteFilePath, suiteName, properties).constants

    assertEquals(clue(expected), clue(actual))
  }

  test("loadJsonSuite - no constants file exist") {
    val suiteFilePath = getClass.getResource("/test_project/suites/gui-controller").getPath
    val suiteName = "notExist"
    val properties: Map[String, String] = Map.empty
    val expected: Map[String, String] = Map.empty

    val actual: Map[String, String] = SuiteFactory.loadJsonSuiteConstants(suiteFilePath, suiteName, properties).constants

    assertEquals(clue(expected), clue(actual))
  }

  test("loadJsonSuite - not all references resolved") {
    val suiteFilePath = getClass.getResource("/test_project/suites/gui-controller").getPath
    val suiteName = "getUserCurrent"
    val properties: Map[String, String] = Map.empty

    intercept[UndefinedConstantsInProperties] {
      SuiteFactory.loadJsonSuiteConstants(suiteFilePath, suiteName, properties).constants
    }
  }

  /*
    filterOnlyOrAll
   */
  test("filterOnlyOrAll - only used - once") {
    val suites = Set(
      Suite(endpoint = "endpoint1", tests = Set(
        SuiteTestScenario(name = "test1", categories = Set("SMOKE"), headers = Set.empty, actions = Set.empty, assertions = Set.empty, only = Some(false)),
        SuiteTestScenario(name = "test2", categories = Set("SMOKE"), headers = Set.empty, actions = Set.empty, assertions = Set.empty, only = Some(true))
      )),
      Suite(endpoint = "endpoint2", tests = Set(
        SuiteTestScenario(name = "test1", categories = Set("SMOKE"), headers = Set.empty, actions = Set.empty, assertions = Set.empty, only = Some(false)),
      ))
    )

    val filteredSuites: Set[Suite] = SuiteFactory.filterOnlyOrAll(suites)

    assertEquals(filteredSuites.size, 1)

    val filteredSuite = filteredSuites.head
    assertEquals(filteredSuite.endpoint, "endpoint1")
    assertEquals(filteredSuite.tests.size, 1)

    val filteredTest = filteredSuite.tests.head
    assertEquals(filteredTest.name, "test2")
    assertEquals(filteredTest.only, Some(true))
  }

  test("fromFile - only used - twice") {
    val suites = Set(
      Suite(endpoint = "endpoint1", tests = Set(
        SuiteTestScenario(name = "test1", categories = Set("SMOKE"), headers = Set.empty, actions = Set.empty, assertions = Set.empty, only = Some(false)),
        SuiteTestScenario(name = "test2", categories = Set("SMOKE"), headers = Set.empty, actions = Set.empty, assertions = Set.empty, only = Some(true))
      )),
      Suite(endpoint = "endpoint2", tests = Set(
        SuiteTestScenario(name = "test1", categories = Set("SMOKE"), headers = Set.empty, actions = Set.empty, assertions = Set.empty, only = Some(true)),
      ))
    )

    val filteredSuites: Set[Suite] = SuiteFactory.filterOnlyOrAll(suites)

    assertEquals(filteredSuites.size, 0)
  }
}
