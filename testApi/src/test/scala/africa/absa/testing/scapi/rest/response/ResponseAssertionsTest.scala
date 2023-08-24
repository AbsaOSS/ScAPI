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
import africa.absa.testing.scapi.{ContentValidationFailed, UndefinedResponseActionType}
import munit.FunSuite

class ResponseAssertionsTest extends FunSuite {

  /*
    validateContent
   */

  // response-time-...

  test("validateContent - response time is below - limit is integer string") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.RESPONSE_TIME_IS_BELOW}", Map("limit" -> "200"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - response time is below - limit is not integer string") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.RESPONSE_TIME_IS_BELOW}", Map("limit" -> "not_integer"))
    interceptMessage[ContentValidationFailed]("Content validation failed for value: 'not_integer': Received value of 'ResponseAssertion.response-time-is-below.limit' cannot be parsed to a long: For input string: \"not_integer\"") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - response time is above - limit is integer string") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.RESPONSE_TIME_IS_ABOVE}", Map("limit" -> "200"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - response time is above - limit is not integer string") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.RESPONSE_TIME_IS_ABOVE}", Map("limit" -> "not_integer"))
    interceptMessage[ContentValidationFailed]("Content validation failed for value: 'not_integer': Received value of 'ResponseAssertion.response-time-is-above.limit' cannot be parsed to a long: For input string: \"not_integer\"") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - response time is above - missing limit parameter") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.RESPONSE_TIME_IS_ABOVE}", Map.empty)
    interceptMessage[IllegalArgumentException]("Missing required 'limit' parameter for assertion response-time-is-above logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  // status-code-...

  test("validateContent - valid status code string") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_EQUALS}", Map("code" -> "200"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - invalid status code string") {
    intercept[ContentValidationFailed] {
      AssertionResponseAction.validateContent(ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_EQUALS}", Map("code" -> "not an integer")))
    }
  }

  test("validateContent - status code equals - missing code parameter") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_EQUALS}", Map.empty)
    interceptMessage[IllegalArgumentException]("Missing required 'code' parameter for assertion status-code-equals logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  // header-...

  test("validateContent - header exists - valid header name string") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.HEADER_EXISTS}", Map("headerName" -> "content-type"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - header exists - invalid header name string") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.HEADER_EXISTS}", Map("headerName" -> ""))
    interceptMessage[ContentValidationFailed]("Content validation failed for value: '': Received string value of 'ResponseAssertion.header-exists.headerName' is empty.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - header exists - missing header name parameter") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.HEADER_EXISTS}", Map.empty)
    interceptMessage[IllegalArgumentException]("Missing required 'headerName' parameter for assertion header-exists logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - header value equals - valid header name and value strings") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.HEADER_VALUE_EQUALS}", Map("headerName" -> "Content-Type", "expectedValue" -> "application/json"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - header value equals - invalid header name string") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.HEADER_VALUE_EQUALS}", Map("headerName" -> "", "expectedValue" -> "application/json"))
    interceptMessage[ContentValidationFailed]("Content validation failed for value: '': Received string value of 'ResponseAssertion.header-value-equals.headerName' is empty.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - header value equals - invalid expected value string") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.HEADER_VALUE_EQUALS}", Map("headerName" -> "Content-Type", "expectedValue" -> ""))
    interceptMessage[ContentValidationFailed]("Content validation failed for value: '': Received string value of 'ResponseAssertion.header-value-equals.expectedValue' is empty.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - header value equals - missing header name string") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.HEADER_VALUE_EQUALS}", Map("expectedValue" -> "application/json"))
    interceptMessage[IllegalArgumentException]("Missing required 'headerName' parameter for assertion header-value-equals logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - header value equals - missing header value string") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.HEADER_VALUE_EQUALS}", Map("headerName" -> "Content-Type"))
    interceptMessage[IllegalArgumentException]("Missing required 'expectedValue' parameter for assertion header-value-equals logic.") {
      AssertionResponseAction.validateContent(responseAction)
    }
  }

  // body-...

  test("validateContent - body is not empty") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.BODY_CONTAINS}", Map("body" -> "test content"))
    AssertionResponseAction.validateContent(responseAction)
  }

  test("validateContent - body is empty") {
    intercept[ContentValidationFailed] {
      AssertionResponseAction.validateContent(ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.BODY_CONTAINS}", Map("body" -> "")))
    }
  }


  test("validateContent - unsupported response action") {
    intercept[UndefinedResponseActionType] {
      AssertionResponseAction.validateContent(ResponseAction(method = s"${Response.GROUP_ASSERT}.unsupported", Map("body" -> "value")))
    }
  }

  /*
    performResponseAction
   */

  // response-time-...

  test("performAssertions - response time is below limit - success") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.RESPONSE_TIME_IS_BELOW}", Map("limit" -> "100"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 99)

    assert(AssertionResponseAction.performResponseAction(response, statusCodeResponseAction))
  }

  test("performAssertions - response time is below limit - failed") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.RESPONSE_TIME_IS_BELOW}", Map("limit" -> "100"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 101)

    assert(!AssertionResponseAction.performResponseAction(response, statusCodeResponseAction))
  }

  test("performAssertions - response time is above limit - success") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.RESPONSE_TIME_IS_ABOVE}", Map("limit" -> "100"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 101)

    assert(AssertionResponseAction.performResponseAction(response, statusCodeResponseAction))
  }

  test("performAssertions - response time is above limit - failed") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.RESPONSE_TIME_IS_ABOVE}", Map("limit" -> "100"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 99)

    assert(!AssertionResponseAction.performResponseAction(response, statusCodeResponseAction))
  }

  // status-code-...

  test("performAssertions - status code assertion - equals") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_EQUALS}", Map("code" -> "200"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, statusCodeResponseAction))
  }

  test("performAssertions - status code assertion - not equals") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_EQUALS}", Map("code" -> "200"))
    val response = Response(500, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(!AssertionResponseAction.performResponseAction(response, statusCodeResponseAction))
  }

  test("performAssertions - status code - is success") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_IS_SUCCESS}", Map.empty)
    val response200 = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response299 = Response(299, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response200, statusCodeResponseAction))
    assert(AssertionResponseAction.performResponseAction(response299, statusCodeResponseAction))
  }

  test("performAssertions - status code - is not success") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_IS_SUCCESS}", Map.empty)
    val response199 = Response(199, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response300 = Response(300, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response500 = Response(500, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(!AssertionResponseAction.performResponseAction(response199, statusCodeResponseAction))
    assert(!AssertionResponseAction.performResponseAction(response300, statusCodeResponseAction))
    assert(!AssertionResponseAction.performResponseAction(response500, statusCodeResponseAction))
  }

  test("performAssertions - status code - is client error") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_IS_CLIENT_ERROR}", Map.empty)
    val response400 = Response(400, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response499 = Response(499, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response400, statusCodeResponseAction))
    assert(AssertionResponseAction.performResponseAction(response499, statusCodeResponseAction))
  }

  test("performAssertions - status code - is not client error") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_IS_CLIENT_ERROR}", Map.empty)
    val response399 = Response(399, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response500 = Response(500, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response200 = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(!AssertionResponseAction.performResponseAction(response399, statusCodeResponseAction))
    assert(!AssertionResponseAction.performResponseAction(response500, statusCodeResponseAction))
    assert(!AssertionResponseAction.performResponseAction(response200, statusCodeResponseAction))
  }

  test("performAssertions - status code - is server error") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_IS_SERVER_ERROR}", Map.empty)
    val response500 = Response(500, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response599 = Response(599, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response500, statusCodeResponseAction))
    assert(AssertionResponseAction.performResponseAction(response599, statusCodeResponseAction))
  }

  test("performAssertions - status code - is not server error") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_IS_SERVER_ERROR}", Map.empty)
    val response499 = Response(499, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response600 = Response(600, "Dummy Body", "", "", Map.empty, Map.empty, 100)
    val response200 = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    assert(!AssertionResponseAction.performResponseAction(response499, statusCodeResponseAction))
    assert(!AssertionResponseAction.performResponseAction(response600, statusCodeResponseAction))
    assert(!AssertionResponseAction.performResponseAction(response200, statusCodeResponseAction))
  }

  // header-...

  test("performAssertions - header exists") {
    val headerExistsResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.HEADER_EXISTS}", Map("headerName" -> "Content-Type"))
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, headerExistsResponseAction))
  }

  test("performAssertions - header does not exists") {
    val headerExistsResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.HEADER_EXISTS}", Map("headerName" -> "headerValue"))
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    assert(!AssertionResponseAction.performResponseAction(response, headerExistsResponseAction))
  }

  test("performAssertions - header value is equals") {
    val headerValueEqualsResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.HEADER_VALUE_EQUALS}", Map("headerName" -> "Content-Type", "expectedValue" -> "application/json"))
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, headerValueEqualsResponseAction))
  }

  test("performAssertions - header value is not equals") {
    val headerValueEqualsResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.HEADER_VALUE_EQUALS}", Map("headerName" -> "someName", "expectedValue" -> "someValue"))
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    assert(!AssertionResponseAction.performResponseAction(response, headerValueEqualsResponseAction))
  }

  // content-type-...

  test("performAssertions - content type is json") {
    val contentTypeIsJsonResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.CONTENT_TYPE_IS_JSON}", Map.empty)
    val response = Response(200, "{}", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction))
  }

  test("performAssertions - content type is not json") {
    val contentTypeIsJsonResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.CONTENT_TYPE_IS_JSON}", Map.empty)
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/xml")), Map.empty, 100)

    assert(!AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction))
  }

  test("performAssertions - content type is xml") {
    val contentTypeIsJsonResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.CONTENT_TYPE_IS_XML}", Map.empty)
    val response = Response(200, """<?xml version="1.0" encoding="UTF-8"?><note><to>QA</to><from>Dev</from><body>Don't forget to test it!</body></note>""", "", "", Map("content-type" -> Seq("application/xml")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction))
  }

  test("performAssertions - content type is not xml") {
    val contentTypeIsJsonResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.CONTENT_TYPE_IS_XML}", Map.empty)
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/json")), Map.empty, 100)

    assert(!AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction))
  }

  test("performAssertions - content type is html") {
    val contentTypeIsJsonResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.CONTENT_TYPE_IS_HTML}", Map.empty)
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("text/html")), Map.empty, 100)

    assert(AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction))
  }

  test("performAssertions - content type is not html") {
    val contentTypeIsJsonResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.CONTENT_TYPE_IS_HTML}", Map.empty)
    val response = Response(200, "Dummy Body", "", "", Map("content-type" -> Seq("application/xml")), Map.empty, 100)

    assert(!AssertionResponseAction.performResponseAction(response, contentTypeIsJsonResponseAction))
  }

  // body-...

  test("performAssertions - body contains assertion") {
    val bodyContainsResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.body-contains", Map("body" -> "dummy"))
    val response = Response(200, "This is a dummy body", "", "", Map.empty, Map.empty, 100)
    assert(AssertionResponseAction.performResponseAction(response, bodyContainsResponseAction))
  }

  test("performAssertions - body does not contains assertion") {
    val bodyContainsResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.body-contains", Map("body" -> "dummies"))
    val response = Response(200, "This is a dummy body", "", "", Map.empty, Map.empty, 100)

    assert(!AssertionResponseAction.performResponseAction(response, bodyContainsResponseAction))
  }


  test("performAssertions - unsupported assertion") {
    val unsupportedResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.unsupported-assertion", Map("nonsense" -> "value"))
    val response = Response(200, "Dummy Body", "", "", Map.empty, Map.empty, 100)

    interceptMessage[IllegalArgumentException]("Unsupported assertion method [group: assert]: unsupported-assertion") {
      AssertionResponseAction.performResponseAction(response, unsupportedResponseAction)
    }
  }
}
