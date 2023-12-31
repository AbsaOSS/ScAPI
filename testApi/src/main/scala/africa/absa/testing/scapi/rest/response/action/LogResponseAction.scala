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

package africa.absa.testing.scapi.rest.response.action

import africa.absa.testing.scapi.json.ResponseAction
import africa.absa.testing.scapi.logging.Logger
import africa.absa.testing.scapi.rest.response.Response
import africa.absa.testing.scapi.rest.response.action.types.LogResponseActionType._
import africa.absa.testing.scapi.utils.validation.ContentValidator
import africa.absa.testing.scapi.{PropertyNotFoundException, UndefinedResponseActionTypeException}

import scala.util.{Failure, Try}

/**
 * Singleton object `ResponseLog` that extends the `ResponsePerformer` trait.
 * It provides utilities for validating and performing logging messages.
 */
object LogResponseAction extends ResponseActions {

  /**
   * Validates the content of an log response action object depending on its type.
   *
   * @param responseAction The response action to be validated.
   * @throws UndefinedResponseActionTypeException if the response action's name is not recognized.
   */
  def validateContent(responseAction: ResponseAction): Unit = {
    Logger.debug(s"Validating content for response action. \nResponseAction: $responseAction")

    val action = fromString(responseAction.name.toLowerCase).getOrElse(None)
    action match {
      case LogInfoResponse => ()
      case Error | Warn | Info | Debug =>
        responseAction.params.get("message") match {
          case Some(message) => ContentValidator.validateNonEmptyString(message, s"ResponseLog.${responseAction.name}.message")
          case None => throw new IllegalArgumentException(s"Missing required 'message' for assertion ${responseAction.name} logic.")
        }
      case _ => throw UndefinedResponseActionTypeException(responseAction.name)
    }
  }

  /**
   * Performs log actions on a response depending on the type of log method provided.
   *
   * @param response       The response on which the response action is to be performed.
   * @param responseAction The response action to be performed on the response.
   * @throws UndefinedResponseActionTypeException if the response action's name is not recognized.
   * @throws PropertyNotFoundException if the required 'message' parameter is missing.
   */
  def performResponseAction(response: Response, responseAction: ResponseAction): Try[Unit] = {
    Logger.trace(s"Performing response action. \nResponse: ${response.toRichString}, \nResponseAction: ${responseAction.toRichString}")

    val action = fromString(responseAction.name.toLowerCase).getOrElse(None)
    Try {
      action match {
        case Error | Warn | Info | Debug =>
          val message = responseAction.params.getOrElse("message", return Failure(PropertyNotFoundException("Missing 'message' parameter")))
          action match {
            case Error => logError (message)
            case Warn => logWarn (message)
            case Info => logInfo (message)
            case Debug => logDebug (message)
          }
        case LogInfoResponse => logInfoResponse(response: Response)
        case _ => Failure(UndefinedResponseActionTypeException(s"Unsupported log method [group: log]: ${responseAction.name}"))
      }
    }
  }

  /*
    dedicated actions
   */

  /**
   * Logs a message at the ERROR level.
   *
   * @param message The message to be logged.
   */
  private def logError(message: String): Unit = {
    Logger.error(message)
  }

  /**
   * Logs a message at the WARN level.
   *
   * @param message The message to be logged.
   */
  private def logWarn(message: String): Unit = {
    Logger.warn(message)
  }

  /**
   * Logs a message at the INFO level.
   *
   * @param message The message to be logged.
   */
  private def logInfo(message: String): Unit = {
    Logger.info(message)
  }

  /**
   * Logs a message at the DEBUG level.
   *
   * @param message The message to be logged.
   */
  private def logDebug(message: String): Unit = {
    Logger.debug(message)
  }

  /**
   * Logs a response data at the INFO level.
   */
  private def logInfoResponse(response: Response): Unit = {
    Logger.info(s"Printing response info:${response.toRichString}")
  }
}
