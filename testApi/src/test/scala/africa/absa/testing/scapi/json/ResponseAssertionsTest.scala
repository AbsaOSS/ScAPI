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

package africa.absa.testing.scapi.json

import africa.absa.testing.scapi.rest.response.{Response, ResponseAssertions}
import africa.absa.testing.scapi.{ContentValidationFailed, UndefinedAssertionType}
import munit.FunSuite

class ResponseAssertionsTest extends FunSuite {

  /*
    validateContent
   */
  test("validateContent - valid status code string") {
    val assertion = Assertion(ResponseAssertions.STATUS_CODE, "200")
    ResponseAssertions.validateContent(assertion)
  }

  test("validateContent - invalid status code string") {
    intercept[ContentValidationFailed] {
      ResponseAssertions.validateContent(Assertion(ResponseAssertions.STATUS_CODE, "not an integer"))
    }
  }

  test("validateContent - body is not empty") {
    val assertion = Assertion(ResponseAssertions.BODY_CONTAINS, "test content")
    ResponseAssertions.validateContent(assertion)
  }

  test("validateContent - body is empty") {
    intercept[ContentValidationFailed] {
      ResponseAssertions.validateContent(Assertion(ResponseAssertions.BODY_CONTAINS, ""))
    }
  }

  test("validateContent - unsupported assertion") {
    intercept[UndefinedAssertionType] {
      ResponseAssertions.validateContent(Assertion("unsupported", "value"))
    }
  }

  /*
    performAssertions
   */
  test("performAssertions - status code assertion - equals") {
    val statusCodeAssertion = Assertion("status-code", "200")
    val response = Response(200, "Dummy Body", Map.empty)

    ResponseAssertions.performAssertions(response, Set(statusCodeAssertion))
  }

  test("performAssertions - status code assertion - not equals") {
    val statusCodeAssertion = Assertion("status-code", "200")
    val response = Response(500, "Dummy Body", Map.empty)

    interceptMessage[java.lang.AssertionError]("assertion failed: Expected 200, but got 500") {
      ResponseAssertions.performAssertions(response, Set(statusCodeAssertion))
    }
  }

  test("performAssertions - body contains assertion") {
    val bodyContainsAssertion = Assertion("body-contains", "dummy")
    val response = Response(200, "This is a dummy body", Map.empty)
    ResponseAssertions.performAssertions(response, Set(bodyContainsAssertion))
  }

  test("performAssertions - body does not contains assertion") {
    val bodyContainsAssertion = Assertion("body-contains", "dummies")
    val response = Response(200, "This is a dummy body", Map.empty)

    interceptMessage[java.lang.AssertionError]("assertion failed: Expected body to contain dummies") {
      ResponseAssertions.performAssertions(response, Set(bodyContainsAssertion))
    }
  }

  test("performAssertions - unsupported assertion") {
    val unsupportedAssertion = Assertion("unsupported-assertion", "value")
    val response = Response(200, "Dummy Body", Map.empty)

    interceptMessage[IllegalArgumentException]("Unsupported assertion: unsupported-assertion") {
      ResponseAssertions.performAssertions(response, Set(unsupportedAssertion))
    }
  }
}
