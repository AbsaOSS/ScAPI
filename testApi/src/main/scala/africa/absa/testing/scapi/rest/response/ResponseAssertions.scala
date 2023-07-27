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
import africa.absa.testing.scapi.utils.validation.ContentValidator

object ResponseAssertions {
  val STATUS_CODE = "status-code"
  val BODY_CONTAINS = "body-contains"

  def validateContent(assertion: Assertion): Unit = {
    assertion.name.toLowerCase match {
      case STATUS_CODE => ContentValidator.validateIntegerString(assertion.value)
      case BODY_CONTAINS => ContentValidator.validateNonEmptyString(assertion.value)
      case _ => throw UndefinedAssertionType(assertion.name)
    }
  }

  def performAssertions(response: Response, assertions: Set[Assertion]): Boolean = {
    assertions.forall { assertion =>
      assertion.name match {
        case STATUS_CODE => assertStatusCode(response, assertion.value)
        case BODY_CONTAINS => assertBodyContains(response, assertion.value)
        case _ => throw new IllegalArgumentException(s"Unsupported assertion: ${assertion.name}")
      }
    }
  }

  def assertStatusCode(response: Response, expectedCode: String): Boolean = {
    val iExpectedCode: Int = expectedCode.toInt

    val isSuccess: Boolean = response.statusCode == iExpectedCode
    if (!isSuccess)
      println(s"Expected $iExpectedCode, but got ${response.statusCode}") // TODO - replace by logger call in Issue #11
    isSuccess
  }

  def assertBodyContains(response: Response, expectedContent: String): Boolean = {
    val isSuccess: Boolean = response.body.contains(expectedContent)
    if (!isSuccess)
      println(s"Expected body to contain $expectedContent") // TODO - replace by logger call in Issue #11
    isSuccess
  }
}