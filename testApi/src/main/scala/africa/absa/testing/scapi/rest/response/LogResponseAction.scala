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

import africa.absa.testing.scapi.UndefinedResponseActionTypeException
import africa.absa.testing.scapi.json.ResponseAction
import africa.absa.testing.scapi.logging.Logger
import africa.absa.testing.scapi.rest.response.LogResponseActionType._
import africa.absa.testing.scapi.utils.validation.ContentValidator

import scala.util.{Failure, Success, Try}

/**
 * Singleton object `ResponseLog` that extends the `ResponsePerformer` trait.
 * It provides utilities for validating and performing logging messages.
 */
object LogResponseAction extends ResponsePerformer {

  /**
   * Validates the content of an log response action object depending on its type.
   *
   * @param responseAction The response action to be validated.
   * @throws UndefinedResponseActionTypeException if the response action's name is not recognized.
   */
  def validateContent(responseAction: ResponseAction): Unit = {
    val action = fromString(responseAction.name.toLowerCase).getOrElse(None)
    action match {
      case ERROR | WARN | INFO | DEBUG =>
        responseAction.params.get("message") match {
          case Some(message) => ContentValidator.validateNonEmptyString(message, s"ResponseLog.${responseAction.name}.message")
          case None => throw new IllegalArgumentException(s"Missing required 'message' for assertion ${responseAction.name} logic.")
        }
      case _ => throw UndefinedResponseActionTypeException(responseAction.name.toString)
    }
  }

  /**
   * Performs log actions on a response depending on the type of log method provided.
   *
   * @param response  The response on which the response action are to be performed.
   * @param responseAction The responseAction to be performed on the response.
   * @throws IllegalArgumentException if the response action's name is not recognized.
   */
  def performResponseAction(response: Response, responseAction: ResponseAction): Try[Unit] = {
    val message = responseAction.params.getOrElse("message", return Failure(new IllegalArgumentException("Missing 'message' parameter")))
    val action = fromString(responseAction.name.toLowerCase).getOrElse(None)
    action match {
      case ERROR => logError(message)
      case WARN => logWarn(message)
      case INFO => logInfo(message)
      case DEBUG => logDebug(message)
      case _ => Failure(new IllegalArgumentException(s"Unsupported log method [group: log]: ${responseAction.name}"))
    }
  }

  /*
    dedicated actions
   */

  /**
   * This method logs a message at the ERROR level.
   *
   * @param message The message to be logged.
   */
  def logError(message: String): Try[Unit] = {
    Logger.error(message)
    Success(())
  }

  /**
   * This method logs a message at the WARN level.
   *
   * @param message The message to be logged.
   */
  def logWarn(message: String): Try[Unit] = {
    Logger.warn(message)
    Success(())
  }

  /**
   * This method logs a message at the INFO level.
   *
   * @param message The message to be logged.
   */
  def logInfo(message: String): Try[Unit] = {
    Logger.info(message)
    Success(())
  }

  /**
   * This method logs a message at the DEBUG level.
   *
   * @param message The message to be logged.
   */
  def logDebug(message: String): Try[Unit] = {
    Logger.debug(message)
    Success(())
  }

}
