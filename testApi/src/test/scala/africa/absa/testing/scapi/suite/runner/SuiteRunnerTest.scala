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
import africa.absa.testing.scapi.model.suite._
import africa.absa.testing.scapi.model.suite.types.SuiteResultType
import africa.absa.testing.scapi.rest.RestClient
import africa.absa.testing.scapi.rest.request.RequestHeaders
import africa.absa.testing.scapi.rest.response.action.types.{AssertResponseActionType, ResponseActionGroupType}
import africa.absa.testing.scapi.utils.cache.RuntimeCache
import munit.FunSuite
import org.apache.logging.log4j.Level

class SuiteRunnerTest extends FunSuite {

  val header: Header = Header(name = RequestHeaders.AUTHORIZATION, value = "Basic abcdefg")
  val action: Action = Action(method = "get", url = "nice url")
  val actionNotSupported: Action = Action(method = "wrong", url = "nice url")
  val responseAction: ResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.StatusCodeEquals.toString, Map("code" -> "200"))
  val method: Method = Method(name = "test", headers = Seq(header), action = action, responseActions = Seq(responseAction))
  val methodNotSupported: Method = Method(name = "test", headers = Seq(header), action = actionNotSupported, responseActions = Seq(responseAction))

  val suitesBundles: Set[Suite] = Set(
    Suite(suite = TestSet(name = "name1", tests = Set(
      SuiteTestScenario(name = "test1", categories = Seq("SMOKE"),
        headers = Seq(header), action = action, responseActions = Seq(responseAction), only = Some(true)),
      SuiteTestScenario(name = "test2", categories = Seq("SMOKE"),
        headers = Seq(header), action = action, responseActions = Seq(responseAction), only = Some(true))
    ))),
    Suite(
      beforeSuite = Some(BeforeSuiteSet(name = "beforeSuite", methods = Set(method))),
      suite = TestSet(name = "name2", tests = Set(
        SuiteTestScenario(name = "test1", categories = Seq("SMOKE"),
          headers = Seq(header), action = action, responseActions = Seq(responseAction), only = Some(false)),
    )),
      afterSuite = Some(AfterSuiteSet(name = "afterSuite", methods = Set(method))),
    ))

  val suitesBundleNoBefore: Set[Suite] = Set(
    Suite(
      suite = TestSet(name = "name2", tests = Set(
        SuiteTestScenario(name = "test1", categories = Seq("SMOKE"),
          headers = Seq(header), action = action, responseActions = Seq(responseAction), only = Some(false)),
    )),
      afterSuite = Some(AfterSuiteSet(name = "afterSuite", methods = Set(method))),
    ))

  val suitesBundleAfterMethodNotSupported: Set[Suite] = Set(
    Suite(
      suite = TestSet(name = "name2", tests = Set(
        SuiteTestScenario(name = "test1", categories = Seq("SMOKE"),
          headers = Seq(header), action = action, responseActions = Seq(responseAction), only = Some(false)),
    )),
      afterSuite = Some(AfterSuiteSet(name = "afterSuite", methods = Set(methodNotSupported))),
    ))

  val suitesBundleNoAfter: Set[Suite] = Set(
    Suite(
      beforeSuite = Some(BeforeSuiteSet(name = "beforeSuite", methods = Set(method))),
      suite = TestSet(name = "name2", tests = Set(
        SuiteTestScenario(name = "test1", categories = Seq("SMOKE"),
          headers = Seq(header), action = action, responseActions = Seq(responseAction), only = Some(false)),
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

  test("runSuite - BeforeSuite exists") {
    val suiteResults: List[SuiteResult] = SuiteRunner.runSuites(suitesBundles, environment, () => new RestClient(FakeScAPIRequestSender))

    val beforeSuiteResult: SuiteResult = suiteResults.find(result =>
      result.resultType == SuiteResultType.BeforeSuiteResult && result.suiteName == "name2").get

    assert(5 == clue(suiteResults.size))
    assert("test" == clue(beforeSuiteResult.name))
    assert(clue(beforeSuiteResult.isSuccess))
  }

  test("runSuite - BeforeSuite empty") {
    val suiteResults: List[SuiteResult] = SuiteRunner.runSuites(suitesBundleNoBefore, environment, () => new RestClient(FakeScAPIRequestSender))

    val beforeSuiteResult: Option[SuiteResult] = suiteResults.find(result =>
      result.resultType == SuiteResultType.BeforeSuiteResult && result.suiteName == "name2")

    assert(2 == clue(suiteResults.size))
    assert(beforeSuiteResult.isEmpty)
  }

  test("runSuite - AfterSuite exists") {
    val suiteResults: List[SuiteResult] = SuiteRunner.runSuites(suitesBundles, environment, () => new RestClient(FakeScAPIRequestSender))

    val afterSuiteResult: SuiteResult = suiteResults.find(result =>
      result.resultType == SuiteResultType.AfterSuiteResult && result.suiteName == "name2").get

    assert(5 == clue(suiteResults.size))
    assert("test" == clue(afterSuiteResult.name))
    assert(clue(afterSuiteResult.isSuccess))
  }

  test("runSuite - AfterSuite empty") {
    val suiteResults: List[SuiteResult] = SuiteRunner.runSuites(suitesBundleNoAfter, environment, () => new RestClient(FakeScAPIRequestSender))

    val afterSuiteResult: Option[SuiteResult] = suiteResults.find(result =>
      result.resultType == SuiteResultType.AfterSuiteResult && result.suiteName == "name2")

    assert(2 == clue(suiteResults.size))
    assert(afterSuiteResult.isEmpty)
  }

  test("runSuite - AfterSuite empty methods") {
    val suiteResults: List[SuiteResult] = SuiteRunner.runSuites(suitesBundleAfterMethodNotSupported, environment, () => new RestClient(FakeScAPIRequestSender))

    val afterSuiteResult: SuiteResult = suiteResults.find(result =>
      result.resultType == SuiteResultType.AfterSuiteResult && result.suiteName == "name2").get

    assert(2 == clue(suiteResults.size))
    assert(clue(!afterSuiteResult.isSuccess))
    assert("RestClient:sendRequest - unexpected action method called" == afterSuiteResult.errorMsg.get)
  }
}
