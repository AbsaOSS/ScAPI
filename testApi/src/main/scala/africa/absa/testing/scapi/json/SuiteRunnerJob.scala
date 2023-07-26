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

import africa.absa.testing.scapi.data.{SuiteBundle, SuiteResults}
import africa.absa.testing.scapi.logging.LoggerConfig
import africa.absa.testing.scapi.logging.functions.Scribe
import africa.absa.testing.scapi.rest.RestClient
import africa.absa.testing.scapi.rest.request.sender.RealRequestSender
import africa.absa.testing.scapi.rest.request.{RequestBody, RequestHeaders, RequestParams}
import africa.absa.testing.scapi.rest.response.Response
import africa.absa.testing.scapi.utils.cache.RuntimeCache

object SuiteRunnerJob {
  private lazy val loggingFunctions: Scribe = Scribe(this.getClass, LoggerConfig.logLevel)

  def runSuites(suiteBundles: Set[SuiteBundle], environment: Environment): Set[SuiteResults] = {
    suiteBundles.flatMap(suiteBundle => {
      loggingFunctions.debug(s"Running Suite: ${suiteBundle.suite.endpoint}")

      val resultSuiteBefore: Set[SuiteResults] = suiteBundle.suiteBefore.flatMap { suiteBefore =>
        Some(suiteBefore.methods.map { method => runSuiteBefore(suiteBundle.suite.endpoint, suiteBefore.name, method, environment) })
      }.getOrElse(Set.empty)

      var resultSuite: Set[SuiteResults] = Set.empty
      var resultSuiteAfter: Set[SuiteResults] = Set.empty
      if (!resultSuiteBefore.forall(_.isSuccess)) {
        loggingFunctions.error(s"Suite-Before for Suite: ${suiteBundle.suite.endpoint} has failed methods. Not executing main tests and Suite-After.")
      } else {
        resultSuite = suiteBundle.suite.tests.map(test =>
          this.runSuiteTest(suiteBundle.suite.endpoint, test, environment))

        resultSuiteAfter = suiteBundle.suiteAfter.flatMap { suiteAfter =>
          Some(suiteAfter.methods.map { method => runSuiteAfter(suiteBundle.suite.endpoint, suiteAfter.name, method, environment) })
        }.getOrElse(Set.empty)
      }

      RuntimeCache.expire(RuntimeCache.SUITE)
      resultSuiteBefore ++ resultSuite ++ resultSuiteAfter
    })
  }

  private def runSuiteBefore(suiteEndpoint: String, suiteBeforeName: String, method: Method, environment: Environment): SuiteResults = {
    loggingFunctions.debug(s"Running Suite-Before: ${suiteBeforeName}")
    val testStartTime: Long = System.currentTimeMillis()

    try {
      val response: Response = sendRequest(method, environment)
      Response.perform(
        response = response,
        assertions = method.assertions
      )

      val testEndTime: Long = System.currentTimeMillis()
      loggingFunctions.debug(s"Before method '${method.name}' finished. Response statusCode is '${response.statusCode}'")
      SuiteResults.successBefore(
        suiteName = suiteEndpoint,
        methodName = method.name,
        duration = Some(testEndTime - testStartTime)
      )
    } catch {
      case e: Exception => handleException(e, suiteEndpoint, suiteBeforeName, testStartTime, "Before")
    }
  }

  private def runSuiteTest(suiteEndpoint: String, test: SuiteTestScenario, environment: Environment): SuiteResults = {
    loggingFunctions.debug(s"Running Suite-Test: ${test.name}")
    val testStartTime: Long = System.currentTimeMillis()

    try {
      val response: Response = sendRequest(test, environment)
      Response.perform(
        response = response,
        assertions = test.assertions
      )

      val testEndTime: Long = System.currentTimeMillis()
      loggingFunctions.debug(s"Test '${test.name}' finished. Response statusCode is '${response.statusCode}'")
      SuiteResults.successTest(
        suiteName = suiteEndpoint,
        testName = test.name,
        duration = Some(testEndTime - testStartTime),
        category = Some(test.categories.mkString(","))
      )

    } catch {
      case e: Exception => handleException(e, suiteEndpoint, test.name, testStartTime, "Test")
    } finally {
      RuntimeCache.expire(RuntimeCache.TEST)
    }
  }

  private def runSuiteAfter(suiteEndpoint: String, suiteAfterName: String, method: Method, environment: Environment): SuiteResults = {
    loggingFunctions.debug(s"Running Suite-After: ${suiteAfterName}")
    val testStartTime: Long = System.currentTimeMillis()

    try {
      val response: Response = sendRequest(method, environment)
      Response.perform(
        response = response,
        assertions = method.assertions
      )

      val testEndTime: Long = System.currentTimeMillis()
      loggingFunctions.debug(s"After method '${method.name}' finished. Response statusCode is '${response.statusCode}'")
      SuiteResults.successAfter(
        suiteName = suiteEndpoint,
        methodName = method.name,
        duration = Some(testEndTime - testStartTime)
      )
    } catch {
      case e: Exception => handleException(e, suiteEndpoint, suiteAfterName, testStartTime, "After")
    }
  }

  private def sendRequest(requestable: Requestable, environment: Environment): Response = {
    new RestClient(RealRequestSender).sendRequest(
      method = requestable.actions.head.methodName,
      url = RuntimeCache.resolve(requestable.actions.head.url),
      headers = RequestHeaders.buildHeaders(requestable.headers),
      body = RequestBody.buildBody(requestable.actions.head.body),
      params = RequestParams.buildParams(requestable.actions.head.params),
      verifySslCerts = Some(environment.constants.get("verifySslCerts").exists(_.toLowerCase == "true")).getOrElse(false)
    )
  }

  private def handleException(e: Throwable, suiteEndpoint: String, name: String, testStartTime: Long, resultType: String): SuiteResults = {
    val testEndTime = System.currentTimeMillis()
    val message = e match {
      case _: AssertionError => s"Assertion error while running suite: ${suiteEndpoint}, ${resultType}: ${name}. Exception: ${e.getMessage}"
      case _ => s"Request exception occurred while running suite: ${suiteEndpoint}, ${resultType}: ${name}. Exception: ${e.getMessage}"
    }
    loggingFunctions.error(message)
    resultType match {
      case "Before" => SuiteResults.failureBefore(suiteName = suiteEndpoint, methodName = name, errorMessage = Some(e.getMessage), duration = Some(testEndTime - testStartTime))
      case "Test" => SuiteResults.failureTest(suiteName = suiteEndpoint, testName = name, errorMessage = Some(e.getMessage), duration = Some(testEndTime - testStartTime))
      case "After" => SuiteResults.failureAfter(suiteName = suiteEndpoint, methodName = name, errorMessage = Some(e.getMessage), duration = Some(testEndTime - testStartTime))
    }
  }
}
