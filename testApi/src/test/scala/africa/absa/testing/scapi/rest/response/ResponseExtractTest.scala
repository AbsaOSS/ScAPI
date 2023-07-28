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

import africa.absa.testing.scapi.{ContentValidationFailed, UndefinedAssertionType}
import africa.absa.testing.scapi.json.Assertion
import africa.absa.testing.scapi.utils.cache.RuntimeCache
import munit.FunSuite

class ResponseExtractTest extends FunSuite {

  val assertionStringFromList: Assertion = Assertion(Response.GROUP_EXTRACT_JSON, ResponseExtractJson.STRING_FROM_LIST, "question_id", Some("1"), Some("id"), Some("suite"))
  val assertionUnsupported: Assertion = Assertion(Response.GROUP_EXTRACT_JSON, "Unsupported", "key", Some("200"), Some("jsonKey"), Some("Test"))

  val responseWithID: Response = Response(
    200,
    "[{\"id\":\"efa01eeb-34cb-42da-b150-ca6dbe52xxx1\",\"domainName\":\"Domain1\"},{\"id\":\"382be85a-1f00-4c15-b607-cbda03ccxxx2\",\"domainName\":\"Domain2\"},{\"id\":\"65173a5b-b13c-4db0-bd1b-24b3e3abxxx3\",\"domainName\":\"Domain3\"}]",
    Map("Content-Type" -> Seq("application/json")))
  val responseNoJsonBody: Response = Response(
    200,
    "no json here",
    Map("Content-Type" -> Seq("application/json")))
  val responseJsonNoArrayBody: Response = Response(
    200,
    "{\"id\":\"efa01eeb-34cb-42da-b150-ca6dbe52xxx1\",\"domainName\":\"Domain1\"}",
    Map("Content-Type" -> Seq("application/json")))

  /*
    validateContent
   */

  test("validateContent - STRING_FROM_LIST") {
    ResponseExtractJson.validateContent(assertionStringFromList)
  }

  test("validateContent - unsupported option") {
    intercept[UndefinedAssertionType] {
      ResponseExtractJson.validateContent(assertionUnsupported)
    }
  }

  /*
    performAssertion
   */

  test("performAssertion - STRING_FROM_LIST") {
    val result: Boolean = ResponseExtractJson.performAssertion(responseWithID, assertionStringFromList)
    assert(result)
    assertEquals("382be85a-1f00-4c15-b607-cbda03ccxxx2", RuntimeCache.get("question_id").get)
  }

  test("performAssertion - unsupported assertion") {
    intercept[IllegalArgumentException] {
      ResponseExtractJson.performAssertion(responseWithID, assertionUnsupported)
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
      ResponseExtractJson.stringFromList(responseWithID, cacheKey, listIndex, jsonKey, runtimeCacheLevel)
    }
  }

  test("stringFromList - incorrect parameters - wrong jsonKey") {
    val cacheKey = "question_id"
    val listIndex = 0
    val jsonKey = "ids"
    val runtimeCacheLevel = "Test"
    val result = ResponseExtractJson.stringFromList(responseWithID, cacheKey, listIndex, jsonKey, runtimeCacheLevel)
    assert(!result)
  }

  test("stringFromList - incorrect parameters - no json body in response") {
    val cacheKey = "question_id"
    val listIndex = 0
    val jsonKey = "id"
    val runtimeCacheLevel = "Test"

    val result = ResponseExtractJson.stringFromList(responseNoJsonBody, cacheKey, listIndex, jsonKey, runtimeCacheLevel)
    assert(!result)
  }

  test("stringFromList - incorrect parameters - no json arrays in response body") {
    val cacheKey = "question_id"
    val listIndex = 0
    val jsonKey = "id"
    val runtimeCacheLevel = "Test"

    val result = ResponseExtractJson.stringFromList(responseJsonNoArrayBody, cacheKey, listIndex, jsonKey, runtimeCacheLevel)
    assert(!result)
  }

  /*
    validateStringFromList
   */

  // positive test "stringFromList - correct parameters" - tested during "validateContent - STRING_FROM_LIST"

  test("validateStringFromList - None parameters") {
    val assertion2None: Assertion = Assertion(Response.GROUP_EXTRACT_JSON, ResponseExtractJson.STRING_FROM_LIST, "", None, None, None)
    val assertion3None: Assertion = Assertion(Response.GROUP_EXTRACT_JSON, ResponseExtractJson.STRING_FROM_LIST, "", Some(""), None, None)

    interceptMessage[ContentValidationFailed]("Content validation failed for value: 'ExtractJson.string-from-list.param_2': Input cannot be None") {
      ResponseExtractJson.validateStringFromList(assertion2None)
    }
    interceptMessage[ContentValidationFailed]("Content validation failed for value: 'ExtractJson.string-from-list.param_3': Input cannot be None") {
      ResponseExtractJson.validateStringFromList(assertion3None)
    }
  }

  test("validateStringFromList - empty parameters") {
    val assertionParam1: Assertion = Assertion(Response.GROUP_EXTRACT_JSON, ResponseExtractJson.STRING_FROM_LIST, "", Some(""), Some(""), Some(""))
    val assertionParam2: Assertion = Assertion(Response.GROUP_EXTRACT_JSON, ResponseExtractJson.STRING_FROM_LIST, "1", Some(""), Some(""), Some(""))
    val assertionParam3: Assertion = Assertion(Response.GROUP_EXTRACT_JSON, ResponseExtractJson.STRING_FROM_LIST, "1", Some("x"), Some(""), Some(""))

    interceptMessage[ContentValidationFailed]("Content validation failed for value: '': Received string value of 'ExtractJson.string-from-list.param_1' is empty.") {
      ResponseExtractJson.validateStringFromList(assertionParam1)
    }
    interceptMessage[ContentValidationFailed]("Content validation failed for value: '': Received string value of 'ExtractJson.string-from-list.param_2' is empty.") {
      ResponseExtractJson.validateStringFromList(assertionParam2)
    }
    interceptMessage[ContentValidationFailed]("Content validation failed for value: '': Received string value of 'ExtractJson.string-from-list.param_3' is empty.") {
      ResponseExtractJson.validateStringFromList(assertionParam3)
    }
  }

  test("validateStringFromList - not integer in string") {
    val assertion: Assertion = Assertion(Response.GROUP_EXTRACT_JSON, ResponseExtractJson.STRING_FROM_LIST, "key", Some("x"), Some("y"), Some("y"))
    interceptMessage[ContentValidationFailed]("Content validation failed for value: 'x': Received value of 'ExtractJson.string-from-list.param_2' cannot be parsed to an integer: For input string: \"x\"") {
      ResponseExtractJson.validateStringFromList(assertion)
    }

  }
}
