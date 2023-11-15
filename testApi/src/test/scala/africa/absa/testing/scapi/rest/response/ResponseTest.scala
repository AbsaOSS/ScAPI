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
import africa.absa.testing.scapi.rest.response.action.types.AssertResponseActionType.AssertResponseActionType
import africa.absa.testing.scapi.rest.response.action.types.ExtractJsonResponseActionType.ExtractJsonResponseActionType
import africa.absa.testing.scapi.rest.response.action.types.LogResponseActionType.LogResponseActionType
import africa.absa.testing.scapi.rest.response.action.types.{AssertResponseActionType, ExtractJsonResponseActionType, ResponseActionGroupType, LogResponseActionType => LogType}
import munit.FunSuite

import scala.language.implicitConversions


class ResponseTest extends FunSuite {

  implicit def extractJsonResponseActionType2String(value: ExtractJsonResponseActionType): String = value.toString
  implicit def assertResponseActionType2String(value: AssertResponseActionType): String = value.toString
  implicit def logResponseActionType2String(value: LogResponseActionType): String = value.toString

  /*
    validate
   */
  test("validate - response action - group assert") {
    val responseAction = ResponseAction(
      group = ResponseActionGroupType.Assert,
      name = AssertResponseActionType.ResponseTimeIsBelow,
      Map("limit" -> "200"))

    Response.validate(responseAction)
  }

  test("validate - response action - group log") {
    val responseAction: ResponseAction = ResponseAction(
      group = ResponseActionGroupType.ExtractJson,
      name = ExtractJsonResponseActionType.StringFromList,
      Map("cacheKey" -> "question_id", "listIndex" -> "1", "jsonKey" -> "id", "cacheLevel" -> "suite"))

    Response.validate(responseAction)
  }

  test("validate - response action - group extractJson") {
    val responseAction = ResponseAction(
      group = ResponseActionGroupType.Log,
      name = LogType.Error,
      Map("message" -> "Non-empty string"))

    Response.validate(responseAction)
  }
}
