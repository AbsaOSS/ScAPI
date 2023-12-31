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
import scala.util.Try

object AssertResponseActionType extends Enumeration {
  type AssertResponseActionType = Value

  // response-time-...
  val ResponseTimeIsBelow: AssertResponseActionType.Value = Value("response-time-is-below")
  val ResponseTimeIsAbove: AssertResponseActionType.Value = Value("response-time-is-above")

  // status-code-...
  val StatusCodeEquals: AssertResponseActionType.Value = Value("status-code-equals")
  val StatusCodeIsSuccess: AssertResponseActionType.Value = Value("status-code-is-success")
  val StatusCodeIsClientError: AssertResponseActionType.Value = Value("status-code-is-client-error")
  val StatusCodeIsServerError: AssertResponseActionType.Value = Value("status-code-is-server-error")

  // header-...
  val HeaderExists: AssertResponseActionType.Value = Value("header-exists")
  val HeaderValueEquals: AssertResponseActionType.Value = Value("header-value-equals")

  // content-type-...
  val ContentTypeIsJson: AssertResponseActionType.Value = Value("content-type-is-json")
  val ContentTypeIsXml: AssertResponseActionType.Value = Value("content-type-is-xml")
  val ContentTypeIsHtml: AssertResponseActionType.Value = Value("content-type-is-html")

  // cookies-...
  val CookieExists: AssertResponseActionType.Value = Value("cookie-exists")
  val CookieValueEquals: AssertResponseActionType.Value = Value("cookie-value-equals")
  val CookieIsSecured: AssertResponseActionType.Value = Value("cookie-is-secured")
  val CookieIsNotSecured: AssertResponseActionType.Value = Value("cookie-is-not-secured")

  // body-...
  val BodyEquals: AssertResponseActionType.Value = Value("body-equals")
  val BodyContainsText: AssertResponseActionType.Value = Value("body-contains-text")
  val BodyIsEmpty: AssertResponseActionType.Value = Value("body-is-empty")
  val BodyIsNotEmpty: AssertResponseActionType.Value = Value("body-is-not-empty")
  val BodyLengthEquals: AssertResponseActionType.Value = Value("body-length-equals")
  val BodyStartsWith: AssertResponseActionType.Value = Value("body-starts-with")
  val BodyEndsWith: AssertResponseActionType.Value = Value("body-ends-with")
  val BodyMatchesRegex: AssertResponseActionType.Value = Value("body-matches-regex")

  // body-json-...
  val BodyJsonIsJsonArray: AssertResponseActionType.Value = Value("body-json-is-json-array")
  val BodyJsonIsJsonObject: AssertResponseActionType.Value = Value("body-json-is-json-object")
  val BodyJsonPathExists: AssertResponseActionType.Value = Value("body-json-path-exists")

  // utils
  def fromString(s: String): Option[AssertResponseActionType] = Try(this.withName(s)).toOption
}
