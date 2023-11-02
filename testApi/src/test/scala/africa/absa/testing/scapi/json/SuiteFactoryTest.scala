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

import africa.absa.testing.scapi.json.factory.SuiteFactory
import africa.absa.testing.scapi.model.suite.{TestSet, Suite, SuiteTestScenario}
import africa.absa.testing.scapi.{ProjectLoadFailedException, UndefinedConstantsInPropertiesException}
import munit.FunSuite
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.OutputStreamAppender
import org.apache.logging.log4j.core.layout.PatternLayout

import java.io.ByteArrayOutputStream

class SuiteFactoryTest extends FunSuite {

  private var ctx: LoggerContext = _
  private var appender: OutputStreamAppender = _
  private var out: ByteArrayOutputStream = _

  private def initTestLogger(): Unit = {
    ctx = LogManager.getContext(false).asInstanceOf[LoggerContext]
    val config = ctx.getConfiguration
    val layout = PatternLayout.createDefaultLayout(config)
    out = new ByteArrayOutputStream()
    appender = OutputStreamAppender.createAppender(layout, null, out, "Capturing", false, true)
    appender.start()
    config.addAppender(appender)
    config.getRootLogger.addAppender(appender, null, null)
    ctx.updateLoggers()
  }

  private def stopTestLogger(): Unit = {
    val config = ctx.getConfiguration
    config.getRootLogger.removeAppender("Capturing")
    ctx.updateLoggers()
    appender.stop()
    out.close()
  }

  /*
    fromFile
   */
  // Note: positive way covered in ScAPIRunnerTest.class

  test("fromFile - report fails - undefined constant") {
    val constants: Map[String, String] = Map.empty
    val properties: Map[String, String] = Map.empty
    val environment: Environment = Environment(constants, properties)
    val testRootPath: String = getClass.getResource("/project_with_issues").getPath

    initTestLogger()

    try {
      val caught = intercept[ProjectLoadFailedException] {
        SuiteFactory.fromFiles(environment, testRootPath, "(.*)", "json")
      }

      assert(caught.isInstanceOf[ProjectLoadFailedException])
      assert(out.toString.contains("Undefined constant(s): 'constants.no_provided' in ''Header' action."))
      assert(out.toString.contains("Not all suites loaded. Failed suites:"))
    } finally {
      stopTestLogger()
    }
  }

  /*
    loadJsonSuite
   */
  // Note: positive way covered in ScAPIRunnerTest.class
  // Note: negative way covered during tests of fromFile

  /*
    loadJsonSuiteConstants
   */
  test("loadJsonSuite - constants loaded") {
    val suiteFilePath = getClass.getResource("/test_project/suites/gui-controller").getPath
    val suiteName = "getUserCurrent"
    val properties: Map[String, String] = Map("env.basic_token" -> "token#value")
    val expected: Map[String, String] = Map(
      "constants.header_auth" -> "Authorization",
      "constants.content_type" -> "application/json",
      "constants.header_basic_token" -> "Basic token#value",
      "constants.unique-key-name-2" -> "value"
    )

    val actual: Map[String, String] = SuiteFactory.loadJsonSuiteConstants(suiteFilePath, suiteName, properties).constants

    assert(clue(expected) == clue(actual))
  }

  test("loadJsonSuite - no constants file exist") {
    val suiteFilePath = getClass.getResource("/test_project/suites/gui-controller").getPath
    val suiteName = "notExist"
    val properties: Map[String, String] = Map.empty
    val expected: Map[String, String] = Map.empty

    val actual: Map[String, String] = SuiteFactory.loadJsonSuiteConstants(suiteFilePath, suiteName, properties).constants

    assert(clue(expected) == clue(actual))
  }

  test("loadJsonSuite - not all references resolved") {
    val suiteFilePath = getClass.getResource("/test_project/suites/gui-controller").getPath
    val suiteName = "getUserCurrent"
    val properties: Map[String, String] = Map.empty

    interceptMessage[UndefinedConstantsInPropertiesException]("Undefined constant(s): 'env.basic_token' in ''SuiteConstants' action.'.") {
      SuiteFactory.loadJsonSuiteConstants(suiteFilePath, suiteName, properties).constants
    }
  }

  /*
    filterOnlyOrAll
   */
  test("filterOnlyOrAll - only used - once") {
    val suitesBundles = Set(
      Suite(suite = TestSet(name = "name1", tests = Set(
        SuiteTestScenario(name = "test1", categories = Seq("SMOKE"), headers = Seq.empty, actions = Seq.empty, responseActions = Seq.empty, only = Some(false)),
        SuiteTestScenario(name = "test2", categories = Seq("SMOKE"), headers = Seq.empty, actions = Seq.empty, responseActions = Seq.empty, only = Some(true))
      ))),
      Suite(suite = TestSet(name = "name1", tests = Set(
        SuiteTestScenario(name = "test1", categories = Seq("SMOKE"), headers = Seq.empty, actions = Seq.empty, responseActions = Seq.empty, only = Some(false)),
        SuiteTestScenario(name = "test2", categories = Seq("SMOKE"), headers = Seq.empty, actions = Seq.empty, responseActions = Seq.empty, only = Some(true))
      ))),
      Suite(suite = TestSet(name = "name2", tests = Set(
        SuiteTestScenario(name = "test1", categories = Seq("SMOKE"), headers = Seq.empty, actions = Seq.empty, responseActions = Seq.empty, only = Some(false)),
      ))))

    val filteredSuiteBundles: Set[Suite] = SuiteFactory.filterOnlyOrAll(suitesBundles)

    assertEquals(filteredSuiteBundles.size, 1)

    val filteredSuite = filteredSuiteBundles.head.suite
    assert("name1" == clue(filteredSuite.name))
    assert(1 == clue(filteredSuite.tests.size))

    val filteredTest = filteredSuite.tests.head
    assert("test2" == clue(filteredTest.name))
    assert(clue(Some(true)) == clue(filteredTest.only))
  }

  test("fromFile - only used - twice") {
    val suitesBundles = Set(
      Suite(suite = TestSet(name = "name1", tests = Set(
        SuiteTestScenario(name = "test1", categories = Seq("SMOKE"), headers = Seq.empty, actions = Seq.empty, responseActions = Seq.empty, only = Some(false)),
        SuiteTestScenario(name = "test2", categories = Seq("SMOKE"), headers = Seq.empty, actions = Seq.empty, responseActions = Seq.empty, only = Some(true))
      ))),
      Suite(suite = TestSet(name = "name2", tests = Set(
        SuiteTestScenario(name = "test1", categories = Seq("SMOKE"), headers = Seq.empty, actions = Seq.empty, responseActions = Seq.empty, only = Some(true)),
      ))))

    val filteredSuiteBundles: Set[Suite] = SuiteFactory.filterOnlyOrAll(suitesBundles)

    assert(0 == clue(filteredSuiteBundles.size))
  }
}
