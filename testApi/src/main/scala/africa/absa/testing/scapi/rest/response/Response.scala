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
import africa.absa.testing.scapi.logging.Logger

case class Response(statusCode: Int, body: String, url: String, statusMessage: String, headers: Map[String, Seq[String]], cookies: Map[String, String], duration: Long)

/**
 * A singleton object that is responsible for managing and handling responses.
 */
object Response {

  val GROUP_ASSERT: String = "assert"
  val GROUP_EXTRACT_JSON: String = "extractJson"
  val GROUP_LOG: String = "log"

  /**
   * Validates an ResponseAction based on its group type.
   * Calls the appropriate group's validateContent method based on group type.
   *
   * @param responseAction The responseAction to be validated.
   * @throws IllegalArgumentException If the response action group is not supported.
   */
  def validate(responseAction: ResponseAction): Unit = {
    responseAction.group match {
      case GROUP_ASSERT => AssertionResponseAction.validateContent(responseAction)
      case GROUP_EXTRACT_JSON => ExtractJsonResponseAction.validateContent(responseAction)
      case GROUP_LOG => LogResponseAction.validateContent(responseAction)
      case _ => throw new IllegalArgumentException(s"Unsupported assertion group: ${responseAction.group}")
    }
  }

  /**
   * Performs actions on the given Response based on a set of Assertions.
   * Each response action is resolved by the runtime cache before being used.
   * The appropriate group's performAssertions method is called based on the group type of each Assertion.
   * Returns true only if all assertions return true, and false as soon as any assertion returns false.
   *
   * @param response   The response on which actions will be performed.
   * @param responseAction The set of response actions that dictate what actions will be performed on the response.
   * @return           Boolean indicating whether all response actions passed (true) or any response action failed (false).
   * @throws IllegalArgumentException If an response action group is not supported.
   */
  def perform(response: Response, responseAction: Seq[ResponseAction]): Boolean = {
    def logParameters(response: Response, resolvedResponseAction: ResponseAction, exception: Option[Throwable] = None): Unit = {
      val filteredParams = resolvedResponseAction.params.filter(_._1 != "method").map { case (k, v) => s"$k->$v" }.mkString(", ")
      val baseLog = s"\nParameters received: \n\tRequired Response-Action: \n\t\tGroup->'${resolvedResponseAction.group}', \n\t\tMethod->'${resolvedResponseAction.name}', \n\t\tParams->'${filteredParams}', \n\tActual Response: \n\t\t$response"
      val exceptionLog = exception.map(e => s"\nException: ${e.getMessage}").getOrElse("")
      Logger.debug(s"Response-${resolvedResponseAction.group}: '${resolvedResponseAction.name}' - error details:$baseLog$exceptionLog")
    }

    responseAction.forall { assertion =>
      val resolvedResponseAction: ResponseAction = assertion.resolveByRuntimeCache()
      Logger.debug(s"Response-${resolvedResponseAction.group}: '${resolvedResponseAction.name}' - Started.")

      try {
        val res = resolvedResponseAction.group match {
          case GROUP_ASSERT => AssertionResponseAction.performResponseAction(response, assertion)
          case GROUP_EXTRACT_JSON => ExtractJsonResponseAction.performResponseAction(response, assertion)
          case GROUP_LOG => LogResponseAction.performResponseAction(response, assertion)
          case _ => throw new IllegalArgumentException(s"Unsupported assertion group: ${assertion.group}")
        }

        Logger.debug(s"Response-${resolvedResponseAction.group}: '${resolvedResponseAction.name}' - ${if (res) "completed successfully" else "failed"}.")
        if (!res) logParameters(response, resolvedResponseAction)
        res
      } catch {
        case e: IllegalArgumentException =>
          Logger.debug(s"Response-${resolvedResponseAction.group}: '${resolvedResponseAction.name}' - failed with exception.")
          logParameters(response, resolvedResponseAction, Some(e))
          throw e
      }
    }
  }
}
