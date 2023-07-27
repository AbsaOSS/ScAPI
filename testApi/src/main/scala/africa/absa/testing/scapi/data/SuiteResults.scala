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

package africa.absa.testing.scapi.data

/**
 * Case class representing the results of a suite test.
 *
 * @param result_type   The type of the result (e.g. Before, Test, After)
 * @param suite         The name of the suite
 * @param name          The name of the test or method
 * @param status        The status of the result (e.g. Success, Failure)
 * @param duration      The duration of the test, if applicable
 * @param errMessage    The error message, if any
 * @param categories    The categories of the test, if any
 */
case class SuiteResults(result_type: String, suite: String, name: String, status: String, duration: Option[Long], errMessage: Option[String] = None, categories: Option[String] = None) {
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

  private val RESULT_TYPE_BEFORE_METHOD = "Before"
  private val RESULT_TYPE_TEST = "Test"
  private val RESULT_TYPE_AFTER_METHOD = "After"

  /**
   * Factory method for creating a successful test result.
   *
   * @param suiteName The name of the suite
   * @param testName  The name of the test
   * @param duration  The duration of the test
   * @param category  The category of the test, if any
   * @return A new SuiteResults object representing a successful test result
   */
  def successTest(suiteName: String, testName: String, duration: Option[Long], category: Option[String] = None): SuiteResults =
    SuiteResults(RESULT_TYPE_TEST, suite = suiteName, name = testName, status = Success, duration = duration, categories = category)

  /**
   * Factory method for creating a failed test result.
   *
   * @param suiteName    The name of the suite
   * @param testName     The name of the test
   * @param duration     The duration of the test
   * @param categories   The category of the test, if any
   * @param errorMessage The error message, if any
   * @return A new SuiteResults object representing a failed test result
   */
  def failureTest(suiteName: String, testName: String, duration: Option[Long], categories: Option[String] = None, errorMessage: Option[String]): SuiteResults =
    SuiteResults(RESULT_TYPE_TEST, suite = suiteName, name = testName, status = Failure, duration = duration, categories = categories, errMessage = errorMessage)

  /**
   * Factory method for creating a successful Suite Before method result.
   *
   * @param suiteName The name of the suite
   * @param methodName  The name of the method
   * @param duration  The duration of the test
   * @return A new SuiteResults object representing a successful test result
   */
  def successBefore(suiteName: String, methodName: String, duration: Option[Long]): SuiteResults =
    SuiteResults(RESULT_TYPE_BEFORE_METHOD, suite = suiteName, name = methodName, status = Success, duration = duration)

  /**
   * Factory method for creating a failed Suite Before method result.
   *
   * @param suiteName  The name of the suite
   * @param methodName The name of the method
   * @param duration   The duration of the test
   * @return A new SuiteResults object representing a successful test result
   */
  def failureBefore(suiteName: String, methodName: String, duration: Option[Long], errorMessage: Option[String]): SuiteResults =
    SuiteResults(RESULT_TYPE_BEFORE_METHOD, suite = suiteName, name = methodName, status = Failure, duration = duration, errMessage = errorMessage)

  /**
   * Factory method for creating a successful Suite After method result.
   *
   * @param suiteName  The name of the suite
   * @param methodName The name of the method
   * @param duration   The duration of the test
   * @return A new SuiteResults object representing a successful test result
   */
  def successAfter(suiteName: String, methodName: String, duration: Option[Long]): SuiteResults =
    SuiteResults(RESULT_TYPE_AFTER_METHOD, suite = suiteName, name = methodName, status = Success, duration = duration)

  /**
   * Factory method for creating a failed Suite After method result.
   *
   * @param suiteName  The name of the suite
   * @param methodName The name of the method
   * @param duration   The duration of the test
   * @return A new SuiteResults object representing a successful test result
   */
  def failureAfter(suiteName: String, methodName: String, duration: Option[Long], errorMessage: Option[String]): SuiteResults =
    SuiteResults(RESULT_TYPE_AFTER_METHOD, suite = suiteName, name = methodName, status = Failure, duration = duration, errMessage = errorMessage)
}
