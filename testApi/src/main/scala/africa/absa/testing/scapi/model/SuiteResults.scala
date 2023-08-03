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

package africa.absa.testing.scapi.model

/**
 * Case class representing the results of a suite test.
 *
 * @param resultType   The type of the result (e.g. Before, Test, After)
 * @param suiteName     The name of the suite
 * @param name          The name of the test or method
 * @param status        The status of the result (e.g. Success, Failure)
 * @param duration      The duration of the test, if applicable
 * @param errMessage    The error message, if any
 * @param categories    The categories of the test, if any
 */
case class SuiteResults(resultType: String,
                        suiteName: String,
                        name: String,
                        status: String,
                        duration: Option[Long],
                        errMessage: Option[String] = None,
                        categories: Option[String] = None) {
  /**
   * Checks if the suite result was a success.
   *
   * @return true if the suite result was a success, false otherwise
   */
  def isSuccess: Boolean = this.status == SuiteResults.Success
}

object SuiteResults {
  val Success: String = "Success"
  val Failure: String = "Failure"

  val RESULT_TYPE_BEFORE_METHOD = "Before"
  val RESULT_TYPE_TEST = "Test"
  val RESULT_TYPE_AFTER_METHOD = "After"

  /**
   * Creates and returns a `SuiteResults` object for a method setup operation. This is typically used for storing test setup operation results.
   *
   * @param resultType  Type of result.
   * @param suiteName   Name of the suite that this setup operation belongs to.
   * @param name        Name of the setup operation.
   * @param status      Boolean indicating the success (true) or failure (false) of the setup operation.
   * @param duration    Optional duration of the setup operation in milliseconds. None if not available.
   * @param errMessage  Optional error message if the setup operation failed. None if not available.
   * @param categories  Optional string representing categories the setup operation belongs to. None if not available.
   * @return            A `SuiteResults` object containing the provided details of the setup operation.
   */
  def withBooleanStatus(resultType: String,
                       suiteName: String,
                       name: String,
                       status: Boolean,
                       duration: Option[Long],
                       errMessage: Option[String] = None,
                       categories: Option[String] = None): SuiteResults = {
    SuiteResults(resultType,
      suiteName,
      name,
      if (status) Success else Failure,
      duration,
      errMessage,
      categories
    )
  }
}
