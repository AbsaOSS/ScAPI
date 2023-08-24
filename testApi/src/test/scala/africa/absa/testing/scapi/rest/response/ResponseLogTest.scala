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
import munit.FunSuite

class ResponseLogTest extends FunSuite {

  /*
    validateContent
   */
  test("validateContent - ERROR supported") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_LOG}.${LogResponseAction.ERROR}", Map("message" -> "Non-empty string"))
    // no exception thrown, meaning validation passed
    LogResponseAction.validateContent(responseAction)
  }

  test("validateContent - WARN supported") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_LOG}.${LogResponseAction.WARN}", Map("message" -> "Non-empty string"))
    // no exception thrown, meaning validation passed
    LogResponseAction.validateContent(responseAction)
  }

  test("validateContent - INFO supported") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_LOG}.${LogResponseAction.INFO}", Map("message" -> "Non-empty string"))
    // no exception thrown, meaning validation passed
    LogResponseAction.validateContent(responseAction)
  }

  test("validateContent - DEBUG supported") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_LOG}.${LogResponseAction.DEBUG}", Map("message" -> "Non-empty string"))
    // no exception thrown, meaning validation passed
    LogResponseAction.validateContent(responseAction)
  }

  test("validateContent - not supported validation type") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_LOG}.wrong", Map("message" -> "Some string"))
    intercept[UndefinedResponseActionType] {
      LogResponseAction.validateContent(responseAction)
    }
  }

  test("validateContent - no message provided") {
    val responseAction = ResponseAction(method = s"${Response.GROUP_LOG}.${LogResponseAction.INFO}", Map.empty)
    interceptMessage[IllegalArgumentException]("Missing required 'message' for assertion info logic.") {
      LogResponseAction.validateContent(responseAction)
    }
  }

  /*
    performResponseAction
   */

  test("performAssertion - ERROR supported") {
    val assertion = ResponseAction(method = s"${Response.GROUP_LOG}.${LogResponseAction.ERROR}", Map("message" -> "info message"))
    val response = Response(500, "OK", "", "", Map("Content-Type" -> Seq("application/json")), Map.empty, 100)
    assertEquals(LogResponseAction.performResponseAction(response, assertion), true)
  }

  test("performAssertion - WARN supported") {
    val assertion = ResponseAction(method = s"${Response.GROUP_LOG}.${LogResponseAction.WARN}", Map("message" -> "info message"))
    val response = Response(401, "OK", "", "", Map("Content-Type" -> Seq("application/json")), Map.empty, 100)
    assertEquals(LogResponseAction.performResponseAction(response, assertion), true)
  }

  test("performAssertion - INFO supported") {
    val assertion = ResponseAction(method = s"${Response.GROUP_LOG}.${LogResponseAction.INFO}", Map("message" -> "info message"))
    val response = Response(200, "OK", "", "", Map("Content-Type" -> Seq("application/json")), Map.empty, 100)
    assertEquals(LogResponseAction.performResponseAction(response, assertion), true)
  }

  test("performAssertion - DEBUG supported") {
    val assertion = ResponseAction(method = s"${Response.GROUP_LOG}.${LogResponseAction.DEBUG}", Map("message" -> "info message"))
    val response = Response(200, "OK", "", "", Map("Content-Type" -> Seq("application/json")), Map.empty, 100)
    assertEquals(LogResponseAction.performResponseAction(response, assertion), true)
  }

  test("performAssertion - not supported validation type") {
    val assertion = ResponseAction(method = s"${Response.GROUP_LOG}.not_info", Map("message" -> "info message"))
    val response = Response(200, "OK", "", "", Map("Content-Type" -> Seq("application/json")), Map.empty, 100)
    intercept[IllegalArgumentException] {
      LogResponseAction.performResponseAction(response, assertion)
    }
  }

  /*
    logError
   */

  test("logError") {
    assertEquals(LogResponseAction.logError("log error message"), true)
  }

  /*
    logWarn
   */

  test("logInfo") {
    assertEquals(LogResponseAction.logWarn("log warn message"), true)
  }

  /*
    logInfo
   */

  test("logInfo") {
    assertEquals(LogResponseAction.logInfo("log info message"), true)
  }

  /*
    logDebug
   */

  test("logDebug") {
    assertEquals(LogResponseAction.logDebug("log debug message"), true)
  }


}
