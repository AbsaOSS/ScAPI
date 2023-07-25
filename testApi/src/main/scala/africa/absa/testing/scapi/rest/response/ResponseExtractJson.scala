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

import africa.absa.testing.scapi.UndefinedAssertionType
import africa.absa.testing.scapi.json.Assertion
import africa.absa.testing.scapi.utils.ContentValidator
import africa.absa.testing.scapi.utils.cache.RuntimeCache
import spray.json._

object ResponseExtractJson extends ResponsePerformer {

  val STRING_FROM_LIST = "string-from-list"

  def validateContent(assertion: Assertion): Unit = {
    assertion.name.toLowerCase match {
      case STRING_FROM_LIST => validateStringFromList(assertion)
      case _ => throw UndefinedAssertionType(assertion.name)
    }
  }

  def performAssertions(response: Response, assertion: Assertion): Unit = {
    assertion.name match {
      case STRING_FROM_LIST => stringFromList(response, assertion.param_1, assertion.param_2.get.toInt, assertion.param_3.get, assertion.param_4.get)
      case _ => throw new IllegalArgumentException(s"Unsupported assertion[group: extract]: ${assertion.name}")
    }
  }

  /*
    dedicated actions
   */
  def stringFromList(response: Response, cacheKey: String, listIndex: Int, jsonKey: String, cacheExpirationLevel: String): Unit = {
    val jsonAst = response.body.parseJson

    val objects = jsonAst match {
      case JsArray(array) => array
      case _ => throw DeserializationException("Expected a JSON array")
    }

    // Extract "jsonKey" from the object at the given index
    val value: String = objects(listIndex).asJsObject.getFields(jsonKey) match {
      case Seq(JsString(value)) => value
      case _ => throw new DeserializationException(s"Expected a single '$jsonKey' field of type string")
    }

    RuntimeCache.put(key = cacheKey, value = value, RuntimeCache.determineLevel(cacheExpirationLevel))
  }

  def validateStringFromList(assertion: Assertion): Unit = {
    ContentValidator.validateNotNone(assertion.param_2, s"ExtractJson.$STRING_FROM_LIST.param_2")
    ContentValidator.validateNotNone(assertion.param_3, s"ExtractJson.$STRING_FROM_LIST.param_3")

    ContentValidator.validateNonEmptyString(assertion.param_1)
    ContentValidator.validateNonEmptyString(assertion.param_2.get)
    ContentValidator.validateNonEmptyString(assertion.param_3.get)

    ContentValidator.validateIntegerString(assertion.param_2.get)
  }
}
