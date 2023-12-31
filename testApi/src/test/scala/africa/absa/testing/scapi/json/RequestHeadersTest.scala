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

import africa.absa.testing.scapi.rest.request.RequestHeaders
import africa.absa.testing.scapi.{ContentValidationFailedException, UndefinedHeaderTypeException}
import munit.FunSuite

class RequestHeadersTest extends FunSuite {

  /*
    buildHeaders
   */

  test("buildHeaders - should correctly build headers map") {
    val headersSeq = Seq(
      Header("Content-Type", "application/json"),
      Header("Authorization", "Bearer abcdefg12345"),
      Header("Custom-Header", "customValue")
    )

    val expectedMap = Map(
      "Content-Type" -> "application/json",
      "Authorization" -> "Bearer abcdefg12345",
      "Custom-Header" -> "customValue"
    )

    val actualMap = RequestHeaders.buildHeaders(headersSeq)
    assert(clue(expectedMap) == clue(actualMap))
  }

  test("buildHeaders - should return an empty map if no headers are provided") {
    val headersSeq = Seq.empty[Header]
    val expectedMap = Map.empty[String, String]

    val actualMap = RequestHeaders.buildHeaders(headersSeq)
    assert(clue(expectedMap) == clue(actualMap))
  }

  /*
    validateContent
   */

  test("validateContent - CONTENT_TYPE header - filled") {
    val header = Header(RequestHeaders.CONTENT_TYPE, "text/html")
    RequestHeaders.validateContent(header)
  }

  test("validateContent - CONTENT_TYPE header - empty") {
    val header = Header(RequestHeaders.CONTENT_TYPE, "")
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'Header.content-type' is empty.") {
      RequestHeaders.validateContent(header)
    }
  }

  test("validateContent - AUTHORIZATION header - filled") {
    val header = Header(RequestHeaders.AUTHORIZATION, "Bearer abcdefg")
    RequestHeaders.validateContent(header)
  }

  test("validateContent - AUTHORIZATION header - empty") {
    val header = Header(RequestHeaders.AUTHORIZATION, "")
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'Header.authorization' is empty.") {
      RequestHeaders.validateContent(header)
    }
  }

  test("validateContent - Unsupported header type") {
    val header = Header("unsupported-header", "value")
    interceptMessage[UndefinedHeaderTypeException]("Undefined Header content type: 'unsupported-header'") {
      RequestHeaders.validateContent(header)
    }
  }
}
