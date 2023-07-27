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

object ContentValidator {

  def validateIntegerString(input: String): Unit = {
    Try(input.toInt) match {
      case Success(_) => // Do nothing
      case Failure(e) => throw ContentValidationFailed(input, s"Received value cannot be parsed to an integer: ${e.getMessage}")
    }
  }

  def validateNonEmptyString(input: String): Unit = {
    if (input.isEmpty) {
      throw ContentValidationFailed(input, s"Received string value is empty.")
    }
  }

  def validateNotNone(input: Option[String], paramName: String): Unit = {
    input match {
      case Some(_) => // do nothing, input is valid
      case None => throw new ContentValidationFailed(paramName, "Input cannot be None")
    }
  }
}