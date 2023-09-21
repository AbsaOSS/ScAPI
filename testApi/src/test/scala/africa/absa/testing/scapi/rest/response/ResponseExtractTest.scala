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

import africa.absa.testing.scapi.{ContentValidationFailedException, UndefinedResponseActionTypeException}
import africa.absa.testing.scapi.json.ResponseAction
import africa.absa.testing.scapi.utils.cache.RuntimeCache
import munit.FunSuite

import scala.util.Try

class ResponseExtractTest extends FunSuite {

  val assertionStringFromList: ResponseAction = ResponseAction(group = ResponseActionGroupType.EXTRACT_JSON, name = ExtractJsonResponseActionType.STRING_FROM_LIST, Map("cacheKey" -> "question_id", "listIndex" -> "1", "jsonKey" -> "id", "cacheLevel" -> "suite"))
  val assertionUnsupported: ResponseAction = ResponseAction(group = ResponseActionGroupType.EXTRACT_JSON, name = "Unsupported", Map("cacheKey" -> "key", "listIndex" -> "200", "jsonKey" -> "jsonKey", "cacheLevel" -> "Test"))

  val responseWithID: Response = Response(
    200,
    "[{\"id\":\"efa01eeb-34cb-42da-b150-ca6dbe52xxx1\",\"domainName\":\"Domain1\"},{\"id\":\"382be85a-1f00-4c15-b607-cbda03ccxxx2\",\"domainName\":\"Domain2\"},{\"id\":\"65173a5b-b13c-4db0-bd1b-24b3e3abxxx3\",\"domainName\":\"Domain3\"}]",
    "",
    "",
    Map("Content-Type" -> Seq("application/json")),
    Map.empty,
    100
  )
  val responseNoJsonBody: Response = Response(
    200,
    "no json here",
    "",
    "",
    Map("Content-Type" -> Seq("application/json")),
    Map.empty,
    100
  )
  val responseJsonNoArrayBody: Response = Response(
    200,
    "{\"id\":\"efa01eeb-34cb-42da-b150-ca6dbe52xxx1\",\"domainName\":\"Domain1\"}",
    "",
    "",
    Map("Content-Type" -> Seq("application/json")),
    Map.empty,
    100
  )

  /*
    validateContent
   */

  test("validateContent - STRING_FROM_LIST") {
    ExtractJsonResponseAction.validateContent(assertionStringFromList)
  }

  test("validateContent - unsupported option") {
    intercept[UndefinedResponseActionTypeException] {
      ExtractJsonResponseAction.validateContent(assertionUnsupported)
    }
  }

  /*
    performResponseAction
   */

  test("performAssertion - STRING_FROM_LIST") {
    val result: Try[Unit] = ExtractJsonResponseAction.performResponseAction(responseWithID, assertionStringFromList)
    assert(result.isSuccess)
    assertEquals("382be85a-1f00-4c15-b607-cbda03ccxxx2", RuntimeCache.get("question_id").get)
  }

  test("performAssertion - unsupported assertion") {
    intercept[IllegalArgumentException] {
      ExtractJsonResponseAction.performResponseAction(responseWithID, assertionUnsupported)
    }
  }

  /*
    stringFromList
   */

  // positive test "stringFromList - correct parameters" - tested during "performAssertion - STRING_FROM_LIST"

  test("stringFromList - incorrect parameters - wrong list index") {
    val cacheKey = "question_id"
    val listIndex = 10
    val jsonKey = "id"
    val runtimeCacheLevel = "Test"
    interceptMessage[IndexOutOfBoundsException]("10 is out of bounds (min 0, max 2)") {
      ExtractJsonResponseAction.stringFromList(responseWithID, cacheKey, listIndex, jsonKey, runtimeCacheLevel)
    }
  }

  test("stringFromList - incorrect parameters - wrong jsonKey") {
    val cacheKey = "question_id"
    val listIndex = 0
    val jsonKey = "ids"
    val runtimeCacheLevel = "Test"
    val result = ExtractJsonResponseAction.stringFromList(responseWithID, cacheKey, listIndex, jsonKey, runtimeCacheLevel)
    assert(!result.isSuccess)
  }

  test("stringFromList - incorrect parameters - no json body in response") {
    val cacheKey = "question_id"
    val listIndex = 0
    val jsonKey = "id"
    val runtimeCacheLevel = "Test"

    val result = ExtractJsonResponseAction.stringFromList(responseNoJsonBody, cacheKey, listIndex, jsonKey, runtimeCacheLevel)
    assert(!result.isSuccess)
  }

  test("stringFromList - incorrect parameters - no json arrays in response body") {
    val cacheKey = "question_id"
    val listIndex = 0
    val jsonKey = "id"
    val runtimeCacheLevel = "Test"

    val result = ExtractJsonResponseAction.stringFromList(responseJsonNoArrayBody, cacheKey, listIndex, jsonKey, runtimeCacheLevel)
    assert(!result.isSuccess)
  }

  /*
    validateStringFromList
   */

  // positive test "stringFromList - correct parameters" - tested during "validateContent - STRING_FROM_LIST"

  test("validateStringFromList - None parameters") {
    val assertion1None: ResponseAction = ResponseAction(group = ResponseActionGroupType.EXTRACT_JSON, name = ExtractJsonResponseActionType.STRING_FROM_LIST, Map("cacheKey" -> "", "listIndex" -> "", "jsonKey" -> ""))
    val assertion2None: ResponseAction = ResponseAction(group = ResponseActionGroupType.EXTRACT_JSON, name = ExtractJsonResponseActionType.STRING_FROM_LIST, Map("cacheKey" -> "", "listIndex" -> ""))
    val assertion3None: ResponseAction = ResponseAction(group = ResponseActionGroupType.EXTRACT_JSON, name = ExtractJsonResponseActionType.STRING_FROM_LIST, Map("cacheKey" -> ""))
    val assertion4None: ResponseAction = ResponseAction(group = ResponseActionGroupType.EXTRACT_JSON, name = ExtractJsonResponseActionType.STRING_FROM_LIST, Map.empty)

    interceptMessage[IllegalArgumentException]("Missing required 'cacheKey' parameter for extract string-from-list logic") {
      ExtractJsonResponseAction.validateStringFromList(assertion4None)
    }
    interceptMessage[IllegalArgumentException]("Missing required 'listIndex' parameter for extract string-from-list logic") {
      ExtractJsonResponseAction.validateStringFromList(assertion3None)
    }
    interceptMessage[IllegalArgumentException]("Missing required 'jsonKey' parameter for extract string-from-list logic") {
      ExtractJsonResponseAction.validateStringFromList(assertion2None)
    }
    interceptMessage[IllegalArgumentException]("Missing required 'cacheLevel' parameter for extract string-from-list logic") {
      ExtractJsonResponseAction.validateStringFromList(assertion1None)
    }
  }

  test("validateStringFromList - empty parameters") {
    val assertionParam1: ResponseAction = ResponseAction(group = ResponseActionGroupType.EXTRACT_JSON, name = ExtractJsonResponseActionType.STRING_FROM_LIST, Map("cacheKey" -> "", "listIndex" -> "", "jsonKey" -> "", "cacheLevel" -> ""))
    val assertionParam2: ResponseAction = ResponseAction(group = ResponseActionGroupType.EXTRACT_JSON, name = ExtractJsonResponseActionType.STRING_FROM_LIST, Map("cacheKey" -> "1", "listIndex" -> "", "jsonKey" -> "", "cacheLevel" -> ""))
    val assertionParam3: ResponseAction = ResponseAction(group = ResponseActionGroupType.EXTRACT_JSON, name = ExtractJsonResponseActionType.STRING_FROM_LIST, Map("cacheKey" -> "1", "listIndex" -> "x", "jsonKey" -> "", "cacheLevel" -> ""))
    val assertionParam4: ResponseAction = ResponseAction(group = ResponseActionGroupType.EXTRACT_JSON, name = ExtractJsonResponseActionType.STRING_FROM_LIST, Map("cacheKey" -> "1", "listIndex" -> "x", "jsonKey" -> "y", "cacheLevel" -> ""))

    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ExtractJson.string-from-list.cacheKey' is empty.") {
      ExtractJsonResponseAction.validateStringFromList(assertionParam1)
    }
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ExtractJson.string-from-list.listIndex' is empty.") {
      ExtractJsonResponseAction.validateStringFromList(assertionParam2)
    }
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ExtractJson.string-from-list.jsonKey' is empty.") {
      ExtractJsonResponseAction.validateStringFromList(assertionParam3)
    }
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: '': Received string value of 'ExtractJson.string-from-list.cacheLevel' is empty.") {
      ExtractJsonResponseAction.validateStringFromList(assertionParam4)
    }
  }

  test("validateStringFromList - not integer in string") {
    val assertion: ResponseAction = ResponseAction(group = ResponseActionGroupType.EXTRACT_JSON, name = ExtractJsonResponseActionType.STRING_FROM_LIST, Map("cacheKey" -> "key", "listIndex" -> "x", "jsonKey" -> "y", "cacheLevel" -> "y"))
    interceptMessage[ContentValidationFailedException]("Content validation failed for value: 'x': Received value of 'ExtractJson.string-from-list.listIndex' cannot be parsed to an integer: For input string: \"x\"") {
      ExtractJsonResponseAction.validateStringFromList(assertion)
    }

  }
}
