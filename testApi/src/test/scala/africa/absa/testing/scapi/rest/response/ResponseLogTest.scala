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

import africa.absa.testing.scapi.UndefinedAssertionType
import africa.absa.testing.scapi.json.Assertion
import munit.FunSuite

class ResponseLogTest extends FunSuite {

  /*
    validateContent
   */
  test("validateContent - INFO supported") {
    val assertionInfo = Assertion(Response.GROUP_LOG, ResponseLog.INFO, Map("param_1" -> "Non-empty string"))
    // no exception thrown, meaning validation passed
    ResponseLog.validateContent(assertionInfo)
  }

  test("validateContent - not supported validation type") {
    val assertion = Assertion(Response.GROUP_LOG, "not_info", Map("param_1" -> "Some string"))
    intercept[UndefinedAssertionType] {
      ResponseLog.validateContent(assertion)
    }
  }

  /*
    performAssertion
   */

  test("performAssertion - INFO supported") {
    val assertion = Assertion(Response.GROUP_LOG, ResponseLog.INFO, Map("param_1" -> "info message"))
    val response = Response(200, "OK", Map("Content-Type" -> Seq("application/json")))
    assertEquals(ResponseLog.performAssertion(response, assertion), true)
  }

  test("performAssertion - not supported validation type") {
    val assertion = Assertion(Response.GROUP_LOG, "not_info", Map("param_1" -> "info message"))
    val response = Response(200, "OK", Map("Content-Type" -> Seq("application/json")))
    intercept[IllegalArgumentException] {
      ResponseLog.performAssertion(response, assertion)
    }
  }

  /*
    logInfo
   */

  test("logInfo") {
    assertEquals(ResponseLog.logInfo("log message"), true)
    // // TODO - review this test Issue #11
  }
}
