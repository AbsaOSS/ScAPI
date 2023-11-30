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

import africa.absa.testing.scapi.SuiteBeforeFailedException
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

      val suiteBeforeResult: List[SuiteResult] = suiteBundle.suiteBefore.toList.flatMap { suiteBefore =>
        suiteBefore.methods.map { method => runSuiteBefore(
          suiteBundle.suite.name,
          suiteBefore.name,
          method,
          environment,
          restClientCreator) }
      }

      var suiteResult: List[SuiteResult] = List.empty
      var suiteAfterResult: List[SuiteResult] = List.empty
      if (!suiteBeforeResult.forall(_.isSuccess)) {
        val errorMsg =  s"Suite-Before for Suite: ${suiteBundle.suite.name} has failed methods. Not executing main tests and Suite-After."
        Logger.error(errorMsg)

        // add failed Test suite result instance and it will not be started
        suiteResult = suiteResult :+ SuiteResult(
          resultType = SuiteResultType.TestSet,
          suiteName = suiteBundle.suite.name,
          name = "SKIPPED",
          result = Failure(SuiteBeforeFailedException(errorMsg)),
          duration = Some(0L),
          categories = Some("SKIPPED"))
      } else {
        suiteResult = suiteBundle.suite.tests.toList.map(test =>
          this.runSuiteTest(
            suiteBundle.suite.name,
            test,
            environment,
            restClientCreator))

        suiteAfterResult = suiteBundle.suiteAfter.toList.flatMap { suiteAfter =>
          suiteAfter.methods.map { method => runSuiteAfter(
            suiteBundle.suite.name,
            suiteAfter.name,
            method,
            environment,
            restClientCreator) }
        }
      }

      RuntimeCache.expire(SuiteLevel)
      resultList ++ suiteBeforeResult ++ suiteResult ++ suiteAfterResult
    }
  }

  /**
   * Runs all the suite-before methods for a given test suite.
   *
   * @param suiteName       Suite's name.
   * @param suiteBeforeName SuiteBefore's name.
   * @param method          Method to execute.
   * @param environment     The current environment.
   * @return SuiteResults after the execution of the suite-before method.
   */
  private def runSuiteBefore(suiteName: String, suiteBeforeName: String, method: Method, environment: Environment, restClientCreator: RestClientCreator): SuiteResult = {
    Logger.debug(s"Suite-Before: $suiteBeforeName - Started")
    val testStartTime: Long = System.currentTimeMillis()

    try {
      val result: Try[Unit] = processRequest(method, environment, restClientCreator)
      val testEndTime: Long = System.currentTimeMillis()
      Logger.debug(s"Suite-Before: method '${method.name}' - ${if (result.isSuccess) "completed successfully" else "failed"}.")
      SuiteResult(
        resultType = SuiteResultType.BeforeTestSet,
        suiteName = suiteName,
        name = method.name,
        result = result,
        duration = Some(testEndTime - testStartTime)
      )
    } catch {
      case e: Exception => handleException(e, suiteName, suiteBeforeName, testStartTime, SuiteResultType.BeforeTestSet)
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
        resultType = SuiteResultType.TestSet,
        suiteName = suiteName,
        name = test.name,
        result = result,
        duration = Some(testEndTime - testStartTime),
        categories = Some(test.categories.mkString(","))
      )

    } catch {
      case e: Exception => handleException(e, suiteName, test.name, testStartTime, SuiteResultType.TestSet, Some(test.categories.mkString(",")))
    } finally {
      RuntimeCache.expire(TestLevel)
    }
  }

  /**
   * Runs all the suite-after methods for a given test suite.
   *
   * @param suiteName      Suite's name.
   * @param suiteAfterName SuiteAfter's name.
   * @param method         Method to execute.
   * @param environment    The current environment.
   * @return SuiteResults after the execution of the suite-after method.
   */
  private def runSuiteAfter(suiteName: String, suiteAfterName: String, method: Method, environment: Environment, restClientCreator: RestClientCreator): SuiteResult = {
    Logger.debug(s"Suite-After: $suiteAfterName - Started")
    val testStartTime: Long = System.currentTimeMillis()

    try {
      val result: Try[Unit] = processRequest(method, environment, restClientCreator)
      val testEndTime: Long = System.currentTimeMillis()
      Logger.debug(s"After method '${method.name}' ${if (result.isSuccess) "completed successfully" else "failed"}.")
      SuiteResult(
        resultType = SuiteResultType.AfterTestSet,
        suiteName = suiteName,
        name = method.name,
        result = result,
        duration = Some(testEndTime - testStartTime)
      )
    } catch {
      case e: Exception => handleException(e, suiteName, suiteAfterName, testStartTime, SuiteResultType.AfterTestSet)
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
