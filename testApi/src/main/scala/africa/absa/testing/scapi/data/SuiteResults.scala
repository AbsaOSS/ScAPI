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

case class SuiteResults(result_type: String, suite: String, name: String, status: String, duration: Option[Long], errMessage: Option[String] = None, categories: Option[String] = None) {
  def isSuccess: Boolean = this.status == SuiteResults.Success
}

object SuiteResults {
  val Success: String = "Success"
  val Failure: String = "Failure"

  private val RESULT_TYPE_BEFORE_METHOD = "Before"
  private val RESULT_TYPE_TEST = "Test"
  private val RESULT_TYPE_AFTER_METHOD = "After"

  def successTest(suiteName: String, testName: String, duration: Option[Long], category: Option[String] = None): SuiteResults =
    SuiteResults(RESULT_TYPE_TEST, suite = suiteName, name = testName, status = Success, duration = duration, categories = category)

  def failureTest(suiteName: String, testName: String, duration: Option[Long], categories: Option[String] = None, errorMessage: Option[String]): SuiteResults =
    SuiteResults(RESULT_TYPE_TEST, suite = suiteName, name = testName, status = Failure, duration = duration, categories = categories, errMessage = errorMessage)

  def successBefore(suiteName: String, methodName: String, duration: Option[Long]): SuiteResults =
    SuiteResults(RESULT_TYPE_BEFORE_METHOD, suite = suiteName, name = methodName, status = Success, duration = duration)

  def failureBefore(suiteName: String, methodName: String, duration: Option[Long], errorMessage: Option[String]): SuiteResults =
    SuiteResults(RESULT_TYPE_BEFORE_METHOD, suite = suiteName, name = methodName, status = Failure, duration = duration, errMessage = errorMessage)

  def successAfter(suiteName: String, methodName: String, duration: Option[Long]): SuiteResults =
    SuiteResults(RESULT_TYPE_AFTER_METHOD, suite = suiteName, name = methodName, status = Success, duration = duration)

  def failureAfter(suiteName: String, methodName: String, duration: Option[Long], errorMessage: Option[String]): SuiteResults =
    SuiteResults(RESULT_TYPE_AFTER_METHOD, suite = suiteName, name = methodName, status = Failure, duration = duration, errMessage = errorMessage)
}
