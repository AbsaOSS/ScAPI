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

package africa.absa.testing.scapi.rest.response.action.types

import scala.language.implicitConversions

object AssertResponseActionType extends Enumeration {
  type AssertResponseActionType = Value

  // response-time-...
  val RESPONSE_TIME_IS_BELOW: AssertResponseActionType.Value = Value("response-time-is-below")
  val RESPONSE_TIME_IS_ABOVE: AssertResponseActionType.Value = Value("response-time-is-above")

  // status-code-...
  val STATUS_CODE_EQUALS: AssertResponseActionType.Value = Value("status-code-equals")
  val STATUS_CODE_IS_SUCCESS: AssertResponseActionType.Value = Value("status-code-is-success")
  val STATUS_CODE_IS_CLIENT_ERROR: AssertResponseActionType.Value = Value("status-code-is-client-error")
  val STATUS_CODE_IS_SERVER_ERROR: AssertResponseActionType.Value = Value("status-code-is-server-error")

  // header-...
  val HEADER_EXISTS: AssertResponseActionType.Value = Value("header-exists")
  val HEADER_VALUE_EQUALS: AssertResponseActionType.Value = Value("header-value-equals")

  // content-type-...
  val CONTENT_TYPE_IS_JSON: AssertResponseActionType.Value = Value("content-type-is-json")
  val CONTENT_TYPE_IS_XML: AssertResponseActionType.Value = Value("content-type-is-xml")
  val CONTENT_TYPE_IS_HTML: AssertResponseActionType.Value = Value("content-type-is-html")

  // cookies-...
  val COOKIE_EXISTS: AssertResponseActionType.Value = Value("cookie-exists")
  val COOKIE_VALUE_EQUALS: AssertResponseActionType.Value = Value("cookie-value-equals")
  val COOKIE_IS_SECURED: AssertResponseActionType.Value = Value("cookie-is-secured")
  val COOKIE_IS_NOT_SECURED: AssertResponseActionType.Value = Value("cookie-is-not-secured")

  // body-...
  val BODY_CONTAINS_TEXT: AssertResponseActionType.Value = Value("body-contains-text")

  private val stringToValueMap = values.map(v => v.toString -> v).toMap

  def fromString(s: String): Option[AssertResponseActionType] = stringToValueMap.get(s)
}
