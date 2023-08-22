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
  test("validateContent - valid status code string") {
    val assertion = ResponseAction(group = Response.GROUP_ASSERT, name = AssertionResponseAction.STATUS_CODE, Map("param_1" -> "200"))
    AssertionResponseAction.validateContent(assertion)
  }

  test("validateContent - invalid status code string") {
    intercept[ContentValidationFailed] {
      AssertionResponseAction.validateContent(ResponseAction(group = Response.GROUP_ASSERT, name = AssertionResponseAction.STATUS_CODE, Map("param_1" -> "not an integer")))
    }
  }

  test("validateContent - body is not empty") {
    val assertion = ResponseAction(group = Response.GROUP_ASSERT, name = AssertionResponseAction.BODY_CONTAINS, Map("param_1" -> "test content"))
    AssertionResponseAction.validateContent(assertion)
  }

  test("validateContent - body is empty") {
    intercept[ContentValidationFailed] {
      AssertionResponseAction.validateContent(ResponseAction(group = Response.GROUP_ASSERT, name = AssertionResponseAction.BODY_CONTAINS, Map("param_1" -> "")))
    }
  }

  test("validateContent - unsupported response action") {
    intercept[UndefinedResponseActionType] {
      AssertionResponseAction.validateContent(ResponseAction(group = Response.GROUP_ASSERT, name = "unsupported", Map("param_1" -> "value")))
    }
  }

  /*
    performResponseAction
   */
  test("performAssertions - status code assertion - equals") {
    val statusCodeAssertion = ResponseAction(group = Response.GROUP_ASSERT, name = "status-code", Map("param_1" -> "200"))
    val response = Response(200, "Dummy Body", Map.empty)

    assert(AssertionResponseAction.performResponseAction(response, statusCodeAssertion))
  }

  test("performAssertions - status code assertion - not equals") {
    val statusCodeAssertion = ResponseAction(group = Response.GROUP_ASSERT, name = "status-code", Map("param_1" -> "200"))
    val response = Response(500, "Dummy Body", Map.empty)

    assert(!AssertionResponseAction.performResponseAction(response, statusCodeAssertion))
  }

  test("performAssertions - body contains assertion") {
    val bodyContainsAssertion = ResponseAction(group = Response.GROUP_ASSERT, name = "body-contains", Map("param_1" -> "dummy"))
    val response = Response(200, "This is a dummy body", Map.empty)
    assert(AssertionResponseAction.performResponseAction(response, bodyContainsAssertion))
  }

  test("performAssertions - body does not contains assertion") {
    val bodyContainsAssertion = ResponseAction(group = Response.GROUP_ASSERT, name = "body-contains", Map("param_1" -> "dummies"))
    val response = Response(200, "This is a dummy body", Map.empty)

    assert(!AssertionResponseAction.performResponseAction(response, bodyContainsAssertion))
  }

  test("performAssertions - unsupported assertion") {
    val unsupportedAssertion = ResponseAction(group = Response.GROUP_ASSERT, name = "unsupported-assertion", Map("param_1" -> "value"))
    val response = Response(200, "Dummy Body", Map.empty)

    interceptMessage[IllegalArgumentException]("Unsupported assertion[group: assert]: unsupported-assertion") {
      AssertionResponseAction.performResponseAction(response, unsupportedAssertion)
    }
  }
}
