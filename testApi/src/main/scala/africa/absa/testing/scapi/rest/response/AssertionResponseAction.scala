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

import africa.absa.testing.scapi.UndefinedResponseActionType
import africa.absa.testing.scapi.json.ResponseAction
import africa.absa.testing.scapi.logging.Logger
import africa.absa.testing.scapi.utils.validation.ContentValidator

/**
 * Object that validates and performs various assertion response actions on the response received.
 * It extends the functionality of ResponsePerformer.
 */
object AssertionResponseAction extends ResponsePerformer {

  val STATUS_CODE = "status-code"
  val BODY_CONTAINS = "body-contains"

  /**
   * Validates the content of an assertion response action object depending on its type.
   *
   * @param responseAction The response action object to be validated.
   * @throws UndefinedResponseActionType If the response action type is not recognized.
   */
  def validateContent(responseAction: ResponseAction): Unit = {
    responseAction.name.toLowerCase match {
      case STATUS_CODE =>
        responseAction.params.get("param_1") match {
          case param_1 => ContentValidator.validateIntegerString(param_1.get, s"ResponseAssertion.$STATUS_CODE.param_1")
          case None => throw new IllegalArgumentException(s"Missing required param_1 for assertion $STATUS_CODE")
        }
      case BODY_CONTAINS =>
        responseAction.params.get("param_1") match {
          case param_1 => ContentValidator.validateNonEmptyString(param_1.get, s"ResponseAssertion.$BODY_CONTAINS.param_1")
          case None => throw new IllegalArgumentException(s"Missing required param_1 for assertion $BODY_CONTAINS")
        }
      case _ => throw UndefinedResponseActionType(responseAction.name)
    }
  }


  /**
   * Performs assertion actions on a response depending on the type of assertion action provided.
   *
   * @param response  The response to perform the assertions on.
   * @param responseAction The assertion response action to perform on the response.
   * @return Boolean value indicating whether the assertion passed or failed.
   * @throws IllegalArgumentException If the assertion type is not supported.
   */
  def performResponseAction(response: Response, responseAction: ResponseAction): Boolean = {
    responseAction.name match {
      case STATUS_CODE =>
        val param_1 = responseAction.params.getOrElse("param_1", throw new IllegalArgumentException("param_1 is missing"))
        assertStatusCode(response, param_1)
      case BODY_CONTAINS =>
        val param_1 = responseAction.params.getOrElse("param_1", throw new IllegalArgumentException("param_1 is missing"))
        assertBodyContains(response, param_1)
      case _ => throw new IllegalArgumentException(s"Unsupported assertion[group: assert]: ${responseAction.name}")
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
    if (!isSuccess) {
      Logger.error(s"Expected $iExpectedCode, but got ${response.statusCode}")
    }
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
        Logger.error(s"Expected body to contain $expectedContent")
    isSuccess
  }
}