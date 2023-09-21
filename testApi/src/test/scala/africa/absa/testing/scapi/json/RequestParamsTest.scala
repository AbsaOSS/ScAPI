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

import africa.absa.testing.scapi.ContentValidationFailedException
import africa.absa.testing.scapi.rest.request.RequestParams
import munit.FunSuite

class RequestParamsTest extends FunSuite {

  /*
    buildParams
   */

  test("buildParams - no params") {
    val paramsSet: Option[Set[Param]] = None
    val result: Map[String, String] = RequestParams.buildParams(paramsSet)
    assertEquals(result, Map.empty[String, String])
  }

  test("buildParams - single valid param") {
    val paramsSet: Option[Set[Param]] = Some(Set(Param("name", "value")))
    val result: Map[String, String] = RequestParams.buildParams(paramsSet)
    assertEquals(result, Map("name" -> "value"))
  }

  test("buildParams - multiple valid params") {
    val paramsSet: Option[Set[Param]] = Some(Set(Param("name1", "value1"), Param("name2", "value2")))
    val result: Map[String, String] = RequestParams.buildParams(paramsSet)
    assertEquals(result, Map("name1" -> "value1", "name2" -> "value2"))
  }

  test("buildParams - params with empty name or value should be ignored") {
    val paramsSet: Option[Set[Param]] = Some(Set(Param("name", ""), Param("", "value"), Param("name2", "value2")))
    val result: Map[String, String] = RequestParams.buildParams(paramsSet)
    assertEquals(result, Map("name2" -> "value2"))
  }

  /*
    validateContent
   */

  test("validateContent - should not throw exception for valid names") {
    val params = Set(Param("param1", "value1"), Param("param2", "value2"))
    RequestParams.validateContent(Some(params))
  }

  test("validateContent - should throw exception for empty name") {
    val params = Set(Param("", "value"))
    intercept[ContentValidationFailedException] {
      RequestParams.validateContent(Some(params))
    }
  }

  test("validateContent - should not throw exception for None") {
    RequestParams.validateContent(None)
  }
}
