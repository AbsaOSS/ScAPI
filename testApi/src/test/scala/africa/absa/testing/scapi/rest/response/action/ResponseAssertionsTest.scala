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
import africa.absa.testing.scapi.rest.model.CookieValue
import africa.absa.testing.scapi.rest.response.Response
import africa.absa.testing.scapi.rest.response.action.types.AssertResponseActionType.AssertResponseActionType
import africa.absa.testing.scapi.rest.response.action.types.{AssertResponseActionType, ResponseActionGroupType}
import africa.absa.testing.scapi.{ContentValidationFailedException, UndefinedResponseActionTypeException}
import munit.FunSuite

import scala.language.implicitConversions
import scala.util.Failure

class ResponseAssertionsTest extends FunSuite {

  implicit def assertResponseActionType2String(value: AssertResponseActionType): String = value.toString

  val jsonResponse: String =
    """
  [
      {
          "id": 1,
          "name": "radiology"
      },
      {
          "id": 2,
          "name": "surgery"
      },
      {
          "id": 3,
          "name": "dentistry"
      }
  ]
  """

  /*
    validateContent
   */

  // response-time-...

  test("validateContent - response time is below - limit is integer string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.ResponseTimeIsBelow, Map("limit" -> "200"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - response time is below - limit is not integer string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.ResponseTimeIsBelow, Map("limit" -> "not_integer"))
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: 'not_integer': Received value of 'ResponseAssertion.response-time-is-below.limit' cannot be parsed to a long: For input string: \"not_integer\"") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - response time is above - limit is integer string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.ResponseTimeIsAbove, Map("limit" -> "200"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - response time is above - limit is not integer string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.ResponseTimeIsAbove, Map("limit" -> "not_integer"))
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: 'not_integer': Received value of 'ResponseAssertion.response-time-is-above.limit' cannot be parsed to a long: For input string: \"not_integer\"") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - response time is above - missing limit parameter") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.ResponseTimeIsAbove, Map.empty)
    interceptMessage[IllegalArgumentException]("Missing required 'limit' parameter for assertion response-time-is-above logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  // status-code-...

  test("validateContent - valid status code string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.StatusCodeEquals, Map("code" -> "200"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - invalid status code string") {
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: 'not an integer': Received value of 'ResponseAssertion.status-code-equals.code' cannot be parsed to an integer: For input string: \"not an integer\"") {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.StatusCodeEquals, Map("code" -> "not an integer")))
    }
  }

  test("validateContent - status code equals - missing code parameter") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.StatusCodeEquals, Map.empty)
    interceptMessage[IllegalArgumentException]("Missing required 'code' parameter for assertion status-code-equals logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  // header-...

  test("validateContent - header exists - valid header name string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.HeaderExists, Map("headerName" -> "content-type"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - header exists - invalid header name string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.HeaderExists, Map("headerName" -> ""))
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ResponseAssertion.header-exists.headerName' is empty.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - header exists - missing header name parameter") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.HeaderExists, Map.empty)
    interceptMessage[IllegalArgumentException]("Missing required 'headerName' parameter for assertion header-exists logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - header value equals - valid header name and value strings") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.HeaderValueEquals, Map("headerName" -> "Content-Type", "expectedValue" -> "application/json"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - header value equals - invalid header name string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.HeaderValueEquals, Map("headerName" -> "", "expectedValue" -> "application/json"))
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ResponseAssertion.header-value-equals.headerName' is empty.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - header value equals - invalid expected value string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.HeaderValueEquals, Map("headerName" -> "Content-Type", "expectedValue" -> ""))
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ResponseAssertion.header-value-equals.expectedValue' is empty.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - header value equals - missing header name string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.HeaderValueEquals, Map("expectedValue" -> "application/json"))
    interceptMessage[IllegalArgumentException]("Missing required 'headerName' parameter for assertion header-value-equals logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - header value equals - missing header value string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.HeaderValueEquals, Map("headerName" -> "Content-Type"))
    interceptMessage[IllegalArgumentException]("Missing required 'expectedValue' parameter for assertion header-value-equals logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  // cookies-...

  test("validateContent - cookie exists - valid cookie name string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.CookieExists, Map("cookieName" -> "testCookie"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - cookie exists - invalid cookie name string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.CookieExists, Map("cookieName" -> ""))
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ResponseAssertion.cookie-exists.cookieName' is empty.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - cookie exists - missing cookie name parameter") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.CookieExists, Map())
    interceptMessage[IllegalArgumentException]("Missing required 'cookieName' parameter for assertion cookie-exists logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - cookie value equals - valid cookie name and value strings") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.CookieValueEquals, Map("cookieName" -> "testCookie", "expectedValue" -> "cookieValue"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - cookie value equals - invalid cookie name string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.CookieValueEquals, Map("cookieName" -> "", "expectedValue" -> "cookieValue"))
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ResponseAssertion.cookie-value-equals.cookieName' is empty.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - cookie value equals - invalid cookie value string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.CookieValueEquals, Map("cookieName" -> "testCookie", "expectedValue" -> ""))
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ResponseAssertion.cookie-value-equals.expectedValue' is empty.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - cookie value equals - missing cookie name string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.CookieValueEquals, Map("expectedValue" -> "cookieValue"))
    interceptMessage[IllegalArgumentException]("Missing required 'cookieName' parameter for assertion cookie-value-equals logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - cookie value equals - missing cookie value string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.CookieValueEquals, Map("cookieName" -> "testCookie"))
    interceptMessage[IllegalArgumentException]("Missing required 'expectedValue' parameter for assertion cookie-value-equals logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  // body-...

  test("validateContent - body equals - equals") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyEquals, Map("body" -> "body content"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - body equals - not equals") {
    intercept[ContentValidationFailedException] {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyEquals, Map("body" -> "")))
    }
  }

  test("validateContent - body equals - body parameter is missing") {
    interceptMessage[IllegalArgumentException](s"Missing required 'body' parameter for assertion ${AssertResponseActionType.BodyEquals} logic.") {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyEquals, Map.empty))
    }
  }

  test("validateContent - body contains text - body is not empty") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyContainsText, Map("text" -> "test content"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - body contains text - body is empty") {
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ResponseAssertion.body-contains-text.text' is empty.") {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyContainsText, Map("text" -> "")))
    }
  }

  test("validateContent - body contains text - body parameter is missing") {
    interceptMessage[IllegalArgumentException]("Missing required 'text' parameter for assertion body-contains-text logic.") {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyContainsText, Map.empty))
    }
  }

  test("validateContent - body length equals - length is not empty") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyLengthEquals, Map("length" -> "123"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - body length equals - length is long string") {
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: 'long': Received value of 'ResponseAssertion.body-length-equals.length' cannot be parsed to a long: For input string: \"long\"") {
      val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyLengthEquals, Map("length" -> "long"))
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - body length equals - length is empty") {
    intercept[ContentValidationFailedException] {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyLengthEquals, Map("length" -> "")))
    }
  }

  test("validateContent - body length equals - length parameter is missing") {
    interceptMessage[IllegalArgumentException](s"Missing required 'length' parameter for assertion ${AssertResponseActionType.BodyLengthEquals} logic.") {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyLengthEquals, Map.empty))
    }
  }

  test("validateContent - body starts with - prefix is not empty") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyStartsWith, Map("prefix" -> "body starts with this"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - body starts with - prefix is empty") {
    intercept[ContentValidationFailedException] {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyStartsWith, Map("prefix" -> "")))
    }
  }

  test("validateContent - body starts with - prefix parameter is missing") {
    interceptMessage[IllegalArgumentException](s"Missing required 'prefix' parameter for assertion ${AssertResponseActionType.BodyStartsWith} logic.") {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyStartsWith, Map.empty))
    }
  }

  test("validateContent - body ends with - suffix is not empty") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyEndsWith, Map("suffix" -> "body ends with this"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - body ends with - suffix is empty") {
    intercept[ContentValidationFailedException] {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyEndsWith, Map("suffix" -> "")))
    }
  }

  test("validateContent - body ends with - suffix parameter is missing") {
    interceptMessage[IllegalArgumentException](s"Missing required 'suffix' parameter for assertion ${AssertResponseActionType.BodyEndsWith} logic.") {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyEndsWith, Map.empty))
    }
  }

  test("validateContent - body matches regex - regex is not empty") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> "pattern"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - body matches regex - regex is empty") {
    intercept[ContentValidationFailedException] {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> "")))
    }
  }

  test("validateContent - body matches regex - regex parameter is missing") {
    interceptMessage[IllegalArgumentException](s"Missing required 'regex' parameter for assertion ${AssertResponseActionType.BodyMatchesRegex} logic.") {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map.empty))
    }
  }

  // body-json-...

  test("validateContent - body json path exists - json path is not empty") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyJsonPathExists, Map("jsonPath" -> "pattern"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - body json path exists - json path is empty") {
    intercept[ContentValidationFailedException] {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyJsonPathExists, Map("jsonPath" -> "")))
    }
  }

  test("validateContent - body json path exists - json path parameter is missing") {
    interceptMessage[IllegalArgumentException](s"Missing required 'jsonPath' parameter for assertion ${AssertResponseActionType.BodyJsonPathExists} logic.") {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyJsonPathExists, Map.empty))
    }
  }

  // unsupported

  test("validateContent - unsupported response action") {
    interceptMessage[UndefinedResponseActionTypeException]("Undefined response action content type: 'unsupported'") {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.Assert, name = "unsupported", Map("body" -> "value")))
    }
  }

  /*
    performResponseAction
   */

  // response-time-...

  test("performAssertions - response time is below limit - success") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.ResponseTimeIsBelow, Map("limit" -> "100"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 99)

    assert(AssertionResponseAction.performResponseAction(response, statusCodeResponseAction).isSuccess)
  }

  test("performAssertions - response time is below limit - failed") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.ResponseTimeIsBelow, Map("limit" -> "100"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 101)

    val result = AssertionResponseAction.performResponseAction(response, statusCodeResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Expected maximal length '100' is smaller then received '101' one.")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - response time is above limit - success") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.ResponseTimeIsAbove, Map("limit" -> "100"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 101)

    assert(AssertionResponseAction.performResponseAction(response, statusCodeResponseAction).isSuccess)
  }

  test("performAssertions - response time is above limit - failed") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.ResponseTimeIsAbove, Map("limit" -> "100"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 99)

    val result = AssertionResponseAction.performResponseAction(response, statusCodeResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Expected minimal length '100' is bigger then received '99' one.")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  // status-code-...

  test("performAssertions - status code assertion - equals") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.StatusCodeEquals, Map("code" -> "200"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, statusCodeResponseAction).isSuccess)
  }

  test("performAssertions - status code assertion - not equals") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.StatusCodeEquals, Map("code" -> "200"))
    val response = Response(500, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, statusCodeResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Expected 200, but got 500")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - status code - is success") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.StatusCodeIsSuccess, Map.empty)
    val response200 = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response299 = Response(299, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response200, statusCodeResponseAction).isSuccess)
    assert(AssertionResponseAction.performResponseAction(response299, statusCodeResponseAction).isSuccess)
  }

  test("performAssertions - status code - is not success") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.StatusCodeIsSuccess, Map.empty)
    val response199 = Response(199, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response300 = Response(300, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response500 = Response(500, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    val result1 = AssertionResponseAction.performResponseAction(response199, statusCodeResponseAction)
    result1 match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Received status code '199' is not in expected range (200 - 299).")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }

    val result2 = AssertionResponseAction.performResponseAction(response300, statusCodeResponseAction)
    result2 match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Received status code '300' is not in expected range (200 - 299).")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }

    val result3 = AssertionResponseAction.performResponseAction(response500, statusCodeResponseAction)
    result3 match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Received status code '500' is not in expected range (200 - 299).")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - status code - is client error") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.StatusCodeIsClientError, Map.empty)
    val response400 = Response(400, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response499 = Response(499, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response400, statusCodeResponseAction).isSuccess)
    assert(AssertionResponseAction.performResponseAction(response499, statusCodeResponseAction).isSuccess)
  }

  test("performAssertions - status code - is not client error") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.StatusCodeIsClientError, Map.empty)
    val response399 = Response(399, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response500 = Response(500, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response200 = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    val result1 = AssertionResponseAction.performResponseAction(response399, statusCodeResponseAction)
    result1 match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Received status code '399' is not in expected range (400 - 499).")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }

    val result2 = AssertionResponseAction.performResponseAction(response500, statusCodeResponseAction)
    result2 match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Received status code '500' is not in expected range (400 - 499).")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }

    val result3 = AssertionResponseAction.performResponseAction(response200, statusCodeResponseAction)
    result3 match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Received status code '200' is not in expected range (400 - 499).")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - status code - is server error") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.StatusCodeIsServerError, Map.empty)
    val response500 = Response(500, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response599 = Response(599, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response500, statusCodeResponseAction).isSuccess)
    assert(AssertionResponseAction.performResponseAction(response599, statusCodeResponseAction).isSuccess)
  }

  test("performAssertions - status code - is not server error") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.StatusCodeIsServerError, Map.empty)
    val response499 = Response(499, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response600 = Response(600, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response200 = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    val result1 = AssertionResponseAction.performResponseAction(response499, statusCodeResponseAction)
    result1 match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Received status code '499' is not in expected range (500 - 599).")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }

    val result2 = AssertionResponseAction.performResponseAction(response600, statusCodeResponseAction)
    result2 match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Received status code '600' is not in expected range (500 - 599).")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }

    val result3 = AssertionResponseAction.performResponseAction(response200, statusCodeResponseAction)
    result3 match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Received status code '200' is not in expected range (500 - 599).")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  // header-...

  test("performAssertions - header exists") {
    val headerExistsResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.HeaderExists, Map("headerName" -> "Content-Type"))
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, headerExistsResponseAction).isSuccess)
  }

  test("performAssertions - header does not exists") {
    val headerExistsResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.HeaderExists, Map("headerName" -> "headerValue"))
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, headerExistsResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Expected header 'headerValue' not found.")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - header value is equals") {
    val headerValueEqualsResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.HeaderValueEquals, Map("headerName" -> "Content-Type", "expectedValue" -> "application/json"))
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, headerValueEqualsResponseAction).isSuccess)
  }

  test("performAssertions - header value is not equals") {
    val headerValueEqualsResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.HeaderValueEquals, Map("headerName" -> "someName", "expectedValue" -> "someValue"))
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, headerValueEqualsResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Expected header 'someName' not found.")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  // content-type-...

  test("performAssertions - content type is json") {
    val contentTypeIsJsonResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.ContentTypeIsJson, Map.empty)
    val response = Response(200, "{}", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction).isSuccess)
  }

  test("performAssertions - content type is not json") {
    val contentTypeIsJsonResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.ContentTypeIsJson, Map.empty)
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/xml")), Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Received content is not JSON type.")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - content type is xml") {
    val contentTypeIsJsonResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.ContentTypeIsXml, Map.empty)
    val response = Response(200, """<?xml version="1.0" encoding="UTF-8"?><note><to>QA</to><from>Dev</from><body>Don't forget to test it!</body></note>""", "", "", Map("content-type" -> Seq("application/xml")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction).isSuccess)
  }

  test("performAssertions - content type is not xml") {
    val contentTypeIsJsonResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.ContentTypeIsXml, Map.empty)
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Received content is not XML type.")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - content type is html") {
    val contentTypeIsJsonResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.ContentTypeIsHtml, Map.empty)
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("text/html")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction).isSuccess)
  }

  test("performAssertions - content type is not html") {
    val contentTypeIsJsonResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.ContentTypeIsHtml, Map.empty)
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/xml")), Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Received content is not HTML type. Details: Assertion failed: Expected header 'content-type' value 'text/html' is not equal to received header value 'application/xml'.")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  // cookies-...

  test("performAssertions - cookie exists") {
    val cookieExistsResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.CookieExists, Map("cookieName" -> "testCookie"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map("testCookie" -> CookieValue(value = "", secured = false)), 100)

    assert(AssertionResponseAction.performResponseAction(response, cookieExistsResponseAction).isSuccess)
  }

  test("performAssertions - cookie does not exists") {
    val cookieExistsResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.CookieExists, Map("cookieName" -> "anotherCookie"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map("testCookie" -> CookieValue(value = "", secured = false)), 100)

    val result = AssertionResponseAction.performResponseAction(response, cookieExistsResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Cookie 'anotherCookie' does not exist in the response.")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - cookie value is equals") {
    val cookieValueEqualsResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.CookieValueEquals, Map("cookieName" -> "testCookie", "expectedValue" -> "cookieValue"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map("testCookie" -> CookieValue(value = "cookieValue", secured = false)), 100)

    assert(AssertionResponseAction.performResponseAction(response, cookieValueEqualsResponseAction).isSuccess)
  }

  test("performAssertions - cookie value is not equals") {
    val cookieValueEqualsResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.CookieValueEquals, Map("cookieName" -> "testCookie", "expectedValue" -> "cookieValue"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map("testCookie" -> CookieValue(value = "anotherValue", secured = false)), 100)

    val result = AssertionResponseAction.performResponseAction(response, cookieValueEqualsResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Cookie 'testCookie' value does not match expected value 'cookieValue'.")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - cookie value equals - cookie does not exist") {
    val cookieValueEqualsResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.CookieValueEquals, Map("cookieName" -> "testCookie", "expectedValue" -> "cookieValue"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, cookieValueEqualsResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Cookie 'testCookie' does not exist in the response.")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - cookie is secured") {
    val cookieIsSecuredResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.CookieIsSecured, Map("cookieName" -> "securedCookie"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map("securedCookie" -> CookieValue(value = "someValue", secured = true)), 100)

    assert(AssertionResponseAction.performResponseAction(response, cookieIsSecuredResponseAction).isSuccess)
  }

  test("performAssertions - cookie is secured - cookie does not exist") {
    val cookieIsSecuredResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.CookieIsSecured, Map("cookieName" -> "testCookie", "expectedValue" -> "cookieValue"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, cookieIsSecuredResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Cookie 'testCookie' does not exist in the response.")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - cookie is not secured") {
    val cookieIsNotSecuredResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.CookieIsNotSecured, Map("cookieName" -> "notSecuredCookie"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map("notSecuredCookie" -> CookieValue(value = "someValue", secured = false)), 100)

    assert(AssertionResponseAction.performResponseAction(response, cookieIsNotSecuredResponseAction).isSuccess)
  }

  test("performAssertions - cookie is not secured - cookie does not exist") {
    val cookieIsNotSecuredResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.CookieIsNotSecured, Map("cookieName" -> "testCookie", "expectedValue" -> "cookieValue"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, cookieIsNotSecuredResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Cookie 'testCookie' does not exist in the response.")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  // body-...

  test("performAssertions - body equals") {
    val bodyContainsTextResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyEquals, Map("body" -> "This is a dummy body"))
    val response = Response(200, "This is a dummy body", "", "", Map.empty, Map.empty, 100)
    assert(AssertionResponseAction.performResponseAction(response, bodyContainsTextResponseAction).isSuccess)
  }

  test("performAssertions - body not equals") {
    val bodyContainsTextResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyEquals, Map("body" -> "This is a dummy body"))
    val response = Response(200, "This is another dummy body", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, bodyContainsTextResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Expected body to be This is a dummy body, but got This is another dummy body")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - body contains assertion") {
    val bodyContainsTextResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyContainsText, Map("text" -> "dummy"))
    val response = Response(200, "This is a dummy body", "", "", Map.empty, Map.empty, 100)
    assert(AssertionResponseAction.performResponseAction(response, bodyContainsTextResponseAction).isSuccess)
  }

  test("performAssertions - body does not contains assertion") {
    val bodyContainsTextResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyContainsText, Map("text" -> "dummies"))
    val response = Response(200, "This is a dummy body", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, bodyContainsTextResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Expected body to contains: 'dummies'. Content of body: 'This is a dummy body'")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - body is empty - is empty") {
    val emptyBodyResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyIsEmpty, Map.empty)
    val response = Response(200, "", "", "", Map.empty, Map.empty, 100)
    assert(AssertionResponseAction.performResponseAction(response, emptyBodyResponseAction).isSuccess)
  }

  test("performAssertions - body is empty - is not empty") {
    val nonEmptyBodyResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyIsEmpty, Map.empty)
    val response = Response(200, "This is a dummy body", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, nonEmptyBodyResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Expected body to be empty")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - body is not empty - is not empty") {
    val nonEmptyBodyResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyIsNotEmpty, Map.empty)
    val response = Response(200, "This is a dummy body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, nonEmptyBodyResponseAction).isSuccess)
  }

  test("performAssertions - body is not empty - is empty") {
    val emptyBodyResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyIsNotEmpty, Map.empty)
    val response = Response(200, "", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, emptyBodyResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Expected body to be not empty")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - body length equals - equals") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyLengthEquals, Map("length" -> "20"))
    val response = Response(200, "This is a dummy body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  test("performAssertions - body length equals - not equals") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyLengthEquals, Map("length" -> "101"))
    val response = Response(200, "", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, responseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Expected body length to be 101, but got 0")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }


  test("performAssertions - body starts with - success") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyStartsWith, Map("prefix" -> "This is"))
    val response = Response(200, "This is a dummy body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  test("performAssertions - body starts with - fail") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyStartsWith, Map("prefix" -> "This is not"))
    val response = Response(200, "This is a dummy body", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, responseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Expected body to start with This is not")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - body ends with - success") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyEndsWith, Map("suffix" -> "y body"))
    val response = Response(200, "This is a dummy body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  test("performAssertions - body ends with - fail") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyEndsWith, Map("suffix" -> "clever body"))
    val response = Response(200, "This is a dummy body", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, responseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Expected body to end with clever body")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - body matches regex - matches") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> """"name": "radiology""""))
    val response = Response(200, jsonResponse, "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  test("performAssertions - body matches regex - not matches case sensitive") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> """"name": "Radiology""""))
    val response = Response(200, jsonResponse, "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, responseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Expected body to match regex pattern \"name\": \"Radiology\"")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - body matches regex - not matches - not present") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> """"name": "cardiology""""))
    val response = Response(200, jsonResponse, "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, responseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Expected body to match regex pattern \"name\": \"cardiology\"")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - body matches regex - matches special characters") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> "sur\\*ery"))
    val response = Response(200, jsonResponse.replace("surgery", "sur*ery"), "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  test("performAssertions - body matches regex - matches numerical value") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> """"id": \d"""))
    val response = Response(200, jsonResponse, "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  test("performAssertions - body matches regex - matches multiple occurrences") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> """"name": ".*y""""))
    val response = Response(200, jsonResponse, "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  test("performAssertions - body matches regex - matches stand alone word") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> ("\\b" + "surgery" + "\\b")))
    val response = Response(200, jsonResponse, "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  test("performAssertions - body matches regex - matches special JSON characters") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> "\\{\\[,:\\]\\}"))
    val response = Response(200, """{"data": "{[,:]}" }""", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  test("performAssertions - body matches regex - matches empty JSON elements") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> """\"emptyObject\": \{\}"""))
    val response = Response(200, """{"emptyObject": {}, "emptyArray": []}""", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  test("performAssertions - body matches regex - matches null values in JSON") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> """\"key\": null"""))
    val response = Response(200, """{"key": null}""", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  test("performAssertions - body matches regex - matches Unicode characters") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> "こんにちは"))
    val response = Response(200, """{"unicode": "こんにちは"}""", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  test("performAssertions - body matches regex - matches deeply nested JSON structures") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> """\"level3\": \"value\""""))
    val response = Response(200, """{"level1": {"level2": {"level3": "value"}}}""", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  test("performAssertions - body matches regex - not matches invalid JSON") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> """\"key\": \"value\"""""))
    val response = Response(200, """"{"key": "value"""", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, responseAction)
    result match {
      case Failure(exception) =>
        val expectedMessage = "Assertion failed: Expected body to match regex pattern \\\"key\\\": \\\"value\\\"\""
        assert(clue(exception.getMessage) == clue(expectedMessage))
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - body matches regex - matches escape characters") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> "line break:\\\\nAnd a tab:\\\\tEnd"))
    val response = Response(200, """{"text": "This is a line break:\nAnd a tab:\tEnd."}""", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  test("performAssertions - body matches regex - matches non-string JSON values") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> """\"boolean\": true"""))
    val response = Response(200, """{"boolean": true, "number": 1234}""", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  test("performAssertions - body matches regex - matches regex meta characters") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyMatchesRegex, Map("regex" -> "\\^\\$\\.\\*\\+\\?\\(\\)\\[\\]\\{\\}\\|"))
    val response = Response(200, """{"data": "^$.*+?()[]{}|"}""", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  // body-json-...

  test("performAssertions - body is json array - success") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyJsonIsJsonArray, Map.empty)
    val response = Response(200, "[\n  \"item1\",\n  \"item2\"\n]", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  test("performAssertions - body is json array - fail with object") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyJsonIsJsonArray, Map.empty)
    val responseWithArray = Response(200, "{\n  \"key\": \"value\"\n}", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(responseWithArray, responseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Expected the body to be a JSON array")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - body is json array - fail with string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyJsonIsJsonArray, Map.empty)
    val responseWithString = Response(200, "This is a dummy body", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(responseWithString, responseAction)
    result match {
      case Failure(exception) =>
        val expectedMessage =
          """|Unexpected character 'T' at input index 0 (line 1, position 1), expected JSON Value:
             |This is a dummy body
             |^
             |""".stripMargin
        assert(clue(exception.getMessage) == clue(expectedMessage))
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - body is json object - success") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyJsonIsJsonObject, Map.empty)
    val response = Response(200, "{\n  \"key\": \"value\"\n}", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, responseAction).isSuccess)
  }

  test("performAssertions - body is json object - fail with array") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyJsonIsJsonObject, Map.empty)
    val responseWithObject = Response(200, "[\n  \"item1\",\n  \"item2\"\n]", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(responseWithObject, responseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Expected the body to be a JSON object")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - body is json object - fail with string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyJsonIsJsonObject, Map.empty)
    val responseWithString = Response(200, "This is a dummy body", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(responseWithString, responseAction)
    result match {
      case Failure(exception) =>
        val expectedMessage =
          """|Unexpected character 'T' at input index 0 (line 1, position 1), expected JSON Value:
             |This is a dummy body
             |^
             |""".stripMargin
        assert(clue(exception.getMessage) == clue(expectedMessage))
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  test("performAssertions - body json path exists - success") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyJsonPathExists, Map("jsonPath" -> "$.user.name"))
    val responseWithObject = Response(200, """{ "user": { "name": "John", "age": 30 } }""", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(responseWithObject, responseAction).isSuccess)
  }

  test("performAssertions - body json path exists - fail") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = AssertResponseActionType.BodyJsonPathExists, Map("jsonPath" -> "$.user.address"))
    val responseWithObject = Response(200, """{ "user": { "name": "John", "age": 30 } }""", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(responseWithObject, responseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Assertion failed: Expected JSON path '$.user.address' does not exist in the response body")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }

  // unsupported

  test("performAssertions - unsupported assertion") {
    val unsupportedResponseAction = ResponseAction(group = ResponseActionGroupType.Assert, name = "unsupported-assertion", Map("nonsense" -> "value"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    val result = AssertionResponseAction.performResponseAction(response, unsupportedResponseAction)
    result match {
      case Failure(exception) =>
        assert(clue(exception.getMessage) == "Undefined response action content type: 'Unsupported assertion method [group: assert]: unsupported-assertion'")
      case _ =>
        fail("Expected a failure of test but test Passed")
    }
  }
}
