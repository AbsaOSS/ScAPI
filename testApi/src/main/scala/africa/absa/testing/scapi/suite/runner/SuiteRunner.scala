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

import africa.absa.testing.scapi.json.{Environment, Requestable}
import africa.absa.testing.scapi.logging.Logger
import africa.absa.testing.scapi.model.{Method, SuiteBundle, SuiteResults, SuiteTestScenario}
import africa.absa.testing.scapi.rest.RestClient
import africa.absa.testing.scapi.rest.request.{RequestBody, RequestHeaders, RequestParams}
import africa.absa.testing.scapi.rest.response.Response
import africa.absa.testing.scapi.utils.cache.{RuntimeCache, SuiteLevel, TestLevel}

/**
 * Main object handling the running of test suites.
 */
object SuiteRunner {
  type RestClientCreator = () => RestClient

  /**
   * Run a set of test suites.
   *
   * @param suiteBundles Set of suites to run.
   * @param environment  The current environment.
   * @return Set of SuiteResults.
   */
  def runSuites(suiteBundles: Set[SuiteBundle], environment: Environment, restClientCreator: RestClientCreator): Set[SuiteResults] = {
    suiteBundles.flatMap(suiteBundle => {
      Logger.debug(s"Running Suite: ${suiteBundle.suite.endpoint}")

      val resultSuiteBefore: Set[SuiteResults] = suiteBundle.suiteBefore.map { suiteBefore =>
        suiteBefore.methods.map { method => runSuiteBefore(suiteBundle.suite.endpoint, suiteBefore.name, method, environment, restClientCreator) }
      }.getOrElse(Set.empty)

      var resultSuite: Set[SuiteResults] = Set.empty
      var resultSuiteAfter: Set[SuiteResults] = Set.empty
      if (!resultSuiteBefore.forall(_.isSuccess)) {
        Logger.error(s"Suite-Before for Suite: ${suiteBundle.suite.endpoint} has failed methods. Not executing main tests and Suite-After.")
      } else {
        resultSuite = suiteBundle.suite.tests.map(test =>
          this.runSuiteTest(suiteBundle.suite.endpoint, test, environment, restClientCreator))

        resultSuiteAfter = suiteBundle.suiteAfter.map { suiteAfter =>
          suiteAfter.methods.map { method => runSuiteAfter(suiteBundle.suite.endpoint, suiteAfter.name, method, environment, restClientCreator) }
        }.getOrElse(Set.empty)
      }

      RuntimeCache.expire(SuiteLevel)
      resultSuiteBefore ++ resultSuite ++ resultSuiteAfter
    })
  }

  /**
   * Runs all the suite-before methods for a given test suite.
   *
   * @param suiteEndpoint   Suite's endpoint.
   * @param suiteBeforeName SuiteBefore's name.
   * @param method          Method to execute.
   * @param environment     The current environment.
   * @return SuiteResults after the execution of the suite-before method.
   */
  private def runSuiteBefore(suiteEndpoint: String, suiteBeforeName: String, method: Method, environment: Environment, restClientCreator: RestClientCreator): SuiteResults = {
    Logger.debug(s"Running Suite-Before: ${suiteBeforeName}")
    val testStartTime: Long = System.currentTimeMillis()

    try {
      val response: Response = sendRequest(method, environment, restClientCreator)
      val isSuccess: Boolean = Response.perform(
        response = response,
        assertions = method.assertions
      )

      val testEndTime: Long = System.currentTimeMillis()
      Logger.debug(s"Before method '${method.name}' finished. Response statusCode is '${response.statusCode}'")
      SuiteResults.withBooleanStatus(
        resultType = SuiteResults.RESULT_TYPE_BEFORE_METHOD,
        suiteName = suiteEndpoint,
        name = method.name,
        status = isSuccess,
        duration = Some(testEndTime - testStartTime)
      )
    } catch {
      case e: Exception => handleException(e, suiteEndpoint, suiteBeforeName, testStartTime, "Before")
    }
  }

  /**
   * Runs all the suite-tests methods for a given test suite.
   *
   * @param suiteEndpoint Suite's endpoint.
   * @param test          The test to run.
   * @param environment   The current environment.
   * @return SuiteResults after the execution of the suite-test.
   */
  private def runSuiteTest(suiteEndpoint: String, test: SuiteTestScenario, environment: Environment, restClientCreator: RestClientCreator): SuiteResults = {
    Logger.debug(s"Running Suite-Test: ${test.name}")
    val testStartTime: Long = System.currentTimeMillis()

    try {
      val response: Response = sendRequest(test, environment, restClientCreator)
      val isSuccess: Boolean = Response.perform(
        response = response,
        assertions = test.assertions
      )

      val testEndTime: Long = System.currentTimeMillis()
      Logger.debug(s"Test '${test.name}' finished. Response statusCode is '${response.statusCode}'")
      SuiteResults.withBooleanStatus(
        resultType = SuiteResults.RESULT_TYPE_TEST,
        suiteName = suiteEndpoint,
        name = test.name,
        status = isSuccess,
        duration = Some(testEndTime - testStartTime),
        categories = Some(test.categories.mkString(","))
      )

    } catch {
      case e: Exception => handleException(e, suiteEndpoint, test.name, testStartTime, "Test", Some(test.categories.mkString(",")))
    } finally {
      RuntimeCache.expire(TestLevel)
    }
  }

  /**
   * Runs all the suite-after methods for a given test suite.
   *
   * @param suiteEndpoint  Suite's endpoint.
   * @param suiteAfterName SuiteAfter's name.
   * @param method         Method to execute.
   * @param environment    The current environment.
   * @return SuiteResults after the execution of the suite-after method.
   */
  private def runSuiteAfter(suiteEndpoint: String, suiteAfterName: String, method: Method, environment: Environment, restClientCreator: RestClientCreator): SuiteResults = {
    Logger.debug(s"Running Suite-After: ${suiteAfterName}")
    val testStartTime: Long = System.currentTimeMillis()

    try {
      val response: Response = sendRequest(method, environment, restClientCreator)
      val isSuccess: Boolean = Response.perform(
        response = response,
        assertions = method.assertions
      )

      val testEndTime: Long = System.currentTimeMillis()
      Logger.debug(s"After method '${method.name}' finished. Response statusCode is '${response.statusCode}'")
      SuiteResults.withBooleanStatus(
        resultType = SuiteResults.RESULT_TYPE_AFTER_METHOD,
        suiteName = suiteEndpoint,
        name = method.name,
        status = isSuccess,
        duration = Some(testEndTime - testStartTime)
      )
    } catch {
      case e: Exception => handleException(e, suiteEndpoint, suiteAfterName, testStartTime, "After")
    }
  }

  /**
   * Send a request and return the response.
   *
   * @param requestable The requestable object containing the data for the request.
   * @param environment The current environment.
   * @return Response to the sent request.
   */
  private def sendRequest(requestable: Requestable, environment: Environment, restClientCreator: RestClientCreator): Response = {
    restClientCreator().sendRequest(
      method = requestable.actions.head.methodName,
      url = RuntimeCache.resolve(requestable.actions.head.url),
      headers = RequestHeaders.buildHeaders(requestable.headers),
      body = RequestBody.buildBody(requestable.actions.head.body),
      params = RequestParams.buildParams(requestable.actions.head.params),
      verifySslCerts = Some(environment.constants.get("verifySslCerts").exists(_.toLowerCase == "true")).getOrElse(false)
    )
  }

  /**
   * Handles exceptions occurring during suite running.
   *
   * @param e             The exception to handle.
   * @param suiteEndpoint Suite's endpoint.
   * @param name          The name of the suite or test.
   * @param testStartTime The starting time of the suite or test.
   * @param resultType    The type of the suite or test ("Before", "Test", or "After").
   * @return SuiteResults after the exception handling.
   */
  private def handleException(e: Throwable, suiteEndpoint: String, name: String, testStartTime: Long, resultType: String, categories: Option[String] = None): SuiteResults = {
    val testEndTime = System.currentTimeMillis()
    val message = e match {
      case _ => s"Request exception occurred while running suite: ${suiteEndpoint}, ${resultType}: ${name}. Exception: ${e.getMessage}"
    }
    Logger.error(message)
    resultType match {
      case "Before" => SuiteResults.withBooleanStatus(
        resultType = SuiteResults.RESULT_TYPE_BEFORE_METHOD,
        suiteName = suiteEndpoint,
        name = name,
        status = false,
        errMessage = Some(e.getMessage),
        duration = Some(testEndTime - testStartTime))

      case "Test" => SuiteResults.withBooleanStatus(
        resultType = SuiteResults.RESULT_TYPE_TEST,
        suiteName = suiteEndpoint,
        name = name,
        status = false,
        errMessage = Some(e.getMessage),
        duration = Some(testEndTime - testStartTime),
        categories = categories
      )

      case "After" => SuiteResults.withBooleanStatus(
        resultType = SuiteResults.RESULT_TYPE_AFTER_METHOD,
        suiteName = suiteEndpoint,
        name = name,
        status = false,
        errMessage = Some(e.getMessage),
        duration = Some(testEndTime - testStartTime))
    }
  }
}
