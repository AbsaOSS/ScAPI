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

import africa.absa.testing.scapi.UndefinedResponseActionType
import africa.absa.testing.scapi.json.ResponseAction
import africa.absa.testing.scapi.logging.Logger
import africa.absa.testing.scapi.utils.validation.ContentValidator

/**
 * Singleton object `ResponseLog` that extends the `ResponsePerformer` trait.
 * It provides utilities for validating and performing logging messages.
 */
object LogResponseAction extends ResponsePerformer {

  val INFO = "info"

  /**
   * Validates the content of an log response action object depending on its type.
   *
   * @param responseAction The response action to be validated.
   * @throws UndefinedResponseActionType if the response action's name is not recognized.
   */
  def validateContent(responseAction: ResponseAction): Unit = {
    responseAction.name.toLowerCase match {
      case INFO =>
        responseAction.params.get("message") match {
          case message => ContentValidator.validateNonEmptyString(message.get, s"ResponseLog.$INFO.message")
          case None => throw new IllegalArgumentException(s"Missing required 'message' for assertion $INFO logic.")
        }
      case _ => throw UndefinedResponseActionType(responseAction.name)
    }
  }

  /**
   * Performs log actions on a response depending on the type of log method provided.
   *
   * @param response  The response on which the response action are to be performed.
   * @param responseAction The responseAction to be performed on the response.
   * @throws IllegalArgumentException if the response action's name is not recognized.
   */
  def performResponseAction(response: Response, responseAction: ResponseAction): Boolean = {
    responseAction.name match {
      case INFO =>
        val message = responseAction.params("message")
        logInfo(message)
      case _ => throw new IllegalArgumentException(s"Unsupported log method [group: log]: ${responseAction.name}")
    }
  }

  /*
    dedicated actions
   */

  /**
   * This method logs a message at the INFO level.
   *
   * @param message The message to be logged.
   */
  def logInfo(message: String): Boolean = {
    Logger.info(message)
    true
  }
}
