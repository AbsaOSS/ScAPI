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

import africa.absa.testing.scapi.ContentValidationFailed
import africa.absa.testing.scapi.rest.request.RequestBody
import munit.FunSuite
import spray.json.JsonParser.ParsingException

class RequestBodyTest extends FunSuite {

  /*
    buildBody
   */

  test("buildBody - return string representation of JSON when jsonBody is not empty") {
    val jsonBody = Some("""{"key":"value"}""")
    val result = RequestBody.buildBody(jsonBody)
    assertEquals(result, """{"key":"value"}""")
  }

  test("buildBody - throw exception when non json string received") {
    val jsonBody = Some("""not json string""")

    intercept[ParsingException] {
      RequestBody.buildBody(jsonBody)
    }
  }

  test("buildBody - return empty string representation of JSON when no body received") {
    val result = RequestBody.buildBody()
    assertEquals(result, """{}""")
  }

  test("buildBody - return empty JSON object string when jsonBody is empty") {
    val jsonBody = Some("")
    val result = RequestBody.buildBody(jsonBody)
    assertEquals(result, "{}")
  }

  test("buildBody - return empty JSON object string when jsonBody is None") {
    val jsonBody: Option[String] = None
    val result = RequestBody.buildBody(jsonBody)
    assertEquals(result, "{}")
  }

  /*
    validateContent
   */

  test("validateContent - pass when body is a json string") {
    val jsonBody = Some("""{"key":"value"}""")
    RequestBody.validateContent(jsonBody)
  }

  test("validateContent - pass when body is an empty json string") {
    val jsonBody = Some("""{}""")
    RequestBody.validateContent(jsonBody)
  }

  test("validateContent - pass when body is an empty string") {
    RequestBody.validateContent(Some(""))
  }

  test("validateContent - fail when body is a non json string") {
    val jsonBody = Some("""not json string""")

    intercept[ContentValidationFailed] {
      RequestBody.validateContent(jsonBody)
    }
  }
}
