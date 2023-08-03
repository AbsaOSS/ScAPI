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

/**
 * A singleton object that is responsible for managing and handling responses.
 */
object Response {

  val GROUP_ASSERT: String = "assert"
  val GROUP_EXTRACT_JSON: String = "extractJson"
  val GROUP_LOG: String = "log"

  /**
   * Validates an Assertion based on its group type.
   * Calls the appropriate group's validateContent method based on group type.
   *
   * @param assertion The assertion to be validated.
   * @throws IllegalArgumentException If the assertion group type is not supported.
   */
  def validate(assertion: Assertion): Unit = {
    assertion.group match {
      case GROUP_ASSERT => ResponseAssertion.validateContent(assertion)
      case GROUP_EXTRACT_JSON => ResponseExtractJson.validateContent(assertion)
      case GROUP_LOG => ResponseLog.validateContent(assertion)
      case _ => throw new IllegalArgumentException(s"Unsupported assertion group: ${assertion.group}")
    }
  }

  /**
   * Performs actions on the given Response based on a set of Assertions.
   * Each Assertion is resolved by the runtime cache before being used.
   * The appropriate group's performAssertions method is called based on the group type of each Assertion.
   * Returns true only if all assertions return true, and false as soon as any assertion returns false.
   *
   * @param response   The response on which actions will be performed.
   * @param assertions The set of assertions that dictate what actions will be performed on the response.
   * @return           Boolean indicating whether all assertions passed (true) or any assertion failed (false).
   * @throws IllegalArgumentException If an assertion group type is not supported.
   */
  def perform(response: Response, assertions: Set[Assertion]): Boolean = {
    assertions.forall { assertion =>
      val resolvedAssertion: Assertion = assertion.resolveByRuntimeCache()
      resolvedAssertion.group match {
        case GROUP_ASSERT => ResponseAssertion.performAssertion(response, assertion)
        case GROUP_EXTRACT_JSON => ResponseExtractJson.performAssertion(response, assertion)
        case GROUP_LOG => ResponseLog.performAssertion(response, assertion)
        case _ => throw new IllegalArgumentException(s"Unsupported assertion group: ${assertion.group}")
      }
    }
  }
}
