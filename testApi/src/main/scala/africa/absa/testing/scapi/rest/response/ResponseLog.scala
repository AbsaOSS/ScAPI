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

package africa.absa.testing.scapi.rest.response

import africa.absa.testing.scapi.UndefinedAssertionType
import africa.absa.testing.scapi.json.Assertion
import africa.absa.testing.scapi.logging.Logger
import africa.absa.testing.scapi.utils.validation.ContentValidator

/**
 * Singleton object `ResponseLog` that extends the `ResponsePerformer` trait.
 * It provides utilities for validating and performing assertions, and logging info messages.
 */
object ResponseLog extends ResponsePerformer {

  val INFO = "info"

  /**
   * This method validates the content of the assertion.
   * It checks if the assertion's name is "info", then validates if its `param_1` is a non-empty string.
   * For all other assertion names, it throws an `UndefinedAssertionType` exception.
   *
   * @param assertion The assertion to be validated.
   * @throws UndefinedAssertionType if the assertion's name is not recognized.
   */
  def validateContent(assertion: Assertion): Unit = {
    assertion.name.toLowerCase match {
      case INFO =>
        assertion.params.get("param_1") match {
          case param_1 => ContentValidator.validateNonEmptyString(param_1.get, s"ResponseLog.$INFO.param_1")
          case None => throw new IllegalArgumentException(s"Missing required param_1 for assertion $INFO")
        }
      case _ => throw UndefinedAssertionType(assertion.name)
    }
  }

  /**
   * This method performs the necessary assertions on the response.
   * It checks if the assertion's name is "info", then logs the `param_1` as info message.
   * For all other assertion names, it throws an `IllegalArgumentException`.
   *
   * @param response  The response on which the assertions are to be performed.
   * @param assertion The assertion to be performed on the response.
   * @throws IllegalArgumentException if the assertion's name is not recognized.
   */
  def performAssertion(response: Response, assertion: Assertion): Boolean = {
    assertion.name match {
      case INFO =>
        val param_1 = assertion.params.getOrElse("param_1", throw new IllegalArgumentException("param_1 is missing"))
        logInfo(param_1)
      case _ => throw new IllegalArgumentException(s"Unsupported assertion[group: log]: ${assertion.name}")
    }
  }

  /*
    dedicated actions
   */

  /**
   * This method logs a message at the INFO level.
   *
   * @param message The message to be logged.
   */
  def logInfo(message: String): Boolean = {
    Logger.info(message)
    true
  }
}
