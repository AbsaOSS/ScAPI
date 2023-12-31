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

object LogResponseActionType extends Enumeration {
  type LogResponseActionType = Value

  val Error: LogResponseActionType.Value = Value("error")
  val Warn: LogResponseActionType.Value = Value("warn")
  val Info: LogResponseActionType.Value = Value("info")
  val Debug: LogResponseActionType.Value = Value("debug")

  val LogInfoResponse: LogResponseActionType.Value = Value("log-info-response")

  def fromString(s: String): Option[LogResponseActionType] = Try(this.withName(s)).toOption
}
