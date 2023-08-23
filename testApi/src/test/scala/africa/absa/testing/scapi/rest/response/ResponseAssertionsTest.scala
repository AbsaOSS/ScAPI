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

  // status-code-...

  test("performAssertions - status code assertion - equals") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_EQUALS}", Map("code" -> "200"))
    val response = Response(200, "Dummy Body", Map.empty)

    assert(AssertionResponseAction.performResponseAction(response, statusCodeResponseAction))
  }

  test("performAssertions - status code assertion - not equals") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_EQUALS}", Map("code" -> "200"))
    val response = Response(500, "Dummy Body", Map.empty)

    assert(!AssertionResponseAction.performResponseAction(response, statusCodeResponseAction))
  }

  test("performAssertions - status code - is success") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_IS_SUCCESS}", Map())
    val response200 = Response(200, "Dummy Body", Map.empty)
    val response299 = Response(299, "Dummy Body", Map.empty)

    assert(AssertionResponseAction.performResponseAction(response200, statusCodeResponseAction))
    assert(AssertionResponseAction.performResponseAction(response299, statusCodeResponseAction))
  }

  test("performAssertions - status code - is not success") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_IS_SUCCESS}", Map())
    val response199 = Response(199, "Dummy Body", Map.empty)
    val response300 = Response(300, "Dummy Body", Map.empty)
    val response500 = Response(500, "Dummy Body", Map.empty)

    assert(!AssertionResponseAction.performResponseAction(response199, statusCodeResponseAction))
    assert(!AssertionResponseAction.performResponseAction(response300, statusCodeResponseAction))
    assert(!AssertionResponseAction.performResponseAction(response500, statusCodeResponseAction))
  }

  test("performAssertions - status code - is client error") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_IS_CLIENT_ERROR}", Map())
    val response400 = Response(400, "Dummy Body", Map.empty)
    val response499 = Response(499, "Dummy Body", Map.empty)

    assert(AssertionResponseAction.performResponseAction(response400, statusCodeResponseAction))
    assert(AssertionResponseAction.performResponseAction(response499, statusCodeResponseAction))
  }

  test("performAssertions - status code - is not client error") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_IS_CLIENT_ERROR}", Map())
    val response399 = Response(399, "Dummy Body", Map.empty)
    val response500 = Response(500, "Dummy Body", Map.empty)
    val response200 = Response(200, "Dummy Body", Map.empty)

    assert(!AssertionResponseAction.performResponseAction(response399, statusCodeResponseAction))
    assert(!AssertionResponseAction.performResponseAction(response500, statusCodeResponseAction))
    assert(!AssertionResponseAction.performResponseAction(response200, statusCodeResponseAction))
  }

  test("performAssertions - status code - is server error") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_IS_SERVER_ERROR}", Map())
    val response500 = Response(500, "Dummy Body", Map.empty)
    val response599 = Response(599, "Dummy Body", Map.empty)

    assert(AssertionResponseAction.performResponseAction(response500, statusCodeResponseAction))
    assert(AssertionResponseAction.performResponseAction(response599, statusCodeResponseAction))
  }

  test("performAssertions - status code - is not server error") {
    val statusCodeResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.${AssertionResponseAction.STATUS_CODE_IS_SERVER_ERROR}", Map())
    val response499 = Response(499, "Dummy Body", Map.empty)
    val response600 = Response(600, "Dummy Body", Map.empty)
    val response200 = Response(200, "Dummy Body", Map.empty)

    assert(!AssertionResponseAction.performResponseAction(response499, statusCodeResponseAction))
    assert(!AssertionResponseAction.performResponseAction(response600, statusCodeResponseAction))
    assert(!AssertionResponseAction.performResponseAction(response200, statusCodeResponseAction))
  }


  // body-...

  test("performAssertions - body contains assertion") {
    val bodyContainsResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.body-contains", Map("body" -> "dummy"))
    val response = Response(200, "This is a dummy body", Map.empty)
    assert(AssertionResponseAction.performResponseAction(response, bodyContainsResponseAction))
  }

  test("performAssertions - body does not contains assertion") {
    val bodyContainsResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.body-contains", Map("body" -> "dummies"))
    val response = Response(200, "This is a dummy body", Map.empty)

    assert(!AssertionResponseAction.performResponseAction(response, bodyContainsResponseAction))
  }


  test("performAssertions - unsupported assertion") {
    val unsupportedResponseAction = ResponseAction(method = s"${Response.GROUP_ASSERT}.unsupported-assertion", Map("nonsense" -> "value"))
    val response = Response(200, "Dummy Body", Map.empty)

    interceptMessage[IllegalArgumentException]("Unsupported assertion method [group: assert]: unsupported-assertion") {
      AssertionResponseAction.performResponseAction(response, unsupportedResponseAction)
    }
  }
}
