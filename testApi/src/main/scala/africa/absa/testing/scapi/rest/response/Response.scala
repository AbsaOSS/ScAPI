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

import africa.absa.testing.scapi.json.Assertion

case class Response(statusCode: Int, body: String, headers: Map[String, Seq[String]])

object Response {

  val GROUP_ASSERT: String = "assert"
  val GROUP_EXTRACT_JSON: String = "extractJson"
  val GROUP_LOG: String = "log"

  def validate(assertion: Assertion): Unit = {
    assertion.group match {
      case GROUP_ASSERT => ResponseAssertion.validateContent(assertion)
      case GROUP_EXTRACT_JSON => ResponseExtractJson.validateContent(assertion)
      case GROUP_LOG => ResponseLog.validateContent(assertion)
      case _ => throw new IllegalArgumentException(s"Unsupported assertion group: ${assertion.group}")
    }
  }

  def perform(response: Response, assertions: Set[Assertion]): Unit = {
    for (assertion <- assertions) {
      val resolvedAssertion: Assertion = assertion.resolveByRuntimeCache()
      resolvedAssertion.group match {
        case GROUP_ASSERT => ResponseAssertion.performAssertions(response, assertion)
        case GROUP_EXTRACT_JSON => ResponseExtractJson.performAssertions(response, assertion)
        case GROUP_LOG => ResponseLog.performAssertions(response, assertion)
        case _ => throw new IllegalArgumentException(s"Unsupported assertion group: ${assertion.group}")
      }
    }
  }
}
