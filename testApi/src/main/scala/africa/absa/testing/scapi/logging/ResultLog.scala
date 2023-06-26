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

package africa.absa.testing.scapi.logging

sealed abstract class ResultLog(message: String, throwable: Throwable = None.orNull) {
  def log(logFunction: (String, Throwable) => Unit): Unit = { logFunction(message, throwable) }

  def log(implicit loggingFunctions: LoggingFunctions): Unit
}

object ResultLog {
  def apply(logLevel: LogLevel, message: String, throwable: Throwable = None.orNull): ResultLog = {
    logLevel match {
      case DebugLogLevel => DebugResultLog(message, throwable)
      case InfoLogLevel => InfoResultLog(message, throwable)
      case WarningLogLevel => WarningResultLog(message, throwable)
      case ErrorLogLevel => ErrorResultLog(message, throwable)
    }
  }
}

case class DebugResultLog(message: String, throwable: Throwable = None.orNull)
  extends ResultLog(message, throwable) {
  def log(implicit loggingFunctions: LoggingFunctions): Unit = loggingFunctions.debug(message, throwable)
}

case class InfoResultLog(message: String, throwable: Throwable = None.orNull)
  extends ResultLog(message, throwable){
  def log(implicit loggingFunctions: LoggingFunctions): Unit = loggingFunctions.info(message, throwable)
}

case class WarningResultLog(message: String, throwable: Throwable = None.orNull)
  extends ResultLog(message, throwable){
  def log(implicit loggingFunctions: LoggingFunctions): Unit = loggingFunctions.warning(message, throwable)
}

case class ErrorResultLog(message: String, throwable: Throwable = None.orNull)
  extends ResultLog(message, throwable){
  def log(implicit loggingFunctions: LoggingFunctions): Unit = loggingFunctions.error(message, throwable)
}
