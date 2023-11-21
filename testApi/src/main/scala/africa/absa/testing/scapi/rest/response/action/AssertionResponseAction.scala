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

package africa.absa.testing.scapi.rest.response.action

import africa.absa.testing.scapi.json.ResponseAction
import africa.absa.testing.scapi.logging.Logger
import africa.absa.testing.scapi.rest.response.Response
import africa.absa.testing.scapi.rest.response.action.types.AssertResponseActionType._
import africa.absa.testing.scapi.utils.validation.ContentValidator
import africa.absa.testing.scapi.{AssertionException, UndefinedResponseActionTypeException}
import com.jayway.jsonpath.{Configuration, JsonPath}
import spray.json._

import scala.util.{Failure, Try}
import scala.xml.XML

/**
 * Object that validates and performs various assertion response actions on the response received.
 * It extends the functionality of ResponsePerformer.
 */
object AssertionResponseAction extends ResponseActions {

  /**
   * Validates the content of an assertion response action object depending on its type.
   *
   * @param responseAction The response action object to be validated.
   * @throws UndefinedResponseActionTypeException If the response action type is not recognized.
   */
  def validateContent(responseAction: ResponseAction): Unit = {
    Logger.debug(s"Validating content for response action. \nResponseAction: $responseAction")

    val action = fromString(responseAction.name.toLowerCase).getOrElse(None)
    action match {

      // response-time-...
      case ResponseTimeIsBelow | ResponseTimeIsAbove =>
        responseAction.params.getOrElse("limit", None) match {
          case limit: String => ContentValidator.validateLongString(limit, s"ResponseAssertion.${responseAction.name}.limit")
          case None => throw new IllegalArgumentException(s"Missing required 'limit' parameter for assertion ${responseAction.name} logic.")
        }

      // status-code-...
      case StatusCodeEquals =>
        responseAction.params.getOrElse("code", None) match {
          case code: String => ContentValidator.validateIntegerString(code, s"ResponseAssertion.$StatusCodeEquals.code")
          case None => throw new IllegalArgumentException(s"Missing required 'code' parameter for assertion $StatusCodeEquals logic.")
        }
      case StatusCodeIsSuccess | StatusCodeIsClientError | StatusCodeIsServerError => ()

      // header-...
      case HeaderExists | HeaderValueEquals =>
        responseAction.params.getOrElse("headerName", None) match {
          case headerName: String => ContentValidator.validateNonEmptyString(headerName, s"ResponseAssertion.${responseAction.name}.headerName")
          case None => throw new IllegalArgumentException(s"Missing required 'headerName' parameter for assertion ${responseAction.name} logic.")
        }
        action match {
          case HeaderValueEquals =>
            responseAction.params.getOrElse("expectedValue", None) match {
              case expectedValue: String => ContentValidator.validateNonEmptyString(expectedValue, s"ResponseAssertion.$HeaderValueEquals.expectedValue")
              case None => throw new IllegalArgumentException(s"Missing required 'expectedValue' parameter for assertion $HeaderValueEquals logic.")
            }
          case _ => ()
        }

      // content-type-...
      case ContentTypeIsJson | ContentTypeIsXml | ContentTypeIsHtml => ()

      // cookies-...
      case CookieExists | CookieValueEquals | CookieIsSecured | CookieIsNotSecured =>
        responseAction.params.getOrElse("cookieName", None) match {
          case cookieName: String => ContentValidator.validateNonEmptyString(cookieName, s"ResponseAssertion.${responseAction.name}.cookieName")
          case None => throw new IllegalArgumentException(s"Missing required 'cookieName' parameter for assertion ${responseAction.name} logic.")
        }
        action match {
          case CookieValueEquals =>
            responseAction.params.getOrElse("expectedValue", None) match {
              case expectedValue: String => ContentValidator.validateNonEmptyString(expectedValue, s"ResponseAssertion.$CookieValueEquals.expectedValue")
              case None => throw new IllegalArgumentException(s"Missing required 'expectedValue' parameter for assertion $CookieValueEquals logic.")
            }
          case _ => ()
        }

      // body-...
      case BodyIsEmpty | BodyIsNotEmpty => ()

      case BodyContainsText =>
        responseAction.params.getOrElse("text", None) match {
          case text: String => ContentValidator.validateNonEmptyString(text, s"ResponseAssertion.$BodyContainsText.text")
          case None => throw new IllegalArgumentException(s"Missing required 'text' parameter for assertion $BodyContainsText logic.")
        }

      case BodyEquals =>
        responseAction.params.getOrElse("body", None) match {
          case body: String => ContentValidator.validateNonEmptyString(body, s"ResponseAssertion.$BodyEquals.body")
          case None => throw new IllegalArgumentException(s"Missing required 'body' parameter for assertion $BodyEquals logic.")
        }

      case BodyLengthEquals =>
        responseAction.params.getOrElse("length", None) match {
          case length: String => ContentValidator.validateLongString(length, s"ResponseAssertion.$BodyLengthEquals.length")
          case None => throw new IllegalArgumentException(s"Missing required 'length' parameter for assertion $BodyLengthEquals logic.")
        }

      case BodyStartsWith =>
        responseAction.params.getOrElse("prefix", None) match {
          case prefix: String => ContentValidator.validateNonEmptyString(prefix, s"ResponseAssertion.$BodyStartsWith.prefix")
          case None => throw new IllegalArgumentException(s"Missing required 'prefix' parameter for assertion $BodyStartsWith logic.")
        }

      case BodyEndsWith =>
        responseAction.params.getOrElse("suffix", None) match {
          case suffix: String => ContentValidator.validateNonEmptyString(suffix, s"ResponseAssertion.$BodyEndsWith.suffix")
          case None => throw new IllegalArgumentException(s"Missing required 'suffix' parameter for assertion $BodyEndsWith logic.")
        }

      case BodyMatchesRegex =>
        responseAction.params.getOrElse("regex", None) match {
          case regex: String => ContentValidator.validateNonEmptyString(regex, s"ResponseAssertion.$BodyMatchesRegex.regex")
          case None => throw new IllegalArgumentException(s"Missing required 'regex' parameter for assertion $BodyMatchesRegex logic.")
        }

      // body-json-...
      case BodyJsonIsJsonArray | BodyJsonIsJsonObject => ()

      case BodyJsonPathExists =>
        responseAction.params.getOrElse("jsonPath", None) match {
          case jsonPath: String => ContentValidator.validateNonEmptyString(jsonPath, s"ResponseAssertion.$BodyJsonPathExists.jsonPath")
          case None => throw new IllegalArgumentException(s"Missing required 'jsonPath' parameter for assertion $BodyJsonPathExists logic.")
        }

      case _ => throw UndefinedResponseActionTypeException(responseAction.name)
    }
  }

  /**
   * Performs assertion actions on a response depending on the type of assertion method provided.
   *
   * @param response       The response on which the assertions are to be performed.
   * @param responseAction The assertion response action to be performed on the response.
   * @throws UndefinedResponseActionTypeException If the assertion type is not supported.
   * @return A Try[Unit] indicating the success of the assertion operation.
   */
  def performResponseAction(response: Response, responseAction: ResponseAction): Try[Unit] = {
    Logger.trace(s"Performing response action: \nResponse: ${response.toRichString}, \nResponseAction: ${responseAction.toRichString}")

    val action = fromString(responseAction.name.toLowerCase).getOrElse(None)
    action match {

      // response-time-...
      case ResponseTimeIsBelow | ResponseTimeIsAbove =>
        val limit = responseAction.params("limit")
        action match {
          case ResponseTimeIsBelow => assertResponseTimeIsBelow(response, limit)
          case ResponseTimeIsAbove => assertResponseTimeIsAbove(response, limit)
        }

      // status-code-...
      case StatusCodeEquals =>
        val code = responseAction.params("code")
        assertStatusCodeEquals(response, code)
      case StatusCodeIsSuccess => assertStatusCodeSuccess(response)
      case StatusCodeIsClientError => assertStatusCodeIsClientError(response)
      case StatusCodeIsServerError => assertStatusCodeIsServerError(response)

      // header-...
      case HeaderExists | HeaderValueEquals =>
        val headerName = responseAction.params("headerName")
        action match {
          case HeaderExists => assertHeaderExists(response, headerName)
          case HeaderValueEquals =>
            val expectedValue = responseAction.params("expectedValue")
            assertHeaderValueEquals(response, headerName, expectedValue)
        }

      // content-type-...
      case ContentTypeIsJson => assertContentTypeIsJson(response)
      case ContentTypeIsXml => assertContentTypeIsXml(response)
      case ContentTypeIsHtml => assertContentTypeIsHtml(response)

      // cookies-...
      case CookieExists | CookieValueEquals | CookieIsSecured | CookieIsNotSecured =>
        val cookieName = responseAction.params("cookieName")
        action match {
          case CookieExists => assertCookieExists(response, cookieName)
          case CookieValueEquals =>
            val expectedValue = responseAction.params("expectedValue")
            assertCookieValueEquals(response, cookieName, expectedValue)
          case CookieIsSecured => assertCookieIsSecured(response, cookieName)
          case CookieIsNotSecured => assertCookieIsNotSecured(response, cookieName)
        }

      // body-...
      case BodyEquals =>
        val body = responseAction.params("body")
        assertBodyEquals(response, body)

      case BodyContainsText =>
        val text = responseAction.params("text")
        assertBodyContainsText(response, text)

      case BodyIsEmpty =>
        assertBodyIsEmpty(response)

      case BodyIsNotEmpty =>
        assertBodyIsNotEmpty(response)

      case BodyLengthEquals =>
        val length = responseAction.params("length")
        assertBodyLengthEquals(response, length)

      case BodyStartsWith =>
        val prefix = responseAction.params("prefix")
        assertBodyStartsWith(response, prefix)

      case BodyEndsWith =>
        val suffix = responseAction.params("suffix")
        assertBodyEndsWith(response, suffix)

      case BodyMatchesRegex =>
        val regex = responseAction.params("regex")
        assertBodyMatchesRegex(response, regex)

      // body-json-...
      case BodyJsonIsJsonArray =>
        assertBodyIsJsonArray(response)

      case BodyJsonIsJsonObject =>
        assertBodyIsJsonObject(response)

      case BodyJsonPathExists =>
        val jsonPath = responseAction.params("jsonPath")
        assertBodyJsonPathExists(response, jsonPath)

      case _ => Failure(UndefinedResponseActionTypeException(s"Unsupported assertion method [group: assert]: ${responseAction.name}"))
    }
  }

  /*
    dedicated actions
   */

  /**
   * Asserts that the response duration is below the specified maximum time.
   *
   * @param response      The response object containing the duration to be checked.
   * @param maxTimeMillis The maximum allowed duration in milliseconds, provided as a string.
   * @return A Try[Unit] that succeeds if the response's duration is below the specified maximum time, and fails with an AssertionException otherwise.
   */
  private def assertResponseTimeIsBelow(response: Response, maxTimeMillis: String): Try[Unit] = Try {
    val lMaxTimeMillis: Long = maxTimeMillis.toLong

    if (response.duration > lMaxTimeMillis) {
      throw AssertionException(s"Expected maximal length '$lMaxTimeMillis' is smaller then received '${response.duration}' one.")
    }
  }

  /**
   * Asserts that the response duration is greater than or equal to the specified minimum time.
   *
   * @param response      The response object containing the duration to be checked.
   * @param minTimeMillisString The minimum required duration in milliseconds, provided as a string.
   * @return A Try[Unit] that is a Success if the response's duration is greater than or equal to the specified minimum time, and a Failure with an AssertionException otherwise.
   */
  private def assertResponseTimeIsAbove(response: Response, minTimeMillisString: String): Try[Unit] = Try {
    val minTimeMillis: Long = minTimeMillisString.toLong

    if (response.duration < minTimeMillis) {
      throw AssertionException(s"Expected minimal length '$minTimeMillis' is bigger then received '${response.duration}' one.")
    }
  }

  /**
   * Compares the status code of the given response with the expected status code.
   *
   * @param response     The HTTP response object to be evaluated.
   * @param expectedCodeString The expected HTTP status code as a string.
   * @return A Try[Unit] that is successful if the response's status code matches the expected code, and contains an exception otherwise.
   * @throws AssertionException if the response's status code does not match the expected code.
   */
  private def assertStatusCodeEquals(response: Response, expectedCodeString: String): Try[Unit] = Try {
    val expectedCode: Int = expectedCodeString.toInt

    if (response.statusCode != expectedCode) {
      throw AssertionException(s"Expected $expectedCode, but got ${response.statusCode}")
    }
  }

  /**
   * Asserts if the status code of the given response is within the success range (200-299).
   *
   * @param response      The HTTP response object containing the status code.
   * @return A Try[Unit] that is a Success if the status code is within the range 200-299, and a Failure with an AssertionException otherwise.
   */
  private def assertStatusCodeSuccess(response: Response): Try[Unit] = Try {
    if (!(response.statusCode >= 200 && response.statusCode <= 299)) {
      throw AssertionException(s"Received status code '${response.statusCode}' is not in expected range (200 - 299).")
    }
  }

  /**
   * Asserts that the status code of the given response is within the client error range (400-499).
   *
   * @param response      The response object containing the status code.
   * @return A Try[Unit] that is a Success if the status code is within the range 400-499, and a Failure with an AssertionException otherwise.
   */
  private def assertStatusCodeIsClientError(response: Response): Try[Unit] = Try {
    if (!(response.statusCode >= 400 && response.statusCode <= 499)) {
      throw AssertionException(s"Received status code '${response.statusCode}' is not in expected range (400 - 499).")
    }
  }

  /**
   * Asserts that the status code of the given response is within the server error range (500-599).
   *
   * @param response      The response object containing the status code.
   * @return A Try[Unit] that is a Success if the status code is within the range 500-599, and a Failure with an AssertionException otherwise.
   */
  private def assertStatusCodeIsServerError(response: Response): Try[Unit] = Try {
    if (!(response.statusCode >= 500 && response.statusCode <= 599)) {
      throw AssertionException(s"Received status code '${response.statusCode}' is not in expected range (500 - 599).")
    }
  }

  /**
   * Asserts that the specified header exists in the given response.
   *
   * @param response      The response object containing the headers.
   * @param headerName    The name of the header to check for.
   * @return A Try[Unit] that is a Success if the header exists in the response, and a Failure with an AssertionException otherwise.
   */
  private def assertHeaderExists(response: Response, headerName: String): Try[Unit] = Try {
    if (!response.headers.contains(headerName.toLowerCase)) {
      throw AssertionException(s"Expected header '$headerName' not found.")
    }
  }

  /**
   * Asserts that the value of the specified header in the given response matches the expected value.
   *
   * @param response      The response object containing the headers.
   * @param headerName    The name of the header to check.
   * @param expectedValue The expected value of the header.
   * @return A Try[Unit] that is a Success if the header value matches the expected value, and a Failure with an AssertionException otherwise.
   */
  private def assertHeaderValueEquals(response: Response, headerName: String, expectedValue: String): Try[Unit] = Try {
    if (assertHeaderExists(response, headerName).isFailure) {
      throw AssertionException(s"Expected header '$headerName' not found.")
    } else if (!expectedValue.equals(response.headers(headerName.toLowerCase).head)) {
      throw AssertionException(s"Expected header '$headerName' value '$expectedValue' is not equal to " +
        s"received header value '${response.headers(headerName.toLowerCase).head}'.")
    }
  }

  /**
   * Asserts that the value of the "Content-Type" header in the given response is "application/json".
   *
   * @param response      The response object containing the headers.
   * @return A Try[Unit] that is a Success if the "Content-Type" header value is "application/json", and a Failure with an AssertionException otherwise.
   */
  private def assertContentTypeIsJson(response: Response): Try[Unit] = Try {
    val isContentTypeJson = assertHeaderValueEquals(response, "content-type", "application/json")
    val isBodyJson = try {
      response.body.parseJson
      true
    } catch {
      case _: JsonParser.ParsingException => false
    }

    if (!isContentTypeJson.isSuccess || !isBodyJson) {
      throw AssertionException("Received content is not JSON type.")
    }
  }

  /**
   * Asserts that the value of the "Content-Type" header in the given response is "application/xml".
   *
   * @param response      The response object containing the headers.
   * @return A Try[Unit] that is a Success if the "Content-Type" header value is "application/xml", and a Failure with an AssertionException otherwise.
   */
  private def assertContentTypeIsXml(response: Response): Try[Unit] = Try {
    val isContentTypeXml = assertHeaderValueEquals(response, "content-type", "application/xml")
    val isBodyXml = try {
      XML.loadString(response.body)
      true
    } catch {
      case _: Exception => false
    }

    if (!isContentTypeXml.isSuccess || !isBodyXml) {
      throw AssertionException("Received content is not XML type.")
    }
  }

  /**
   * Asserts that the value of the "Content-Type" header in the given response is "text/html".
   *
   * @param response      The response object containing the headers.
   * @return A Try[Unit] that is a Success if the "Content-Type" header value is "text/html", and a Failure with an AssertionException otherwise.
   */
  private def assertContentTypeIsHtml(response: Response): Try[Unit] = {
    assertHeaderValueEquals(response, "content-type", "text/html").recover {
      f => throw AssertionException(s"Received content is not HTML type. Details: ${f.getMessage}")
    }
  }

  // cookies-...

  /**
   * Asserts that the specified cookie exists in the given response.
   *
   * @param response      The response object containing the cookies.
   * @param cookieName    The name of the cookie to check for existence.
   * @return A Try[Unit] that is a Success if the specified cookie exists in the response, and a Failure with an AssertionException otherwise.
   */
  private def assertCookieExists(response: Response, cookieName: String): Try[Unit] = Try {
    if (!response.cookies.contains(cookieName)) {
      throw AssertionException(s"Cookie '$cookieName' does not exist in the response.")
    }
  }

  /**
   * Asserts that the value of the specified cookie in the given response equals the expected value.
   *
   * @param response      The response object containing the cookies.
   * @param cookieName    The name of the cookie to check.
   * @param expectedValue The expected value of the cookie.
   * @return A Try[Unit] that is a Success if the value of the specified cookie matches the expected value, and a Failure with an AssertionException otherwise.
   */
  private def assertCookieValueEquals(response: Response, cookieName: String, expectedValue: String): Try[Unit] = {
    assertCookieExists(response, cookieName).flatMap { _ =>
      Try {
        if (!(response.cookies(cookieName).value == expectedValue)) {
          throw AssertionException(s"Cookie '$cookieName' value does not match expected value '$expectedValue'.")
        }
      }
    }
  }

  /**
   * Asserts that the specified cookie in the given response is secured.
   *
   * @param response      The response object containing the cookies.
   * @param cookieName    The name of the cookie to check.
   * @return A Try[Unit] that is a Success if the specified cookie is secured, and a Failure with an AssertionException otherwise.
   */
  private def assertCookieIsSecured(response: Response, cookieName: String): Try[Unit] = {
    assertCookieExists(response, cookieName).flatMap { _ =>
      Try {
        if (!response.cookies(cookieName).secured) {
          throw AssertionException(s"Cookie '$cookieName' is not secured.")
        }
      }
    }
  }

  /**
   * Asserts that the specified cookie in the given response is not secured.
   *
   * @param response      The response object containing the cookies.
   * @param cookieName    The name of the cookie to check.
   * @return A Try[Unit] that is a Success if the specified cookie is not secured, and a Failure with an AssertionException otherwise.
   */
  private def assertCookieIsNotSecured(response: Response, cookieName: String): Try[Unit] = {
    assertCookieExists(response, cookieName).flatMap { _ =>
      Try {
        if (response.cookies(cookieName).secured) {
          throw AssertionException(s"Cookie '$cookieName' is secured.")
        }
      }
    }
  }

  // body-...

  /**
   * Asserts that the body of the response is equal to the expected body.
   *
   * @param response     The HTTP response to check the body of.
   * @param expectedBody The expected body content as a string.
   * @return A Try[Unit] indicating success or containing an exception if the assertion fails.
   */
  private def assertBodyEquals(response: Response, expectedBody: String): Try[Unit] = Try {
    if (response.body != expectedBody) {
      throw AssertionException(s"Expected body to be $expectedBody, but got ${response.body}")
    }
  }

  /**
   * Asserts that the body of the response contains the expected content.
   *
   * @param response      The HTTP response to check the body of.
   * @param text          The expected text present in the response body as a string.
   * @return A Try[Unit] that is a Success if the body contains the expected text, and a Failure with an AssertionException otherwise.
   */
  private def assertBodyContainsText(response: Response, text: String): Try[Unit] = Try {
    if (!response.body.contains(text)) {
      val errMsg = s"Expected body to contains: '$text'. Content of body: '${response.body}'"
      Logger.error(errMsg)
      throw AssertionException(errMsg)
    }
  }

  /**
   * Asserts that the body of the response is empty.
   *
   * @param response The HTTP response to check the body of.
   * @return A Try[Unit] that is a Success if the body is empty, and a Failure with an AssertionException otherwise.
   */
  private def assertBodyIsEmpty(response: Response): Try[Unit] = Try {
    if (response.body.nonEmpty) {
      val errMsg = s"Expected body to be empty"
      Logger.error(errMsg)
      throw AssertionException(errMsg)
    }
  }

  /**
   * Asserts that the body of the response is not empty.
   *
   * @param response The HTTP response to check the body of.
   * @return A Try[Unit] that is a Success if the body is not empty, and a Failure with an AssertionException otherwise.
   */
  private def assertBodyIsNotEmpty(response: Response): Try[Unit] = Try {
    if (response.body.isEmpty) {
      val errMsg = s"Expected body to be not empty"
      Logger.error(errMsg)
      throw AssertionException(errMsg)
    }
  }

  /**
   * Asserts that the length of the response body is equal to the length of the expected body.
   *
   * @param response The HTTP response to check the body length of.
   * @param length   The expected body length to compare with.
   * @return A Try[Unit] indicating success or containing an exception if the assertion fails.
   */
  private def assertBodyLengthEquals(response: Response, length: String): Try[Unit] = Try {
    val expectedLength: Integer = length.toInt
    if (response.body.length != expectedLength) {
      val errMsg = s"Expected body length to be $expectedLength, but got ${response.body.length}"
      Logger.error(errMsg)
      throw AssertionException(errMsg)
    }
  }

  /**
   * Asserts that the body of the response starts with the expected prefix.
   *
   * @param response The HTTP response to check the body of.
   * @param prefix   The expected prefix of the response body.
   * @return A Try[Unit] indicating success or containing an exception if the assertion fails.
   */
  private def assertBodyStartsWith(response: Response, prefix: String): Try[Unit] = Try {
    Logger.debug(s"Asserting that the body of the response starts with $prefix")
    Logger.debug(s"Response body: ${response.body}")

    if (!response.body.startsWith(prefix)) {
      val errMsg = s"Expected body to start with $prefix"
      Logger.error(errMsg)
      throw AssertionException(errMsg)
    }
  }

  /**
   * Asserts that the body of the response ends with the expected suffix.
   *
   * @param response The HTTP response to check the body of.
   * @param suffix   The expected suffix of the response body.
   * @return A Try[Unit] indicating success or containing an exception if the assertion fails.
   */
  private def assertBodyEndsWith(response: Response, suffix: String): Try[Unit] = Try {
    if (!response.body.endsWith(suffix)) {
      val errMsg = s"Expected body to end with $suffix"
      Logger.error(errMsg)
      throw AssertionException(errMsg)
    }
  }

  /**
   * Asserts that the body of the response matches the provided regex pattern.
   *
   * @param response     The HTTP response to check the body of.
   * @param regexPattern The regex pattern to match against the response body.
   * @return A Try[Unit] indicating success or containing an exception if the assertion fails.
   */
  private def assertBodyMatchesRegex(response: Response, regexPattern: String): Try[Unit] = Try {
    val pattern = regexPattern.r

    if (pattern.findFirstIn(response.body).isEmpty) {
      val errMsg = s"Expected body to match regex pattern $regexPattern"
      Logger.error(errMsg)
      throw AssertionException(errMsg)
    }
  }

  // body-json-...

  /**
   * Asserts that the body of the response is a JSON array.
   *
   * @param response The HTTP response to check the body of.
   * @return A Try[Unit] indicating success or containing an exception if the assertion fails.
   */
  private def assertBodyIsJsonArray(response: Response): Try[Unit] = Try {
    val jsonAst = response.body.parseJson
    if (!jsonAst.isInstanceOf[JsArray]) {
      val errMsg = s"Expected the body to be a JSON array"
      Logger.error(errMsg)
      throw AssertionException(errMsg)
    }
  }

  /**
   * Asserts that the body of the response is a JSON object.
   *
   * @param response The HTTP response to check the body of.
   * @return A Try[Unit] indicating success or containing an exception if the assertion fails.
   */
  private def assertBodyIsJsonObject(response: Response): Try[Unit] = Try {
    val jsonAst = response.body.parseJson
    if (!jsonAst.isInstanceOf[JsObject]) {
      val errMsg = s"Expected the body to be a JSON object"
      Logger.error(errMsg)
      throw AssertionException(errMsg)
    }
  }

  /**
   * Asserts that the specified JSON path exists in the response body.
   *
   * @param response The HTTP response to check the body of.
   * @param jsonPath The JSON path to check for existence.
   * @return A Try[Unit] indicating success or containing an exception if the assertion fails.
   */
  private def assertBodyJsonPathExists(response: Response, jsonPath: String): Try[Unit] = Try {
    val configuration = Configuration.defaultConfiguration().addOptions(com.jayway.jsonpath.Option.SUPPRESS_EXCEPTIONS)
    val extractedValue = JsonPath.using(configuration).parse(response.body).read[Any](jsonPath)

    if (extractedValue == null) {
      val errMsg = s"Expected JSON path '$jsonPath' does not exist in the response body"
      Logger.error(errMsg)
      throw AssertionException(errMsg)
    }
  }
}
