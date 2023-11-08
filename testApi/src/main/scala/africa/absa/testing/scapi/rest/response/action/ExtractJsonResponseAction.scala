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
import africa.absa.testing.scapi.rest.response.action.types.ExtractJsonResponseActionType._
import africa.absa.testing.scapi.utils.cache.RuntimeCache
import africa.absa.testing.scapi.utils.validation.ContentValidator
import africa.absa.testing.scapi.{AssertionException, UndefinedResponseActionTypeException}
import spray.json._

import scala.util.Try

/**
 * ExtractJsonResponseAction is an object that extends ResponsePerformer.
 * It is designed to extract specific data from a JSON response and perform validations.
 */
object ExtractJsonResponseAction extends ResponseActions {

  /**
   * Validates the content of an extract response action object depending on its type.
   *
   * @param responseAction The ResponseAction instance to be validated.
   * @throws UndefinedResponseActionTypeException if an unsupported assertion type is encountered.
   */
  def validateContent(responseAction: ResponseAction): Unit = {
    val action = fromString(responseAction.name.toLowerCase).getOrElse(None)
    action match {
      case StringFromList => validateStringFromList(responseAction)
      case _ => throw UndefinedResponseActionTypeException(responseAction.name)
    }
  }

  /**
   * Performs extract actions on a response depending on the type of assertion method provided.
   *
   * @param response       The Response instance to perform response action on.
   * @param responseAction The ResponseAction instance containing the response action details.
   * @throws UndefinedResponseActionTypeException if an unsupported response action name is encountered.
   */
  def performResponseAction(response: Response, responseAction: ResponseAction): Try[Unit] = {
    val action = fromString(responseAction.name.toLowerCase).getOrElse(None)
    action match {
      case StringFromList =>
        val cacheKey = responseAction.params("cacheKey")
        val listIndex = responseAction.params("listIndex").toInt
        val jsonKey = responseAction.params("jsonKey")
        val cacheLevel = responseAction.params("cacheLevel")

        stringFromList(response, cacheKey, listIndex, jsonKey, cacheLevel)
      case _ => throw UndefinedResponseActionTypeException(s"Unsupported assertion[group: extract]: ${responseAction.name}")
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
   * @return A Try[Unit] indicating whether the string extraction and caching operation was successful or not.
   */
  private def stringFromList(response: Response, cacheKey: String, listIndex: Int, jsonKey: String, runtimeCacheLevel: String): Try[Unit] = {
    Try {
      val jsonAst = response.body.parseJson

      val objects = jsonAst match {
        case JsArray(array) => array
        case _ =>
          Logger.error("Expected a JSON array")
          throw AssertionException("Expected a JSON array in the response.")
      }

      // Extract "jsonKey" from the object at the given index
      val value: String = objects(listIndex).asJsObject.getFields(jsonKey) match {
        case Seq(JsString(value)) => value
        case Seq(JsNumber(value)) => value.toString()
        case _ =>
          Logger.error(s"Expected '$jsonKey' field not found in provided json.")
          throw AssertionException(s"Expected '$jsonKey' field not found in provided json.")
      }

      RuntimeCache.put(key = cacheKey, value = value, RuntimeCache.determineLevel(runtimeCacheLevel))
      Logger.debug(s"Extracted string '$value' from json array at index $listIndex and stored it in runtime cache with key '$cacheKey' and expiration level '$runtimeCacheLevel'.")
    } recover {
      case e: spray.json.JsonParser.ParsingException =>
        Logger.error(s"Expected json string in response body. JSON parsing error: ${e.getMessage}")
        throw AssertionException(s"Expected json string in response body. JSON parsing error: ${e.getMessage}")
    }
  }

  /**
   * This method validates the parameters of the StringFromList type of response action.
   * It ensures none of the required parameters are None and validates that they are non-empty strings.
   * Additionally, it ensures param_2 is a valid integer.
   *
   * @param assertion The ResponseAction instance containing the response action details.
   */
  private def validateStringFromList(assertion: ResponseAction): Unit = {
    val cacheKey = assertion.params.getOrElse("cacheKey", throw new IllegalArgumentException(s"Missing required 'cacheKey' parameter for extract $StringFromList logic"))
    val listIndex = assertion.params.getOrElse("listIndex", throw new IllegalArgumentException(s"Missing required 'listIndex' parameter for extract $StringFromList logic"))
    val jsonKey = assertion.params.getOrElse("jsonKey", throw new IllegalArgumentException(s"Missing required 'jsonKey' parameter for extract $StringFromList logic"))
    val cacheLevel = assertion.params.getOrElse("cacheLevel", throw new IllegalArgumentException(s"Missing required 'cacheLevel' parameter for extract $StringFromList logic"))

    ContentValidator.validateNonEmptyString(cacheKey, s"ExtractJson.$StringFromList.cacheKey")
    ContentValidator.validateNonEmptyString(listIndex, s"ExtractJson.$StringFromList.listIndex")
    ContentValidator.validateNonEmptyString(jsonKey, s"ExtractJson.$StringFromList.jsonKey")
    ContentValidator.validateNonEmptyString(cacheLevel, s"ExtractJson.$StringFromList.cacheLevel")

    ContentValidator.validateIntegerString(listIndex, s"ExtractJson.$StringFromList.listIndex")
  }

}
