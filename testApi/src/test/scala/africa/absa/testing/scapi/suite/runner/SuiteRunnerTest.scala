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

package africa.absa.testing.scapi.suite.runner

import africa.absa.testing.scapi.json.{Action, Environment, Header, ResponseAction}
import africa.absa.testing.scapi.logging.Logger
import africa.absa.testing.scapi.model._
import africa.absa.testing.scapi.rest.RestClient
import africa.absa.testing.scapi.rest.request.RequestHeaders
import africa.absa.testing.scapi.rest.response.{AssertResponseActionType, ResponseActionGroupType}
import africa.absa.testing.scapi.utils.cache.RuntimeCache
import munit.FunSuite
import org.apache.logging.log4j.Level

class SuiteRunnerTest extends FunSuite {

  val header: Header = Header(name = RequestHeaders.AUTHORIZATION, value = "Basic abcdefg")
  val action: Action = Action(methodName = "get", url = "nice url")
  val actionNotSupported: Action = Action(methodName = "wrong", url = "nice url")
  val responseAction: ResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.STATUS_CODE_EQUALS.toString, Map("code" -> "200"))
  val method: Method = Method(name = "test", headers = Seq(header), actions = Seq(action), responseActions = Seq(responseAction))
  val methodNotSupported: Method = Method(name = "test", headers = Seq(header), actions = Seq(actionNotSupported), responseActions = Seq(responseAction))

  val suitesBundles: Set[SuiteBundle] = Set(
    SuiteBundle(suite = Suite(endpoint = "endpoint1", tests = Set(
      SuiteTestScenario(name = "test1", categories = Seq("SMOKE"),
        headers = Seq(header), actions = Seq(action), responseActions = Seq(responseAction), only = Some(true)),
      SuiteTestScenario(name = "test2", categories = Seq("SMOKE"),
        headers = Seq(header), actions = Seq(action), responseActions = Seq(responseAction), only = Some(true))
    ))),
    SuiteBundle(
      suiteBefore = Some(SuiteBefore(name = "suiteBefore", methods = Set(method))),
      suite = Suite(endpoint = "endpoint2", tests = Set(
        SuiteTestScenario(name = "test1", categories = Seq("SMOKE"),
          headers = Seq(header), actions = Seq(action), responseActions = Seq(responseAction), only = Some(false)),
    )),
      suiteAfter = Some(SuiteAfter(name = "suiteAfter", methods = Set(method))),
    ))

  val suitesBundleNoBefore: Set[SuiteBundle] = Set(
    SuiteBundle(
      suite = Suite(endpoint = "endpoint2", tests = Set(
        SuiteTestScenario(name = "test1", categories = Seq("SMOKE"),
          headers = Seq(header), actions = Seq(action), responseActions = Seq(responseAction), only = Some(false)),
    )),
      suiteAfter = Some(SuiteAfter(name = "suiteAfter", methods = Set(method))),
    ))

  val suitesBundleAfterMethodNotSupported: Set[SuiteBundle] = Set(
    SuiteBundle(
      suite = Suite(endpoint = "endpoint2", tests = Set(
        SuiteTestScenario(name = "test1", categories = Seq("SMOKE"),
          headers = Seq(header), actions = Seq(action), responseActions = Seq(responseAction), only = Some(false)),
    )),
      suiteAfter = Some(SuiteAfter(name = "suiteAfter", methods = Set(methodNotSupported))),
    ))

  val suitesBundleNoAfter: Set[SuiteBundle] = Set(
    SuiteBundle(
      suiteBefore = Some(SuiteBefore(name = "suiteBefore", methods = Set(method))),
      suite = Suite(endpoint = "endpoint2", tests = Set(
        SuiteTestScenario(name = "test1", categories = Seq("SMOKE"),
          headers = Seq(header), actions = Seq(action), responseActions = Seq(responseAction), only = Some(false)),
    ))))

  val constants: Map[String, String] = Map(
    "notUsed" -> "never used",
    "port" -> "8080",
    "server" -> "localhost"
  )
  val propertiesResolved: Map[String, String] = Map(
    "url" -> "http://localhost:8080/restcontroller"
  )

  val environment: Environment = Environment(constants, propertiesResolved)

  override def beforeEach(context: BeforeEach): Unit = {
    RuntimeCache.reset()

    Logger.setLevel(Level.DEBUG)

    super.beforeEach(context) // important to call this
  }

  /*
    runSuite
   */

  test("runSuite - SuiteBefore exists") {
    val suiteResults: List[SuiteResult] = SuiteRunner.runSuites(suitesBundles, environment, () => new RestClient(FakeScAPIRequestSender))

    val beforeSuiteResult: SuiteResult = suiteResults.find(result =>
      result.resultType == SuiteResultType.BEFORE_SUITE && result.suiteName == "endpoint2").get

    assertEquals(5, clue(suiteResults.size))
    assertEquals("test", clue(beforeSuiteResult.name))
    assertEquals(true, clue(beforeSuiteResult.isSuccess))
  }

  test("runSuite - SuiteBefore empty") {
    val suiteResults: List[SuiteResult] = SuiteRunner.runSuites(suitesBundleNoBefore, environment, () => new RestClient(FakeScAPIRequestSender))

    val beforeSuiteResult: Option[SuiteResult] = suiteResults.find(result =>
      result.resultType == SuiteResultType.BEFORE_SUITE && result.suiteName == "endpoint2")

    assertEquals(2, clue(suiteResults.size))
    assert(beforeSuiteResult.isEmpty)
  }

  test("runSuite - SuiteAfter exists") {
    val suiteResults: List[SuiteResult] = SuiteRunner.runSuites(suitesBundles, environment, () => new RestClient(FakeScAPIRequestSender))

    val afterSuiteResult: SuiteResult = suiteResults.find(result =>
      result.resultType == SuiteResultType.AFTER_SUITE && result.suiteName == "endpoint2").get

    assertEquals(5, clue(suiteResults.size))
    assertEquals("test", clue(afterSuiteResult.name))
    assertEquals(true, clue(afterSuiteResult.isSuccess))
  }

  test("runSuite - SuiteAfter empty") {
    val suiteResults: List[SuiteResult] = SuiteRunner.runSuites(suitesBundleNoAfter, environment, () => new RestClient(FakeScAPIRequestSender))

    val afterSuiteResult: Option[SuiteResult] = suiteResults.find(result =>
      result.resultType == SuiteResultType.AFTER_SUITE && result.suiteName == "endpoint2")

    assertEquals(2, clue(suiteResults.size))
    assert(afterSuiteResult.isEmpty)
  }

  test("runSuite - SuiteAfter empty methods") {
    val suiteResults: List[SuiteResult] = SuiteRunner.runSuites(suitesBundleAfterMethodNotSupported, environment, () => new RestClient(FakeScAPIRequestSender))

    val afterSuiteResult: SuiteResult = suiteResults.find(result =>
      result.resultType == SuiteResultType.AFTER_SUITE && result.suiteName == "endpoint2").get

    assertEquals(2, clue(suiteResults.size))
    assertEquals(false, clue(afterSuiteResult.isSuccess))
    assertEquals("RestClient:sendRequest - unexpected action method called", afterSuiteResult.errorMsg.get)
  }
}
