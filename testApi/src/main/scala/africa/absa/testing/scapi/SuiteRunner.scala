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

package africa.absa.testing.scapi

import africa.absa.testing.scapi.model.{Suite, TestResults}
import africa.absa.testing.scapi.json.Environment
import africa.absa.testing.scapi.logging.functions.Scribe
import africa.absa.testing.scapi.rest.RestClient
import africa.absa.testing.scapi.rest.request.sender.ScAPIRequestSender
import africa.absa.testing.scapi.rest.request.{RequestBody, RequestHeaders, RequestParams}
import africa.absa.testing.scapi.rest.response.{Response, ResponseAssertions}

object SuiteRunner {
  def runSuites(suites: Set[Suite], environment: Environment)
               (implicit loggingFunctions: Scribe): Set[TestResults] = {

    suites.flatMap(suite => {
      suite.tests.map(test => {
        loggingFunctions.debug(s"Running Suite: ${suite.endpoint}, Test: ${test.name}")
        val testStartTime: Long = System.currentTimeMillis()

        try {
          val response: Response = new RestClient(ScAPIRequestSender).sendRequest(
            method = test.actions.head.methodName,
            url = test.actions.head.url,
            headers = RequestHeaders.buildHeaders(test.headers),
            body = RequestBody.buildBody(test.actions.head.body),
            params = RequestParams.buildParams(test.actions.head.params),
            verifySslCerts = Some(environment.constants.get("verifySslCerts").exists(_.toLowerCase == "true")).getOrElse(false)
          )

          val isSuccess: Boolean = ResponseAssertions.performAssertions(
            response = response,
            assertions = test.assertions
          )

          val testEndTime: Long = System.currentTimeMillis()
          loggingFunctions.debug(s"Test '${test.name}' finished. Response statusCode is '${response.statusCode}'")
          TestResults.withBooleanStatus(
            suiteName = suite.endpoint,
            testName = test.name,
            isSuccess,
            duration = Some(testEndTime - testStartTime),
            categories = Some(test.categories.mkString(","))
          )

        } catch {
          case e: Exception =>
            val testEndTime = System.currentTimeMillis()
            loggingFunctions.error(s"Request exception occurred while running suite: ${suite.endpoint}, Test: ${test.name}. Exception: ${e.getMessage}")
            TestResults(
              suiteName = suite.endpoint,
              testName = test.name,
              TestResults.Failure,
              errMessage = Some(e.getMessage),
              duration = Some(testEndTime - testStartTime),
              categories = Some(test.categories.mkString(","))
            )
        }
      })
    })
  }
}