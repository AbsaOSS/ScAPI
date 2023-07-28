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

/**
 * Object that validates and performs various assertions on the response received.
 * It extends the functionality of ResponsePerformer.
 */
object ResponseAssertion extends ResponsePerformer {

  val STATUS_CODE = "status-code"
  val BODY_CONTAINS = "body-contains"

  /**
   * Validates the content of an assertion object depending on its type.
   *
   * @param assertion The assertion object to be validated.
   * @throws UndefinedAssertionType If the assertion type is not recognized.
   */
  def validateContent(assertion: Assertion): Unit = {
    assertion.name.toLowerCase match {
      case STATUS_CODE => ContentValidator.validateIntegerString(assertion.param_1)
      case BODY_CONTAINS => ContentValidator.validateNonEmptyString(assertion.param_1)
      case _ => throw UndefinedAssertionType(assertion.name)
    }
  }

  /**
   * Performs assertions on a response depending on the type of assertion provided.
   *
   * @param response  The response to perform the assertions on.
   * @param assertion The assertion to perform on the response.
   * @return Boolean value indicating whether the assertion passed or failed.
   * @throws IllegalArgumentException If the assertion type is not supported.
   */
  def performAssertion(response: Response, assertion: Assertion): Boolean = {
    assertion.name match {
      case STATUS_CODE => assertStatusCode(response, assertion.param_1)
      case BODY_CONTAINS => assertBodyContains(response, assertion.param_1)
      case _ => throw new IllegalArgumentException(s"Unsupported assertion[group: assert]: ${assertion.name}")
    }
  }

  /*
    dedicated actions
   */

  /**
   * Asserts that the status code of the response matches the expected status code.
   *
   * @param response     The response whose status code is to be checked.
   * @param expectedCode The expected status code as a string.
   * @return A Boolean indicating whether the response's status code matches the expected code. Returns true if they match, false otherwise.
   */
  def assertStatusCode(response: Response, expectedCode: String): Boolean = {
    val iExpectedCode: Int = expectedCode.toInt

    val isSuccess: Boolean = response.statusCode == iExpectedCode
    if (!isSuccess)
      println(s"Expected $iExpectedCode, but got ${response.statusCode}") // TODO - replace by logger call in Issue #11
    isSuccess
  }

  /**
   * Asserts that the body of the response contains the expected content.
   *
   * @param response        The HTTP response to check the body of.
   * @param expectedContent The expected content present in the response body as a string.
   * @return A Boolean indicating whether the expected content is present in the response body or not.
   */
  def assertBodyContains(response: Response, expectedContent: String): Boolean = {
    val isSuccess: Boolean = response.body.contains(expectedContent)
    if (!isSuccess)
      println(s"Expected body to contain $expectedContent") // TODO - replace by logger call in Issue #11
    isSuccess
  }
}
