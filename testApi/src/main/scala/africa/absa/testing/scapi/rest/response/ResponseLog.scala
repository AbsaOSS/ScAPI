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
import africa.absa.testing.scapi.logging.functions.Scribe
import africa.absa.testing.scapi.utils.ContentValidator

object ResponseLog extends ResponsePerformer {

  val INFO = "info"

  def validateContent(assertion: Assertion): Unit = {
    assertion.name.toLowerCase match {
      case INFO => ContentValidator.validateNonEmptyString(assertion.param_1)
      case _ => throw UndefinedAssertionType(assertion.name)
    }
  }

  def performAssertions(response: Response, assertion: Assertion): Unit = {
    assertion.name match {
      case INFO => logInfo(assertion.param_1)
      case _ => throw new IllegalArgumentException(s"Unsupported assertion[group: log]: ${assertion.name}")
    }
  }

  /*
    dedicated actions
   */
  def logInfo(message: String): Unit = {
    implicit val loggingFunctions: Scribe = Scribe(this.getClass, Scribe.INFO)
    loggingFunctions.info(message)
  }
}
