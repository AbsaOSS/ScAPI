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

import africa.absa.testing.scapi.json.ResponseAction
import africa.absa.testing.scapi.rest.model.CookieValue
import africa.absa.testing.scapi.rest.response.action.AssertionResponseAction
import africa.absa.testing.scapi.rest.response.action.types.AssertResponseActionType.AssertResponseActionType
import africa.absa.testing.scapi.rest.response.action.types.{AssertResponseActionType, ResponseActionGroupType}
import africa.absa.testing.scapi.{ContentValidationFailedException, UndefinedResponseActionTypeException}
import munit.FunSuite

import scala.language.implicitConversions

class ResponseAssertionsTest extends FunSuite {

  implicit def assertResponseActionType2String(value: AssertResponseActionType): String = value.toString

  /*
    validateContent
   */

  // response-time-...

  test("validateContent - response time is below - limit is integer string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.RESPONSE_TIME_IS_BELOW, Map("limit" -> "200"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - response time is below - limit is not integer string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.RESPONSE_TIME_IS_BELOW, Map("limit" -> "not_integer"))
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: 'not_integer': Received value of 'ResponseAssertion.response-time-is-below.limit' cannot be parsed to a long: For input string: \"not_integer\"") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - response time is above - limit is integer string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.RESPONSE_TIME_IS_ABOVE, Map("limit" -> "200"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - response time is above - limit is not integer string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.RESPONSE_TIME_IS_ABOVE, Map("limit" -> "not_integer"))
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: 'not_integer': Received value of 'ResponseAssertion.response-time-is-above.limit' cannot be parsed to a long: For input string: \"not_integer\"") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - response time is above - missing limit parameter") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.RESPONSE_TIME_IS_ABOVE, Map.empty)
    interceptMessage[IllegalArgumentException]("Missing required 'limit' parameter for assertion response-time-is-above logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  // status-code-...

  test("validateContent - valid status code string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.STATUS_CODE_EQUALS, Map("code" -> "200"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - invalid status code string") {
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: 'not an integer': Received value of 'ResponseAssertion.status-code-equals.code' cannot be parsed to an integer: For input string: \"not an integer\"") {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.STATUS_CODE_EQUALS, Map("code" -> "not an integer")))
    }
  }

  test("validateContent - status code equals - missing code parameter") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.STATUS_CODE_EQUALS, Map.empty)
    interceptMessage[IllegalArgumentException]("Missing required 'code' parameter for assertion status-code-equals logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  // header-...

  test("validateContent - header exists - valid header name string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.HEADER_EXISTS, Map("headerName" -> "content-type"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - header exists - invalid header name string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.HEADER_EXISTS, Map("headerName" -> ""))
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ResponseAssertion.header-exists.headerName' is empty.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - header exists - missing header name parameter") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.HEADER_EXISTS, Map.empty)
    interceptMessage[IllegalArgumentException]("Missing required 'headerName' parameter for assertion header-exists logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - header value equals - valid header name and value strings") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.HEADER_VALUE_EQUALS, Map("headerName" -> "Content-Type", "expectedValue" -> "application/json"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - header value equals - invalid header name string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.HEADER_VALUE_EQUALS, Map("headerName" -> "", "expectedValue" -> "application/json"))
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ResponseAssertion.header-value-equals.headerName' is empty.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - header value equals - invalid expected value string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.HEADER_VALUE_EQUALS, Map("headerName" -> "Content-Type", "expectedValue" -> ""))
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ResponseAssertion.header-value-equals.expectedValue' is empty.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - header value equals - missing header name string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.HEADER_VALUE_EQUALS, Map("expectedValue" -> "application/json"))
    interceptMessage[IllegalArgumentException]("Missing required 'headerName' parameter for assertion header-value-equals logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - header value equals - missing header value string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.HEADER_VALUE_EQUALS, Map("headerName" -> "Content-Type"))
    interceptMessage[IllegalArgumentException]("Missing required 'expectedValue' parameter for assertion header-value-equals logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  // cookies-...

  test("validateContent - cookie exists - valid cookie name string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.COOKIE_EXISTS, Map("cookieName" -> "testCookie"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - cookie exists - invalid cookie name string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.COOKIE_EXISTS, Map("cookieName" -> ""))
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ResponseAssertion.cookie-exists.cookieName' is empty.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - cookie exists - missing cookie name parameter") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.COOKIE_EXISTS, Map())
    interceptMessage[IllegalArgumentException]("Missing required 'cookieName' parameter for assertion cookie-exists logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - cookie value equals - valid cookie name and value strings") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.COOKIE_VALUE_EQUALS, Map("cookieName" -> "testCookie", "expectedValue" -> "cookieValue"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - cookie value equals - invalid cookie name string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.COOKIE_VALUE_EQUALS, Map("cookieName" -> "", "expectedValue" -> "cookieValue"))
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ResponseAssertion.cookie-value-equals.cookieName' is empty.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - cookie value equals - invalid cookie value string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.COOKIE_VALUE_EQUALS, Map("cookieName" -> "testCookie", "expectedValue" -> ""))
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ResponseAssertion.cookie-value-equals.expectedValue' is empty.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - cookie value equals - missing cookie name string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.COOKIE_VALUE_EQUALS, Map("expectedValue" -> "cookieValue"))
    interceptMessage[IllegalArgumentException]("Missing required 'cookieName' parameter for assertion cookie-value-equals logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - cookie value equals - missing cookie value string") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.COOKIE_VALUE_EQUALS, Map("cookieName" -> "testCookie"))
    interceptMessage[IllegalArgumentException]("Missing required 'expectedValue' parameter for assertion cookie-value-equals logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  // body-...

  test("validateContent - body contains text - body is not empty") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.BODY_CONTAINS_TEXT, Map("text" -> "test content"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - body contains text - body is empty") {
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ResponseAssertion.body-contains-text.text' is empty.") {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.BODY_CONTAINS_TEXT, Map("text" -> "")))
    }
  }

  test("validateContent - body contains text - body parameter is missing") {
    interceptMessage[IllegalArgumentException]("Missing required 'text' parameter for assertion body-contains-text logic.") {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.BODY_CONTAINS_TEXT, Map.empty))
    }
  }

  test("validateContent - unsupported response action") {
    interceptMessage[UndefinedResponseActionTypeException]("Undefined response action content type: 'unsupported'") {
      AssertionResponseAction.validateContent(ResponseAction(group = ResponseActionGroupType.ASSERT, name = "unsupported", Map("body" -> "value")))
    }
  }

  /*
    performResponseAction
   */

  // response-time-...

  test("performAssertions - response time is below limit - success") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.RESPONSE_TIME_IS_BELOW, Map("limit" -> "100"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 99)

    assert(AssertionResponseAction.performResponseAction(response, statusCodeResponseAction).isSuccess)
  }

  test("performAssertions - response time is below limit - failed") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.RESPONSE_TIME_IS_BELOW, Map("limit" -> "100"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 101)

    assert(AssertionResponseAction.performResponseAction(response, statusCodeResponseAction).isFailure)
  }

  test("performAssertions - response time is above limit - success") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.RESPONSE_TIME_IS_ABOVE, Map("limit" -> "100"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 101)

    assert(AssertionResponseAction.performResponseAction(response, statusCodeResponseAction).isSuccess)
  }

  test("performAssertions - response time is above limit - failed") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.RESPONSE_TIME_IS_ABOVE, Map("limit" -> "100"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 99)

    assert(AssertionResponseAction.performResponseAction(response, statusCodeResponseAction).isFailure)
  }

  // status-code-...

  test("performAssertions - status code assertion - equals") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.STATUS_CODE_EQUALS, Map("code" -> "200"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, statusCodeResponseAction).isSuccess)
  }

  test("performAssertions - status code assertion - not equals") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.STATUS_CODE_EQUALS, Map("code" -> "200"))
    val response = Response(500, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, statusCodeResponseAction).isFailure)
  }

  test("performAssertions - status code - is success") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.STATUS_CODE_IS_SUCCESS, Map.empty)
    val response200 = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response299 = Response(299, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response200, statusCodeResponseAction).isSuccess)
    assert(AssertionResponseAction.performResponseAction(response299, statusCodeResponseAction).isSuccess)
  }

  test("performAssertions - status code - is not success") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.STATUS_CODE_IS_SUCCESS, Map.empty)
    val response199 = Response(199, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response300 = Response(300, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response500 = Response(500, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response199, statusCodeResponseAction).isFailure)
    assert(AssertionResponseAction.performResponseAction(response300, statusCodeResponseAction).isFailure)
    assert(AssertionResponseAction.performResponseAction(response500, statusCodeResponseAction).isFailure)
  }

  test("performAssertions - status code - is client error") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.STATUS_CODE_IS_CLIENT_ERROR, Map.empty)
    val response400 = Response(400, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response499 = Response(499, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response400, statusCodeResponseAction).isSuccess)
    assert(AssertionResponseAction.performResponseAction(response499, statusCodeResponseAction).isSuccess)
  }

  test("performAssertions - status code - is not client error") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.STATUS_CODE_IS_CLIENT_ERROR, Map.empty)
    val response399 = Response(399, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response500 = Response(500, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response200 = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response399, statusCodeResponseAction).isFailure)
    assert(AssertionResponseAction.performResponseAction(response500, statusCodeResponseAction).isFailure)
    assert(AssertionResponseAction.performResponseAction(response200, statusCodeResponseAction).isFailure)
  }

  test("performAssertions - status code - is server error") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.STATUS_CODE_IS_SERVER_ERROR, Map.empty)
    val response500 = Response(500, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response599 = Response(599, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response500, statusCodeResponseAction).isSuccess)
    assert(AssertionResponseAction.performResponseAction(response599, statusCodeResponseAction).isSuccess)
  }

  test("performAssertions - status code - is not server error") {
    val statusCodeResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.STATUS_CODE_IS_SERVER_ERROR, Map.empty)
    val response499 = Response(499, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response600 = Response(600, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response200 = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response499, statusCodeResponseAction).isFailure)
    assert(AssertionResponseAction.performResponseAction(response600, statusCodeResponseAction).isFailure)
    assert(AssertionResponseAction.performResponseAction(response200, statusCodeResponseAction).isFailure)
  }

  // header-...

  test("performAssertions - header exists") {
    val headerExistsResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.HEADER_EXISTS, Map("headerName" -> "Content-Type"))
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, headerExistsResponseAction).isSuccess)
  }

  test("performAssertions - header does not exists") {
    val headerExistsResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.HEADER_EXISTS, Map("headerName" -> "headerValue"))
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, headerExistsResponseAction).isFailure)
  }

  test("performAssertions - header value is equals") {
    val headerValueEqualsResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.HEADER_VALUE_EQUALS, Map("headerName" -> "Content-Type", "expectedValue" -> "application/json"))
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, headerValueEqualsResponseAction).isSuccess)
  }

  test("performAssertions - header value is not equals") {
    val headerValueEqualsResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.HEADER_VALUE_EQUALS, Map("headerName" -> "someName", "expectedValue" -> "someValue"))
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, headerValueEqualsResponseAction).isFailure)
  }

  // content-type-...

  test("performAssertions - content type is json") {
    val contentTypeIsJsonResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.CONTENT_TYPE_IS_JSON, Map.empty)
    val response = Response(200, "{}", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction).isSuccess)
  }

  test("performAssertions - content type is not json") {
    val contentTypeIsJsonResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.CONTENT_TYPE_IS_JSON, Map.empty)
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/xml")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction).isFailure)
  }

  test("performAssertions - content type is xml") {
    val contentTypeIsJsonResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.CONTENT_TYPE_IS_XML, Map.empty)
    val response = Response(200, """<?xml version="1.0" encoding="UTF-8"?><note><to>QA</to><from>Dev</from><body>Don't forget to test it!</body></note>""", "", "", Map("content-type" -> Seq("application/xml")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction).isSuccess)
  }

  test("performAssertions - content type is not xml") {
    val contentTypeIsJsonResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.CONTENT_TYPE_IS_XML, Map.empty)
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction).isFailure)
  }

  test("performAssertions - content type is html") {
    val contentTypeIsJsonResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.CONTENT_TYPE_IS_HTML, Map.empty)
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("text/html")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction).isSuccess)
  }

  test("performAssertions - content type is not html") {
    val contentTypeIsJsonResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.CONTENT_TYPE_IS_HTML, Map.empty)
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/xml")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction).isFailure)
  }

  // cookies-...

  test("performAssertions - cookie exists") {
    val cookieExistsResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.COOKIE_EXISTS, Map("cookieName" -> "testCookie"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map("testCookie" -> CookieValue(value = "", secured = false)), 100)

    assert(AssertionResponseAction.performResponseAction(response, cookieExistsResponseAction).isSuccess)
  }

  test("performAssertions - cookie does not exists") {
    val cookieExistsResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.COOKIE_EXISTS, Map("cookieName" -> "anotherCookie"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map("testCookie" -> CookieValue(value = "", secured = false)), 100)

    assert(AssertionResponseAction.performResponseAction(response, cookieExistsResponseAction).isFailure)
  }

  test("performAssertions - cookie value is equals") {
    val cookieValueEqualsResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.COOKIE_VALUE_EQUALS, Map("cookieName" -> "testCookie", "expectedValue" -> "cookieValue"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map("testCookie" -> CookieValue(value = "cookieValue", secured = false)), 100)

    assert(AssertionResponseAction.performResponseAction(response, cookieValueEqualsResponseAction).isSuccess)
  }

  test("performAssertions - cookie value is not equals") {
    val cookieValueEqualsResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.COOKIE_VALUE_EQUALS, Map("cookieName" -> "testCookie", "expectedValue" -> "cookieValue"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map("testCookie" -> CookieValue(value = "anotherValue", secured = false)), 100)

    assert(AssertionResponseAction.performResponseAction(response, cookieValueEqualsResponseAction).isFailure)
  }

  test("performAssertions - cookie value equals - cookie does not exist") {
    val cookieValueEqualsResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.COOKIE_VALUE_EQUALS, Map("cookieName" -> "testCookie", "expectedValue" -> "cookieValue"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, cookieValueEqualsResponseAction).isFailure)
  }

  test("performAssertions - cookie is secured") {
    val cookieIsSecuredResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.COOKIE_IS_SECURED, Map("cookieName" -> "securedCookie"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map("securedCookie" -> CookieValue(value = "someValue", secured = true)), 100)

    assert(AssertionResponseAction.performResponseAction(response, cookieIsSecuredResponseAction).isSuccess)
  }

  test("performAssertions - cookie is secured - cookie does not exist") {
    val cookieIsSecuredResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.COOKIE_IS_SECURED, Map("cookieName" -> "testCookie", "expectedValue" -> "cookieValue"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, cookieIsSecuredResponseAction).isFailure)
  }

  test("performAssertions - cookie is not secured") {
    val cookieIsNotSecuredResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.COOKIE_IS_NOT_SECURED, Map("cookieName" -> "notSecuredCookie"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map("notSecuredCookie" -> CookieValue(value = "someValue", secured = false)), 100)

    assert(AssertionResponseAction.performResponseAction(response, cookieIsNotSecuredResponseAction).isSuccess)
  }

  test("performAssertions - cookie is not secured - cookie does not exist") {
    val cookieIsNotSecuredResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.COOKIE_IS_NOT_SECURED, Map("cookieName" -> "testCookie", "expectedValue" -> "cookieValue"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, cookieIsNotSecuredResponseAction).isFailure)
  }

  // body-...

  test("performAssertions - body contains assertion") {
    val bodyContainsTextResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.BODY_CONTAINS_TEXT, Map("text" -> "dummy"))
    val response = Response(200, "This is a dummy body", "", "", Map.empty, Map.empty, 100)
    assert(AssertionResponseAction.performResponseAction(response, bodyContainsTextResponseAction).isSuccess)
  }

  test("performAssertions - body does not contains assertion") {
    val bodyContainsTextResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = AssertResponseActionType.BODY_CONTAINS_TEXT, Map("text" -> "dummies"))
    val response = Response(200, "This is a dummy body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, bodyContainsTextResponseAction).isFailure)
  }

  // unsupported

  test("performAssertions - unsupported assertion") {
    val unsupportedResponseAction = ResponseAction(group = ResponseActionGroupType.ASSERT, name = "unsupported-assertion", Map("nonsense" -> "value"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, unsupportedResponseAction).isFailure)
  }
}
