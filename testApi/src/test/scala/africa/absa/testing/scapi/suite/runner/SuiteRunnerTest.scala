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
import africa.absa.testing.scapi.rest.response.{AssertionResponseAction, Response}
import africa.absa.testing.scapi.utils.cache.RuntimeCache
import munit.FunSuite
import org.apache.logging.log4j.Level

class SuiteRunnerTest extends FunSuite {

  val header: Header = Header(name = RequestHeaders.AUTHORIZATION, value = "Basic abcdefg")
  val action: Action = Action(methodName = "get", url = "nice url")
  val actionNotSupported: Action = Action(methodName = "wrong", url = "nice url")
  val responseAction: ResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_EQUALS}", Map("code" -> "200"))
  val method: Method = Method(name = "test", headers = Set(header), actions = Set(action), responseActions = Set(responseAction))
  val methodNotSupported: Method = Method(name = "test", headers = Set(header), actions = Set(actionNotSupported), responseActions = Set(responseAction))

  val suitesBundles: Set[SuiteBundle] = Set(
    SuiteBundle(suite = Suite(endpoint = "endpoint1", tests = Set(
      SuiteTestScenario(name = "test1", categories = Set("SMOKE"),
        headers = Set(header), actions = Set(action), responseActions = Set(responseAction), only = Some(true)),
      SuiteTestScenario(name = "test2", categories = Set("SMOKE"),
        headers = Set(header), actions = Set(action), responseActions = Set(responseAction), only = Some(true))
    ))),
    SuiteBundle(
      suiteBefore = Some(SuiteBefore(name = "suiteBefore", methods = Set(method))),
      suite = Suite(endpoint = "endpoint2", tests = Set(
        SuiteTestScenario(name = "test1", categories = Set("SMOKE"),
          headers = Set(header), actions = Set(action), responseActions = Set(responseAction), only = Some(false)),
    )),
      suiteAfter = Some(SuiteAfter(name = "suiteAfter", methods = Set(method))),
    ))

  val suitesBundleNoBefore: Set[SuiteBundle] = Set(
    SuiteBundle(
      suite = Suite(endpoint = "endpoint2", tests = Set(
        SuiteTestScenario(name = "test1", categories = Set("SMOKE"),
          headers = Set(header), actions = Set(action), responseActions = Set(responseAction), only = Some(false)),
    )),
      suiteAfter = Some(SuiteAfter(name = "suiteAfter", methods = Set(method))),
    ))

  val suitesBundleAfterMethodNotSupported: Set[SuiteBundle] = Set(
    SuiteBundle(
      suite = Suite(endpoint = "endpoint2", tests = Set(
        SuiteTestScenario(name = "test1", categories = Set("SMOKE"),
          headers = Set(header), actions = Set(action), responseActions = Set(responseAction), only = Some(false)),
    )),
      suiteAfter = Some(SuiteAfter(name = "suiteAfter", methods = Set(methodNotSupported))),
    ))

  val suitesBundleNoAfter: Set[SuiteBundle] = Set(
    SuiteBundle(
      suiteBefore = Some(SuiteBefore(name = "suiteBefore", methods = Set(method))),
      suite = Suite(endpoint = "endpoint2", tests = Set(
        SuiteTestScenario(name = "test1", categories = Set("SMOKE"),
          headers = Set(header), actions = Set(action), responseActions = Set(responseAction), only = Some(false)),
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
    val suiteResults: List[SuiteResults] = SuiteRunner.runSuites(suitesBundles, environment, () => new RestClient(FakeScAPIRequestSender))

    val beforeSuiteResult: SuiteResults = suiteResults.find(result =>
      result.resultType == SuiteResults.RESULT_TYPE_BEFORE_METHOD && result.suiteName == "endpoint2").get

    assertEquals(5, suiteResults.size)
    assertEquals("test", beforeSuiteResult.name)
    assertEquals("Success", beforeSuiteResult.status)
  }

  test("runSuite - SuiteBefore empty") {
    val suiteResults: List[SuiteResults] = SuiteRunner.runSuites(suitesBundleNoBefore, environment, () => new RestClient(FakeScAPIRequestSender))

    val beforeSuiteResult: Option[SuiteResults] = suiteResults.find(result =>
      result.resultType == SuiteResults.RESULT_TYPE_BEFORE_METHOD && result.suiteName == "endpoint2")

    assertEquals(2, suiteResults.size)
    assert(beforeSuiteResult.isEmpty)
  }

  test("runSuite - SuiteAfter exists") {
    val suiteResults: List[SuiteResults] = SuiteRunner.runSuites(suitesBundles, environment, () => new RestClient(FakeScAPIRequestSender))

    val afterSuiteResult: SuiteResults = suiteResults.find(result =>
      result.resultType == SuiteResults.RESULT_TYPE_AFTER_METHOD && result.suiteName == "endpoint2").get

    assertEquals(5, suiteResults.size)
    assertEquals("test", afterSuiteResult.name)
    assertEquals("Success", afterSuiteResult.status)
  }

  test("runSuite - SuiteAfter empty") {
    val suiteResults: List[SuiteResults] = SuiteRunner.runSuites(suitesBundleNoAfter, environment, () => new RestClient(FakeScAPIRequestSender))

    val afterSuiteResult: Option[SuiteResults] = suiteResults.find(result =>
      result.resultType == SuiteResults.RESULT_TYPE_AFTER_METHOD && result.suiteName == "endpoint2")

    assertEquals(2, suiteResults.size)
    assert(afterSuiteResult.isEmpty)
  }

  test("runSuite - SuiteAfter empty methods") {
    val suiteResults: List[SuiteResults] = SuiteRunner.runSuites(suitesBundleAfterMethodNotSupported, environment, () => new RestClient(FakeScAPIRequestSender))

    val afterSuiteResult: SuiteResults = suiteResults.find(result =>
      result.resultType == SuiteResults.RESULT_TYPE_AFTER_METHOD && result.suiteName == "endpoint2").get

    assertEquals(2, suiteResults.size)
    assertEquals("Failure", afterSuiteResult.status)
    assertEquals("RestClient:sendRequest - unexpected action method called", afterSuiteResult.errMessage.get)
  }
}
