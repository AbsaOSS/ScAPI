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
import africa.absa.testing.scapi.rest.response.action.LogResponseAction
import africa.absa.testing.scapi.rest.response.action.types.LogResponseActionType.LogResponseActionType
import africa.absa.testing.scapi.rest.response.action.types.{ResponseActionGroupType, LogResponseActionType => LogType}
import munit.FunSuite

import scala.language.implicitConversions


class ResponseLogTest extends FunSuite {

  implicit def logResponseActionType2String(value: LogResponseActionType): String = value.toString

  /*
    validateContent
   */
  test("validateContent - ERROR supported") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Log, name = LogType.Error, Map("message" -> "Non-empty string"))
    // no exception thrown, meaning validation passed
    LogResponseAction.validateContent(responseAction)
  }

  test("validateContent - WARN supported") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Log, name = LogType.Warn, Map("message" -> "Non-empty string"))
    // no exception thrown, meaning validation passed
    LogResponseAction.validateContent(responseAction)
  }

  test("validateContent - INFO supported") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Log, name = LogType.Info, Map("message" -> "Non-empty string"))
    // no exception thrown, meaning validation passed
    LogResponseAction.validateContent(responseAction)
  }

  test("validateContent - DEBUG supported") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Log, name = LogType.Debug, Map("message" -> "Non-empty string"))
    // no exception thrown, meaning validation passed
    LogResponseAction.validateContent(responseAction)
  }

  test("validateContent - no message provided") {
    val responseAction = ResponseAction(group = ResponseActionGroupType.Log, name = LogType.Info, Map.empty)
    interceptMessage[IllegalArgumentException]("Missing required 'message' for assertion info logic.") {
      LogResponseAction.validateContent(responseAction)
    }
  }

  /*
    performResponseAction
   */

  test("performAssertion - ERROR supported") {
    val assertion = ResponseAction(group = ResponseActionGroupType.Log, name = LogType.Error, Map("message" -> "info message"))
    val response = Response(500, "OK", "", "", Map("Content-Type" -> Seq("application/json")), Map.empty, 100)
    assert(LogResponseAction.performResponseAction(response, assertion).isSuccess)
  }

  test("performAssertion - WARN supported") {
    val assertion = ResponseAction(group = ResponseActionGroupType.Log, name = LogType.Warn, Map("message" -> "info message"))
    val response = Response(401, "OK", "", "", Map("Content-Type" -> Seq("application/json")), Map.empty, 100)
    assert(LogResponseAction.performResponseAction(response, assertion).isSuccess)
  }

  test("performAssertion - INFO supported") {
    val assertion = ResponseAction(group = ResponseActionGroupType.Log, name = LogType.Info, Map("message" -> "info message"))
    val response = Response(200, "OK", "", "", Map("Content-Type" -> Seq("application/json")), Map.empty, 100)
    assert(LogResponseAction.performResponseAction(response, assertion).isSuccess)
  }

  test("performAssertion - DEBUG supported") {
    val assertion = ResponseAction(group = ResponseActionGroupType.Log, name = LogType.Debug, Map("message" -> "info message"))
    val response = Response(200, "OK", "", "", Map("Content-Type" -> Seq("application/json")), Map.empty, 100)
    assert(LogResponseAction.performResponseAction(response, assertion).isSuccess)
  }
}
