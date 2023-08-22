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
import munit.FunSuite

class ResponseTest extends FunSuite {

  /*
    validate
   */

  test("validate - unsupported group") {
    val unsupportedAssertion = ResponseAction(method = "unsupportedGroup.not needed", Map("param 1" -> ""))

    intercept[IllegalArgumentException] {
      Response.validate(unsupportedAssertion)
    }
  }

  /*
    perform
   */

  test("perform - unsupported group") {
    val unsupportedAssertion = ResponseAction(method = "unsupportedGroup.not needed", Map("param 1" -> ""))
    val response = Response(200, "OK", Map.empty)

    intercept[IllegalArgumentException] {
      Response.perform(response, Set(unsupportedAssertion))
    }
  }

}
