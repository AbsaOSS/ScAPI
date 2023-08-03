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

package africa.absa.testing.scapi.utils.validation

import africa.absa.testing.scapi.ContentValidationFailed
import scala.util.{Failure, Success, Try}

/**
 * Object that provides a validation methods for suite content.
 */
object ContentValidator {

  /**
   * Validates that a string can be parsed to an integer. Throws an exception if the string cannot be parsed.
   *
   * @param input The string to be validated.
   * @throws ContentValidationFailed if the input string cannot be parsed to an integer.
   */
  def validateIntegerString(input: String, param: String): Unit = {
    Try(input.toInt) match {
      case Success(_) => // Do nothing
      case Failure(e) => throw ContentValidationFailed(input, s"Received value of '$param' cannot be parsed to an integer: ${e.getMessage}")
    }
  }

  /**
   * Validates that a string is not empty. Throws an exception if the string is empty.
   *
   * @param input The string to be validated.
   * @throws ContentValidationFailed if the input string is empty.
   */
  def validateNonEmptyString(input: String, param: String): Unit = {
    if (input.isEmpty) {
      throw ContentValidationFailed(input, s"Received string value of '$param' is empty.")
    }
  }

  /**
   * Validates that an Option[String] is not None. Throws an exception if the Option is None.
   *
   * @param input     The Option[String] to be validated.
   * @param paramName The name of the parameter, used in error messaging.
   * @throws ContentValidationFailed if the input Option[String] is None.
   */
  def validateNotNone(input: Option[String], paramName: String): Unit = {
    input match {
      case Some(_) => // do nothing, input is valid
      case None => throw new ContentValidationFailed(paramName, "Input cannot be None")
    }
  }
}
