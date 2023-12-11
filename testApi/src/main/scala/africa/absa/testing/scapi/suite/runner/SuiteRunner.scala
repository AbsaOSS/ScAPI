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

import africa.absa.testing.scapi.BeforeSuiteFailedException
import africa.absa.testing.scapi.json.{Environment, Requestable}
import africa.absa.testing.scapi.logging.Logger
import africa.absa.testing.scapi.model.suite.types.SuiteResultType
import africa.absa.testing.scapi.model.suite.types.SuiteResultType.SuiteResultType
import africa.absa.testing.scapi.model.suite.{Method, Suite, SuiteResult, SuiteTestScenario}
import africa.absa.testing.scapi.rest.RestClient
import africa.absa.testing.scapi.rest.request.{RequestBody, RequestHeaders, RequestParams}
import africa.absa.testing.scapi.rest.response.Response
import africa.absa.testing.scapi.utils.cache.{RuntimeCache, SuiteLevel, TestLevel}

import scala.util.{Failure, Try}

/**
 * Main object handling the running of test suites.
 */
object SuiteRunner {
  private type RestClientCreator = () => RestClient

  /**
   * Run a set of test suites.
   *
   * @param suiteBundles Set of suites to run.
   * @param environment  The current environment.
   * @return Set of SuiteResults.
   */
  def runSuites(suiteBundles: Set[Suite], environment: Environment, restClientCreator: RestClientCreator): List[SuiteResult] = {
    suiteBundles.foldLeft(List[SuiteResult]()) { (resultList, suiteBundle) =>
      Logger.debug(s"Suite: ${suiteBundle.suite.name} - Started")

      val beforeSuiteResult: List[SuiteResult] = suiteBundle.beforeSuite.toList.flatMap { beforeSuite =>
        beforeSuite.methods.map { method => runBeforeSuite(
          suiteBundle.suite.name,
          beforeSuite.name,
          method,
          environment,
          restClientCreator) }
      }

      var suiteResult: List[SuiteResult] = List.empty
      var afterSuiteResult: List[SuiteResult] = List.empty
      if (!beforeSuiteResult.forall(_.isSuccess)) {
        val errorMsg =  s"BeforeSuite for Suite: ${suiteBundle.suite.name} has failed methods. Not executing main tests and After-Suite."
        Logger.error(errorMsg)

        // add failed Test suite result instance and it will not be started
        suiteResult = suiteResult :+ SuiteResult(
          resultType = SuiteResultType.TestResult,
          suiteName = suiteBundle.suite.name,
          name = "SKIPPED",
          result = Failure(BeforeSuiteFailedException(errorMsg)),
          duration = Some(0L),
          categories = Some("SKIPPED"))
      } else {
        suiteResult = suiteBundle.suite.tests.toList.map(test =>
          this.runSuiteTest(
            suiteBundle.suite.name,
            test,
            environment,
            restClientCreator))

        afterSuiteResult = suiteBundle.afterSuite.toList.flatMap { afterSuite =>
          afterSuite.methods.map { method => runAfterSuite(
            suiteBundle.suite.name,
            afterSuite.name,
            method,
            environment,
            restClientCreator) }
        }
      }

      RuntimeCache.expire(SuiteLevel)
      resultList ++ beforeSuiteResult ++ suiteResult ++ afterSuiteResult
    }
  }

  /**
   * Runs all the before-suite methods for a given test suite.
   *
   * @param suiteName       Suite's name.
   * @param beforeSuiteName BeforeSuite's name.
   * @param method          Method to execute.
   * @param environment     The current environment.
   * @return SuiteResults after the execution of the before-suite method.
   */
  private def runBeforeSuite(suiteName: String, beforeSuiteName: String, method: Method, environment: Environment, restClientCreator: RestClientCreator): SuiteResult = {
    Logger.debug(s"Before-Suite: $beforeSuiteName - Started")
    val testStartTime: Long = System.currentTimeMillis()

    try {
      val result: Try[Unit] = processRequest(method, environment, restClientCreator)
      val testEndTime: Long = System.currentTimeMillis()
      Logger.debug(s"Before-Suite: method '${method.name}' - ${if (result.isSuccess) "completed successfully" else "failed"}.")
      SuiteResult(
        resultType = SuiteResultType.BeforeSuiteResult,
        suiteName = suiteName,
        name = method.name,
        result = result,
        duration = Some(testEndTime - testStartTime)
      )
    } catch {
      case e: Exception => handleException(e, suiteName, beforeSuiteName, testStartTime, SuiteResultType.BeforeSuiteResult)
    }
  }

  /**
   * Runs all the suite-tests methods for a given test suite.
   *
   * @param suiteName     Suite's name.
   * @param test          The test to run.
   * @param environment   The current environment.
   * @return SuiteResults after the execution of the suite-test.
   */
  private def runSuiteTest(suiteName: String, test: SuiteTestScenario, environment: Environment, restClientCreator: RestClientCreator): SuiteResult = {
    Logger.debug(s"Suite-Test: ${test.name} - Started")
    val testStartTime: Long = System.currentTimeMillis()

    try {
      val result: Try[Unit] = processRequest(test, environment, restClientCreator)
      val testEndTime: Long = System.currentTimeMillis()
      Logger.debug(s"Suite-Test: '${test.name}' - ${if (result.isSuccess) "completed successfully" else "failed"}.")
      SuiteResult(
        resultType = SuiteResultType.TestResult,
        suiteName = suiteName,
        name = test.name,
        result = result,
        duration = Some(testEndTime - testStartTime),
        categories = Some(test.categories.mkString(","))
      )

    } catch {
      case e: Exception => handleException(e, suiteName, test.name, testStartTime, SuiteResultType.TestResult, Some(test.categories.mkString(",")))
    } finally {
      RuntimeCache.expire(TestLevel)
    }
  }

  /**
   * Runs all the after-suite methods for a given test suite.
   *
   * @param suiteName      Suite's name.
   * @param afterSuiteName AfterSuite's name.
   * @param method         Method to execute.
   * @param environment    The current environment.
   * @return SuiteResults after the execution of the after-suite method.
   */
  private def runAfterSuite(suiteName: String, afterSuiteName: String, method: Method, environment: Environment, restClientCreator: RestClientCreator): SuiteResult = {
    Logger.debug(s"After-Suite: $afterSuiteName - Started")
    val testStartTime: Long = System.currentTimeMillis()

    try {
      val result: Try[Unit] = processRequest(method, environment, restClientCreator)
      val testEndTime: Long = System.currentTimeMillis()
      Logger.debug(s"After method '${method.name}' ${if (result.isSuccess) "completed successfully" else "failed"}.")
      SuiteResult(
        resultType = SuiteResultType.AfterSuiteResult,
        suiteName = suiteName,
        name = method.name,
        result = result,
        duration = Some(testEndTime - testStartTime)
      )
    } catch {
      case e: Exception => handleException(e, suiteName, afterSuiteName, testStartTime, SuiteResultType.AfterSuiteResult)
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
    val resolvedAction = requestable.action.resolveByRuntimeCache()

    restClientCreator().sendRequest(
      method = resolvedAction.method,
      url = RuntimeCache.resolve(resolvedAction.url),
      headers = RequestHeaders.buildHeaders(requestable.headers.map(header => header.resolveByRuntimeCache())),
      body = RequestBody.buildBody(resolvedAction.body),
      params = RequestParams.buildParams(resolvedAction.params),
      verifySslCerts = Some(environment.constants.get("verifySslCerts").exists(_.toLowerCase == "true")).getOrElse(false)
    )
  }

  /**
   * Process the request and perform the associated response actions.
   *
   * @param requestable       The request-able method containing the actions and response actions.
   * @param environment       The current environment.
   * @param restClientCreator A creator function for the REST client.
   * @return A Try containing the result of the response actions.
   */
  private def processRequest(requestable: Requestable, environment: Environment, restClientCreator: RestClientCreator): Try[Unit] = {
    val response: Response = sendRequest(requestable, environment, restClientCreator)
    Response.perform(
      response = response,
      responseActions = requestable.responseActions
    )
  }

  /**
   * Handles exceptions occurring during suite running.
   *
   * @param e               The exception to handle.
   * @param suiteName       Suite's name.
   * @param name            The name of the suite or test.
   * @param testStartTime   The starting time of the suite or test.
   * @param suiteResultType The type of the suite or test ("Before", "Test", or "After").
   * @return SuiteResults after the exception handling.
   */
  private def handleException(e: Throwable, suiteName: String, name: String, testStartTime: Long, suiteResultType: SuiteResultType, categories: Option[String] = None): SuiteResult = {
    val testEndTime = System.currentTimeMillis()
    Logger.error(s"Request exception occurred while running suite: $suiteName, $suiteResultType: $name. Exception: ${e.getMessage}")

    SuiteResult(
      resultType = suiteResultType,
      suiteName = suiteName,
      name = name,
      result = Failure(e),
      duration = Some(testEndTime - testStartTime))
  }
}
