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
import africa.absa.testing.scapi.utils.cache.RuntimeCache
import africa.absa.testing.scapi.utils.validation.ContentValidator
import spray.json._

/**
 * ExtractJsonResponseAction is an object that extends ResponsePerformer.
 * It is designed to extract specific data from a JSON response and perform validations.
 */
object ExtractJsonResponseAction extends ResponsePerformer {

  val STRING_FROM_LIST = "string-from-list"

  /**
   * This method validates the response action's content based on the response action's name.
   * It supports the STRING_FROM_LIST type of response action.
   *
   * @param responseAction The ResponseAction instance to be validated.
   * @throws UndefinedResponseActionType if an unsupported assertion type is encountered.
   */
  def validateContent(responseAction: ResponseAction): Unit = {
    responseAction.name.toLowerCase match {
      case STRING_FROM_LIST => validateStringFromList(responseAction)
      case _ => throw UndefinedResponseActionType(responseAction.name)
    }
  }

  /**
   * This method performs response actions on a given response based on the response action's name.
   * It supports the STRING_FROM_LIST type of response action.
   *
   * @param response  The Response instance to perform response action on.
   * @param responseAction The ResponseAction instance containing the response action details.
   * @throws IllegalArgumentException if an unsupported response action name is encountered.
   */
  def performResponseAction(response: Response, responseAction: ResponseAction): Boolean = {
    responseAction.name match {
      case STRING_FROM_LIST =>
        val param_1 = responseAction.params.getOrElse("param_1", throw new IllegalArgumentException("param_1 is missing"))
        val param_2 = responseAction.params.get("param_2").map(_.toInt).getOrElse(throw new IllegalArgumentException("param_2 is missing"))
        val param_3 = responseAction.params.getOrElse("param_3", throw new IllegalArgumentException("param_3 is missing"))
        val param_4 = responseAction.params.getOrElse("param_4", throw new IllegalArgumentException("param_4 is missing"))

        stringFromList(response, param_1, param_2, param_3, param_4)
      case _ => throw new IllegalArgumentException(s"Unsupported assertion[group: extract]: ${responseAction.name}")
    }
  }

  /*
    dedicated actions
   */

  /**
   * This method extracts a string from a JSON array response at a given index
   * and stores it in a runtime cache with a given key and expiration level.
   *
   * @param response             The Response instance containing the JSON body.
   * @param cacheKey             The key to use when storing the extracted string in the runtime cache.
   * @param listIndex            The index in the JSON array from which to extract the string.
   * @param jsonKey              The key in the JSON object from which to extract the string.
   * @param runtimeCacheLevel    The expiration level to use when storing the extracted string in the runtime cache.
   * @return Boolean indicating whether the string extraction and caching operation was successful.
   */
  def stringFromList(response: Response, cacheKey: String, listIndex: Int, jsonKey: String, runtimeCacheLevel: String): Boolean = {
    try {
      val jsonAst = response.body.parseJson

      val objects = jsonAst match {
        case JsArray(array) => array
        case _ =>
          Logger.error("Expected a JSON array")
          return false
      }

      // Extract "jsonKey" from the object at the given index
      val value: String = objects(listIndex).asJsObject.getFields(jsonKey) match {
        case Seq(JsString(value)) => value
        case Seq(JsNumber(value)) => value.toString()
        case _ =>
          Logger.error(s"Expected '$jsonKey' field not found in provided json.")
          return false
      }

      RuntimeCache.put(key = cacheKey, value = value, RuntimeCache.determineLevel(runtimeCacheLevel))
      true
    } catch {
      case e: spray.json.JsonParser.ParsingException =>
        Logger.error(s"Expected json string in response body. JSON parsing error: ${e.getMessage}")
        false
    }
  }

  /**
   * This method validates the parameters of the STRING_FROM_LIST type of response action.
   * It ensures none of the required parameters are None and validates that they are non-empty strings.
   * Additionally, it ensures param_2 is a valid integer.
   *
   * @param assertion The ResponseAction instance containing the response action details.
   */
  def validateStringFromList(assertion: ResponseAction): Unit = {
    val param_1 = assertion.params.getOrElse("param_1", throw new IllegalArgumentException("param_1 is missing"))
    val param_2 = assertion.params.getOrElse("param_2", throw new IllegalArgumentException("param_2 is missing"))
    val param_3 = assertion.params.getOrElse("param_3", throw new IllegalArgumentException("param_3 is missing"))
    val param_4 = assertion.params.getOrElse("param_4", throw new IllegalArgumentException("param_4 is missing"))

    ContentValidator.validateNonEmptyString(param_1, s"ExtractJson.$STRING_FROM_LIST.param_1")
    ContentValidator.validateNonEmptyString(param_2, s"ExtractJson.$STRING_FROM_LIST.param_2")
    ContentValidator.validateNonEmptyString(param_3, s"ExtractJson.$STRING_FROM_LIST.param_3")
    ContentValidator.validateNonEmptyString(param_4, s"ExtractJson.$STRING_FROM_LIST.param_4")

    ContentValidator.validateIntegerString(param_2, s"ExtractJson.$STRING_FROM_LIST.param_2")
  }

}
