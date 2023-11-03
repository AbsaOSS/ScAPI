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

package africa.absa.testing.scapi.rest.request

import africa.absa.testing.scapi.UndefinedHeaderTypeException
import africa.absa.testing.scapi.json.Header
import africa.absa.testing.scapi.utils.cache.RuntimeCache
import africa.absa.testing.scapi.utils.validation.ContentValidator

/**
 * Represents an object to manage request headers.
 */
object RequestHeaders {
  val CONTENT_TYPE = "content-type"
  val AUTHORIZATION = "authorization"

  /**
   * Builds a Map of headers from a given set of Header objects.
   * Syntax validation is done here; content validation is handled elsewhere.
   *
   * @param headersSet A set of Header objects to be processed.
   * @return A Map where the key is the header name (lowercase) and the value is the resolved header value.
   */
  def buildHeaders(headersSet: Seq[Header]): Map[String, String] = {
    headersSet.foldLeft(Map.empty[String, String]) {
      (acc, header) => header.name.toLowerCase match {
        case CONTENT_TYPE => acc + (header.name -> RuntimeCache.resolve(header.value))
        case AUTHORIZATION => acc + (header.name -> s"${RuntimeCache.resolve(header.value)}")
        case _ => acc + (header.name -> RuntimeCache.resolve(header.value))
        // this place does solve syntax validation, content validation is solved on another place
      }
    }
  }

  /**
   * Validates the content of a given header.
   * For 'content-type' and 'authorization', it checks if the value is not an empty string.
   * For any other header type, it throws an UndefinedHeaderType exception.
   *
   * @param header The Header object to be validated.
   * @throws UndefinedHeaderTypeException If an undefined header type is encountered.
   */
  def validateContent(header: Header): Unit = {
    header.name.toLowerCase match {
      case CONTENT_TYPE => ContentValidator.validateNonEmptyString(header.value, s"Header.${header.name}")
      case AUTHORIZATION => ContentValidator.validateNonEmptyString(header.value, s"Header.${header.name}")
      case _ => throw UndefinedHeaderTypeException(header.name)
    }
  }
}
