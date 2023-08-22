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
   * This method validates the content of the response action.
   * It checks if the response action's name is "info", then validates if its `param_1` is a non-empty string.
   * For all other response action names, it throws an `UndefinedAssertionType` exception.
   *
   * @param responseAction The response action to be validated.
   * @throws UndefinedResponseActionType if the response action's name is not recognized.
   */
  def validateContent(responseAction: ResponseAction): Unit = {
    responseAction.name.toLowerCase match {
      case INFO =>
        responseAction.params.get("param_1") match {
          case param_1 => ContentValidator.validateNonEmptyString(param_1.get, s"ResponseLog.$INFO.param_1")
          case None => throw new IllegalArgumentException(s"Missing required param_1 for assertion $INFO")
        }
      case _ => throw UndefinedResponseActionType(responseAction.name)
    }
  }

  /**
   * This method performs the necessary response actions on the response.
   * It checks if the response action's name is "info", then logs the `param_1` as info message.
   * For all other response action names, it throws an `IllegalArgumentException`.
   *
   * @param response  The response on which the response action are to be performed.
   * @param responseAction The responseAction to be performed on the response.
   * @throws IllegalArgumentException if the response action's name is not recognized.
   */
  def performResponseAction(response: Response, responseAction: ResponseAction): Boolean = {
    responseAction.name match {
      case INFO =>
        val param_1 = responseAction.params.getOrElse("param_1", throw new IllegalArgumentException("param_1 is missing"))
        logInfo(param_1)
      case _ => throw new IllegalArgumentException(s"Unsupported assertion[group: log]: ${responseAction.name}")
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
