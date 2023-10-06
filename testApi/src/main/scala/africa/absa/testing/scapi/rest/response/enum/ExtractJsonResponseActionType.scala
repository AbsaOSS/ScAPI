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

package africa.absa.testing.scapi.rest.response.`enum`

import scala.language.implicitConversions

object ExtractJsonResponseActionType extends Enumeration {
  type ExtractJsonResponseActionType = Value

  val STRING_FROM_LIST: ExtractJsonResponseActionType.Value = Value("string-from-list")

  private val stringToValueMap = values.map(v => v.toString -> v).toMap

  def fromString(s: String): Option[ExtractJsonResponseActionType] = stringToValueMap.get(s)

  // Implicit conversion from AssertResponseActionType to String
  implicit def enumValueToString(value: ExtractJsonResponseActionType): String = value.toString
}



