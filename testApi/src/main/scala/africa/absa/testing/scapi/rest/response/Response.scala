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
import africa.absa.testing.scapi.rest.model.CookieValue
import africa.absa.testing.scapi.rest.response.action.types.ResponseActionGroupType
import africa.absa.testing.scapi.rest.response.action.{AssertionResponseAction, ExtractJsonResponseAction, LogResponseAction}

import scala.util.{Failure, Success, Try}

case class Response(statusCode: Int,
                    body: String,
                    url: String,
                    statusMessage: String,
                    headers: Map[String, Seq[String]],
                    cookies: Map[String, CookieValue],
                    duration: Long) {

  def toRichString: String = {
    val headerString = headers.map { case (name, value) => s"  $name: $value" }.mkString("\n")
    val cookieString = cookies.map { case (name, value) => s"  $name: $value" }.mkString("\n")

    s"""
       |URL: $url
       |Status: $statusCode $statusMessage
       |Duration: ${duration}ms
       |Headers:
       |$headerString
       |Cookies:
       |$cookieString
       |Body:
       |$body
       |""".stripMargin
  }
}

/**
 * A singleton object that is responsible for managing and handling responses.
 */
object Response {

  /**
   * Validates an ResponseAction based on its group type.
   * Calls the appropriate group's validateContent method based on group type.
   *
   * @param responseAction The responseAction to be validated.
   * @throws IllegalArgumentException If the response action group is not supported.
   */
  def validate(responseAction: ResponseAction): Unit = {
    responseAction.group match {
      case ResponseActionGroupType.Assert => AssertionResponseAction.validateContent(responseAction)
      case ResponseActionGroupType.ExtractJson => ExtractJsonResponseAction.validateContent(responseAction)
      case ResponseActionGroupType.Log => LogResponseAction.validateContent(responseAction)
      case _ => throw new IllegalArgumentException(s"Unsupported assertion group: ${responseAction.group}")
    }
  }

  /**
   * Executes a sequence of response actions on the provided Response object based on a set of ResponseActions.
   * Each response action is resolved using runtime cache before execution.
   * This method delegates the assertion execution to the corresponding performAssertions method
   * based on the group type of each ResponseAction within the sequence.
   * The method returns successfully only if all assertions pass.
   * If any assertion fails, the method returns a failed Try with an appropriate exception.
   *
   * @param response        The HTTP response object on which the actions will be performed.
   * @param responseActions A sequence of ResponseAction objects representing the actions and assertions
   *                        to be performed on the response.
   * @return                A Try[Unit] indicating success if all response actions pass, or containing
   *                        an exception if any response action fails.
   * @throws IllegalArgumentException If an unsupported response action group is encountered.
   */
  def perform(response: Response, responseActions: Seq[ResponseAction]): Try[Unit] = {
    def logParameters(response: Response, resolvedResponseAction: ResponseAction, exception: Option[Throwable] = None): Unit = {
      val filteredParams = resolvedResponseAction.params
        .flatMap {
          case (k, v) if k != "method" => Some(s"$k->$v")
          case _ => None
        }
        .mkString(", ")
      val baseLog =
        s"""
           |Parameters received:
           |  Required Response-Action:
           |    Group->'${resolvedResponseAction.group}',
           |    Method->'${resolvedResponseAction.name}',
           |    Params->'$filteredParams',
           |  Actual Response:
           |    $response""".stripMargin
      val exceptionLog = exception.map(e => s"\nException: ${e.getMessage}").getOrElse("")
      Logger.debug(s"Response-${resolvedResponseAction.group}: '${resolvedResponseAction.name}' - error details:$baseLog$exceptionLog")
    }

    responseActions.map { responseAction =>
      val resolvedResponseAction: ResponseAction = responseAction.resolveByRuntimeCache()
      Logger.debug(s"Response-${resolvedResponseAction.group}: '${resolvedResponseAction.name}' - Started.")

      val res: Try[Unit] = resolvedResponseAction.group match {
        case ResponseActionGroupType.Assert => AssertionResponseAction.performResponseAction(response, resolvedResponseAction)
        case ResponseActionGroupType.ExtractJson => ExtractJsonResponseAction.performResponseAction(response, resolvedResponseAction)
        case ResponseActionGroupType.Log => LogResponseAction.performResponseAction(response, resolvedResponseAction)
        case _ => Failure(new IllegalArgumentException(s"Unsupported assertion group: ${resolvedResponseAction.group}"))
      }

      res match {
        case Success(_) =>
          Logger.debug(s"Response-${resolvedResponseAction.group}: '${resolvedResponseAction.name}' - completed successfully.")
        case Failure(e: IllegalArgumentException) =>
          Logger.debug(s"Response-${resolvedResponseAction.group}: '${resolvedResponseAction.name}' - failed with exception.")
          logParameters(response, resolvedResponseAction, Some(e))
        case Failure(e) =>
          Logger.debug(s"Response-${resolvedResponseAction.group}: '${resolvedResponseAction.name}' - failed with unexpected exception.")
          logParameters(response, resolvedResponseAction, Some(e))
      }
      res
    }.find(_.isFailure).getOrElse(Success(()))
  }
}
