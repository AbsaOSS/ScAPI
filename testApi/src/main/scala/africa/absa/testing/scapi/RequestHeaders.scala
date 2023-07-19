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

package africa.absa.testing.scapi

import africa.absa.testing.scapi.utils.ContentValidator

object RequestHeaders {
  private val CONTENT_TYPE = "content-type"
  private val AUTHORIZATION = "authorization"

  def buildHeaders(headersSet: Set[Header]): Map[String, String] = {
    headersSet.foldLeft(Map.empty[String, String]) {
      (acc, header) => header.name.toLowerCase match {
        case CONTENT_TYPE => acc + (header.name -> header.value)
        case AUTHORIZATION => acc + (header.name -> s"${header.value}")
        case _ => acc + (header.name -> header.value)
        // this place does solve syntax validation, content validation is solved on another place
      }
    }
  }

  def validateContent(header: Header): Unit = {
    header.name.toLowerCase match {
      case CONTENT_TYPE => ContentValidator.validateNonEmptyString(header.value)
      case AUTHORIZATION => ContentValidator.validateNonEmptyString(header.value)
      case _ => throw UndefinedHeaderType(header.name)
    }
  }
}
