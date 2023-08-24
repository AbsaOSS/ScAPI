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
import spray.json._
import scala.xml.XML

/**
 * Object that validates and performs various assertion response actions on the response received.
 * It extends the functionality of ResponsePerformer.
 */
object AssertionResponseAction extends ResponsePerformer {

  // response-time-...
  val RESPONSE_TIME_IS_BELOW = "response-time-is-below"
  val RESPONSE_TIME_IS_ABOVE = "response-time-is-above"

  // status-code-...
  val STATUS_CODE_EQUALS = "status-code-equals"
  val STATUS_CODE_IS_SUCCESS = "status-code-is-success"
  val STATUS_CODE_IS_CLIENT_ERROR = "status-code-is-client-error"
  val STATUS_CODE_IS_SERVER_ERROR = "status-code-is-server-error"

  // header-...
  val HEADER_EXISTS = "header-exists"
  val HEADER_VALUE_EQUALS = "header-value-equals"

  // content-type-...
  val CONTENT_TYPE_IS_JSON = "content-type-is-json"
  val CONTENT_TYPE_IS_XML = "content-type-is-xml"
  val CONTENT_TYPE_IS_HTML = "content-type-is-html"

  // cookies-...
  val COOKIE_EXISTS = "cookie-exists"
  val COOKIE_VALUE_EQUALS = "cookie-value-equals"
  val COOKIE_IS_SECURED = "cookie-is-secured"
  val COOKIE_IS_NOT_SECURED = "cookie-is-not-secured"

  // body-...
  val BODY_CONTAINS_TEXT = "body-contains-text"

  /**
   * Validates the content of an assertion response action object depending on its type.
   *
   * @param responseAction The response action object to be validated.
   * @throws UndefinedResponseActionType If the response action type is not recognized.
   */
  def validateContent(responseAction: ResponseAction): Unit = {
    responseAction.name.toLowerCase match {

      // response-time-...
      case RESPONSE_TIME_IS_BELOW | RESPONSE_TIME_IS_ABOVE =>
        responseAction.params.getOrElse("limit", None) match {
          case limit: String => ContentValidator.validateLongString(limit, s"ResponseAssertion.${responseAction.name}.limit")
          case None => throw new IllegalArgumentException(s"Missing required 'limit' parameter for assertion ${responseAction.name} logic.")
        }

      // status-code-...
      case STATUS_CODE_EQUALS =>
        responseAction.params.getOrElse("code", None) match {
          case code: String => ContentValidator.validateIntegerString(code, s"ResponseAssertion.$STATUS_CODE_EQUALS.code")
          case None => throw new IllegalArgumentException(s"Missing required 'code' parameter for assertion $STATUS_CODE_EQUALS logic.")
        }
      case STATUS_CODE_IS_SUCCESS | STATUS_CODE_IS_CLIENT_ERROR | STATUS_CODE_IS_SERVER_ERROR => ()

      // header-...
      case HEADER_EXISTS | HEADER_VALUE_EQUALS =>
        responseAction.params.getOrElse("headerName", None) match {
          case headerName: String => ContentValidator.validateNonEmptyString(headerName, s"ResponseAssertion.${responseAction.name}.headerName")
          case None => throw new IllegalArgumentException(s"Missing required 'headerName' parameter for assertion ${responseAction.name} logic.")
        }
        responseAction.name.toLowerCase match {
          case HEADER_VALUE_EQUALS =>
            responseAction.params.getOrElse("expectedValue", None) match {
              case expectedValue: String => ContentValidator.validateNonEmptyString(expectedValue, s"ResponseAssertion.$HEADER_VALUE_EQUALS.expectedValue")
              case None => throw new IllegalArgumentException(s"Missing required 'expectedValue' parameter for assertion $HEADER_VALUE_EQUALS logic.")
            }
          case _ => ()
        }

      // content-type-...
      case CONTENT_TYPE_IS_JSON | CONTENT_TYPE_IS_XML | CONTENT_TYPE_IS_HTML => ()

      // cookies-...
      case COOKIE_EXISTS | COOKIE_VALUE_EQUALS | COOKIE_IS_SECURED | COOKIE_IS_NOT_SECURED =>
        responseAction.params.getOrElse("cookieName", None) match {
          case cookieName: String => ContentValidator.validateNonEmptyString(cookieName, s"ResponseAssertion.${responseAction.name}.cookieName")
          case None => throw new IllegalArgumentException(s"Missing required 'cookieName' parameter for assertion ${responseAction.name} logic.")
        }
        responseAction.name.toLowerCase match {
          case COOKIE_VALUE_EQUALS =>
            responseAction.params.getOrElse("expectedValue", None) match {
              case expectedValue: String => ContentValidator.validateNonEmptyString(expectedValue, s"ResponseAssertion.$COOKIE_VALUE_EQUALS.expectedValue")
              case None => throw new IllegalArgumentException(s"Missing required 'expectedValue' parameter for assertion $COOKIE_VALUE_EQUALS logic.")
            }
          case _ => ()
        }

      // body-...
      case BODY_CONTAINS_TEXT =>
        responseAction.params.getOrElse("text", None) match {
          case text: String => ContentValidator.validateNonEmptyString(text, s"ResponseAssertion.$BODY_CONTAINS_TEXT.text")
          case None => throw new IllegalArgumentException(s"Missing required 'text' parameter for assertion $BODY_CONTAINS_TEXT logic.")
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

      // response-time-...
      case RESPONSE_TIME_IS_BELOW | RESPONSE_TIME_IS_ABOVE =>
        val limit = responseAction.params("limit")
        responseAction.name match {
          case RESPONSE_TIME_IS_BELOW => assertResponseTimeIsBelow(response, limit)
          case RESPONSE_TIME_IS_ABOVE => assertResponseTimeIsAbove(response, limit)
        }

      // status-code-...
      case STATUS_CODE_EQUALS =>
        val code = responseAction.params("code")
        assertStatusCodeEquals(response, code)
      case STATUS_CODE_IS_SUCCESS => assertStatusCodeSuccess(response)
      case STATUS_CODE_IS_CLIENT_ERROR => assertStatusCodeIsClientError(response)
      case STATUS_CODE_IS_SERVER_ERROR => assertStatusCodeIsServerError(response)

      // header-...
      case HEADER_EXISTS | HEADER_VALUE_EQUALS =>
        val headerName = responseAction.params("headerName")
        responseAction.name match {
          case HEADER_EXISTS => assertHeaderExists(response, headerName)
          case HEADER_VALUE_EQUALS =>
            val expectedValue = responseAction.params("expectedValue")
            assertHeaderValueEquals(response, headerName, expectedValue)
        }

      // content-type-...
      case CONTENT_TYPE_IS_JSON => assertContentTypeIsJson(response)
      case CONTENT_TYPE_IS_XML => assertContentTypeIsXml(response)
      case CONTENT_TYPE_IS_HTML => assertContentTypeIsHtml(response)

      // cookies-...
      case COOKIE_EXISTS | COOKIE_VALUE_EQUALS | COOKIE_IS_SECURED | COOKIE_IS_NOT_SECURED =>
        val cookieName = responseAction.params("cookieName")
        responseAction.name match {
          case COOKIE_EXISTS => assertCookieExists(response, cookieName)
          case COOKIE_VALUE_EQUALS =>
            val expectedValue = responseAction.params("expectedValue")
            assertCookieValueEquals(response, cookieName, expectedValue)
          case COOKIE_IS_SECURED => assertCookieIsSecured(response, cookieName)
          case COOKIE_IS_NOT_SECURED => assertCookieIsNotSecured(response, cookieName)
        }

      // body-...
      case BODY_CONTAINS_TEXT =>
        val text = responseAction.params("text")
        assertBodyContainsText(response, text)

      case _ => throw new IllegalArgumentException(s"Unsupported assertion method [group: assert]: ${responseAction.name}")
    }
  }

  /*
    dedicated actions
   */

  /**
   * Asserts that the response time is below the specified maximum time.
   *
   * @param response      The response whose duration is to be checked.
   * @param maxTimeMillis The maximum allowed time in milliseconds as a string.
   * @return A Boolean indicating whether the response's duration is below the specified maximum time. Returns true if it's below, false otherwise.
   */
  def assertResponseTimeIsBelow(response: Response, maxTimeMillis: String): Boolean = {
    val lMaxTimeMillis: Long = maxTimeMillis.toLong
    response.duration <= lMaxTimeMillis
  }

  /**
   * Asserts that the response time is above the specified minimum time.
   *
   * @param response      The response whose duration is to be checked.
   * @param minTimeMillis The minimum required time in milliseconds as a string.
   * @return A Boolean indicating whether the response's duration is above the specified minimum time. Returns true if it's above, false otherwise.
   */
  def assertResponseTimeIsAbove(response: Response, minTimeMillis: String): Boolean = {
    val lMinTimeMillis: Long = minTimeMillis.toLong
    response.duration >= lMinTimeMillis
  }

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
   * Checks if the specified header exists in the given response.
   *
   * @param response The response object containing the headers.
   * @param headerName The name of the header to check for.
   * @return True if the header exists in the response, otherwise false.
   */
  def assertHeaderExists(response: Response, headerName: String): Boolean = {
    response.headers.contains(headerName.toLowerCase)
  }

  /**
   * Asserts that the value of the specified header in the given response matches the expected value.
   *
   * @param response The response object containing the headers.
   * @param headerName The name of the header to check.
   * @param expectedValue The expected value of the header.
   * @return True if the header value matches the expected value, otherwise false.
   */
  def assertHeaderValueEquals(response: Response, headerName: String, expectedValue: String): Boolean = {
    if (assertHeaderExists(response, headerName))
      expectedValue.equals(response.headers(headerName.toLowerCase).head)
    else
      false
  }

  /**
   * Asserts that the value of the "Content-Type" header in the given response is "application/json".
   *
   * @param response The response object containing the headers.
   * @return True if the "Content-Type" header value is "application/json", otherwise false.
   */
  def assertContentTypeIsJson(response: Response): Boolean = {
    val isContentTypeJson = assertHeaderValueEquals(response, "content-type", "application/json")
    val isBodyJson = try {
      response.body.parseJson
      true
    } catch {
      case _: JsonParser.ParsingException => false
    }

    isContentTypeJson && isBodyJson
  }

  /**
   * Asserts that the value of the "Content-Type" header in the given response is "application/xml".
   *
   * @param response The response object containing the headers.
   * @return True if the "Content-Type" header value is "application/xml", otherwise false.
   */
  def assertContentTypeIsXml(response: Response): Boolean = {
    val isContentTypeXml = assertHeaderValueEquals(response, "content-type", "application/xml")
    val isBodyXml = try {
      XML.loadString(response.body)
      true
    } catch {
      case _: Exception => false
    }

    isContentTypeXml && isBodyXml
  }

  /**
   * Asserts that the value of the "Content-Type" header in the given response is "text/html".
   *
   * @param response The response object containing the headers.
   * @return True if the "Content-Type" header value is "text/html", otherwise false.
   */
  def assertContentTypeIsHtml(response: Response): Boolean = {
    assertHeaderValueEquals(response, "content-type", "text/html")
  }

  // cookies-...

  /**
   * Asserts that the specified cookie exists in the given response.
   *
   * @param response The response object containing the cookies.
   * @param cookieName The name of the cookie to check for existence.
   * @return True if the specified cookie exists in the response, otherwise false.
   */
  def assertCookieExists(response: Response, cookieName: String): Boolean = {
    response.cookies.contains(cookieName)
  }

  /**
   * Asserts that the value of the specified cookie in the given response equals the expected value.
   *
   * @param response      The response object containing the cookies.
   * @param cookieName    The name of the cookie to check.
   * @param expectedValue The expected value of the cookie.
   * @return True if the value of the specified cookie matches the expected value, otherwise false.
   */
  def assertCookieValueEquals(response: Response, cookieName: String, expectedValue: String): Boolean = {
    if (assertCookieExists(response, cookieName))
      response.cookies(cookieName)._1 == expectedValue
    else
      false
  }

  /**
   * Asserts that the specified cookie in the given response is secured.
   *
   * @param response   The response object containing the cookies.
   * @param cookieName The name of the cookie to check.
   * @return True if the specified cookie is secured, otherwise false.
   */
  def assertCookieIsSecured(response: Response, cookieName: String): Boolean = {
    if (assertCookieExists(response, cookieName))
      response.cookies(cookieName)._2
    else
      false
  }

  /**
   * Asserts that the specified cookie in the given response is not secured.
   *
   * @param response   The response object containing the cookies.
   * @param cookieName The name of the cookie to check.
   * @return True if the specified cookie is not secured, otherwise false.
   */
  def assertCookieIsNotSecured(response: Response, cookieName: String): Boolean = {
    if (assertCookieExists(response, cookieName))
      !response.cookies(cookieName)._2
    else
      false
  }

  /**
   * Asserts that the body of the response contains the expected content.
   *
   * @param response        The HTTP response to check the body of.
   * @param text The expected text present in the response body as a string.
   * @return A Boolean indicating whether the expected content is present in the response body or not.
   */
  def assertBodyContainsText(response: Response, text: String): Boolean = {
    val isSuccess: Boolean = response.body.contains(text)
    if (!isSuccess)
        Logger.error(s"Expected body to contain $text")
    isSuccess
  }
}
