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

  val STATUS_CODE_EQUALS = "status-code-equals"
  val STATUS_CODE_IS_SUCCESS = "status-code-is-success"
  val STATUS_CODE_IS_CLIENT_ERROR = "status-code-is_client_error"
  val STATUS_CODE_IS_SERVER_ERROR = "status-code-is_server_error"
  val BODY_CONTAINS = "body-contains"

  /**
   * Validates the content of an assertion response action object depending on its type.
   *
   * @param responseAction The response action object to be validated.
   * @throws UndefinedResponseActionType If the response action type is not recognized.
   */
  def validateContent(responseAction: ResponseAction): Unit = {
    responseAction.name.toLowerCase match {

      // status-code-...
      case STATUS_CODE_EQUALS =>
        responseAction.params.get("code") match {
          case code => ContentValidator.validateIntegerString(code.get, s"ResponseAssertion.$STATUS_CODE_EQUALS.code")
          case None => throw new IllegalArgumentException(s"Missing required 'code' parameter for assertion $STATUS_CODE_EQUALS logic.")
        }
      case STATUS_CODE_IS_SUCCESS | STATUS_CODE_IS_CLIENT_ERROR | STATUS_CODE_IS_SERVER_ERROR => ()

      // body-...
      case BODY_CONTAINS =>
        responseAction.params.get("body") match {
          case body => ContentValidator.validateNonEmptyString(body.get, s"ResponseAssertion.$BODY_CONTAINS.body")
          case None => throw new IllegalArgumentException(s"Missing required 'body' parameter for assertion $BODY_CONTAINS logic.")
        }
      case _ => throw UndefinedResponseActionType(responseAction.name)
    }
  }

  /**
   * Performs assertion actions on a response depending on the type of assertion method provided.
   *
   * @param response  The response to perform the assertions on.
   * @param responseAction The assertion response action to perform on the response.
   * @return Boolean value indicating whether the assertion passed or failed.
   * @throws IllegalArgumentException If the assertion type is not supported.
   */
  def performResponseAction(response: Response, responseAction: ResponseAction): Boolean = {
    responseAction.name match {

      // status-code-...
      case STATUS_CODE_EQUALS =>
        val code = responseAction.params("code")
        assertStatusCodeEquals(response, code)
      case STATUS_CODE_IS_SUCCESS => assertStatusCodeSuccess(response)
      case STATUS_CODE_IS_CLIENT_ERROR => assertStatusCodeIsClientError(response)
      case STATUS_CODE_IS_SERVER_ERROR => assertStatusCodeIsServerError(response)

      // body-...
      case BODY_CONTAINS =>
        val body = responseAction.params("body")
        assertBodyContains(response, body)
      case _ => throw new IllegalArgumentException(s"Unsupported assertion method [group: assert]: ${responseAction.name}")
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
  def assertStatusCodeEquals(response: Response, expectedCode: String): Boolean = {
    val iExpectedCode: Int = expectedCode.toInt

    val isSuccess: Boolean = response.statusCode == iExpectedCode
    if (!isSuccess) {
      Logger.error(s"Expected $iExpectedCode, but got ${response.statusCode}")
    }
    isSuccess
  }

  /**
   * Checks if the status code of the given response is in the success range (200-299).
   *
   * @param response The response object containing the status code.
   * @return True if the status code is in the range 200-299, otherwise false.
   */
  def assertStatusCodeSuccess(response: Response): Boolean = {
    response.statusCode >= 200 && response.statusCode <= 299
  }

  /**
   * Checks if the status code of the given response is in the client error range (400-499).
   *
   * @param response The response object containing the status code.
   * @return True if the status code is in the range 400-499, otherwise false.
   */
  def assertStatusCodeIsClientError(response: Response): Boolean = {
    response.statusCode >= 400 && response.statusCode <= 499
  }

  /**
   * Checks if the status code of the given response is in the server error range (500-599).
   *
   * @param response The response object containing the status code.
   * @return True if the status code is in the range 500-599, otherwise false.
   */
  def assertStatusCodeIsServerError(response: Response): Boolean = {
    response.statusCode >= 500 && response.statusCode <= 599
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
