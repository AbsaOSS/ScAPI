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

case class TestResults(suiteName: String,
                       testName: String,
                       status: String,
                       duration: Option[Long] = None,
                       errMessage: Option[String] = None,
                       categories: Option[String] = None)

object TestResults {
  val Success: String = "Success"
  val Failure: String = "Failure"

  def withBooleanStatus(suiteName: String,
                        testName: String,
                        status: Boolean,
                        duration: Option[Long] = None,
                        errMessage: Option[String] = None,
                        categories: Option[String] = None): TestResults =
    TestResults(suiteName = suiteName,
      testName = testName,
      status = if (status) Success else Failure,
      duration = duration,
      errMessage = errMessage,
      categories = categories)
}
