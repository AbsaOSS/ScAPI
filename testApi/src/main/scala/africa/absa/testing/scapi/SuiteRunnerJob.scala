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

import africa.absa.testing.scapi.logging.functions.Scribe

object SuiteRunnerJob {
  implicit val loggingFunctions: Scribe = Scribe(this.getClass)

  def runSuites(suites: Set[Suite], environment: Environment): Unit = {
    for (suite <- suites;
         test <- suite.tests) {
      loggingFunctions.debug(s"Running Suite: ${suite.endpoint}, Test: ${test.name}")

      // TODO - will be solved in #3
      //  in json there are actions as set - but used is only one to do get, put, post or delete
      //  decide if it is correct in time of designing before/after logic which has same data format - maybe define two formats

      try {
        val response: Response = RestClient.sendRequest(
          method = test.actions.head.name,
          url = test.actions.head.value,
          body = None, // TODO - missing concept how to insert a body
          headers = RequestHeaders.buildHeaders(test.headers),
          verifySslCerts = Some(environment.constants.get("verifySslCerts").exists(_.toLowerCase == "true")).getOrElse(false)
        )

        ResponseAssertions.performAssertions(
          response = response,
          assertions = test.assertions
        )

        loggingFunctions.debug(s"Test '${test.name}' finished. Response statusCode is '${response.status}'")
      } catch {
        case e: Exception =>
          loggingFunctions.error(s"Exception occurred while running suite: ${suite.endpoint}, Test: ${test.name}. Exception: ${e.getMessage}")
      }
    }
  }
}
