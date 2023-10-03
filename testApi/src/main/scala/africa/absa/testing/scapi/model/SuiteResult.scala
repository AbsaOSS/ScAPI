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

import africa.absa.testing.scapi.model.SuiteResultType.SuiteResultType

import scala.util.{Failure, Try}

/**
 * Represents the results of a suite test.
 *
 * @param resultType   The type of the result (e.g. Before, Test, After)
 * @param suiteName    The name of the test suite
 * @param name         The specific name of the test or method within the suite
 * @param result       The outcome of the test (e.g. Success, Failure)
 * @param duration     The time taken to execute the test, if applicable (in milliseconds)
 * @param categories   The categories or tags associated with the test, if any
 */
case class SuiteResult(resultType: SuiteResultType,
                       suiteName: String,
                       name: String,
                       result: Try[Unit],
                       duration: Option[Long],
                       categories: Option[String] = None) {
  /**
   * Checks if the suite result was a success.
   *
   * @return true if the suite result was a success, false otherwise
   */
  def isSuccess: Boolean = result.isSuccess

  def errorMsg: Option[String] = result match {
    case Failure(t) => Some(t.getMessage)
    case _ => None
  }
}
