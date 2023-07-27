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

package africa.absa.testing.scapi.logging.functions

import africa.absa.testing.scapi.logging.LoggingFunctions
import africa.absa.testing.scapi.logging.functions.Scribe.{DEBUG, ERROR, WARNING}
import scribe.format._
import scribe.{Level, Logger}

case class Scribe(logOrigin: String, logLevel: String = Scribe.INFO, formatter: Option[Formatter] = None) extends LoggingFunctions {

  private val scribeLoglevel: Level = logLevel.toLowerCase() match {
    case DEBUG => Level.Debug
    case ERROR => Level.Error
    case WARNING => Level.Warn
    case _ => Level.Info
  }

  private val defaultFormatter = formatter"$date $level ${string(logOrigin)} - $message$mdc"
  Logger.root.clearHandlers().withHandler(formatter = formatter.getOrElse(defaultFormatter))
    .withMinimumLevel(scribeLoglevel)
    .replace()

  def debug(m: String, t: Throwable = None.orNull): Unit = scribe.debug(m, t)
  def info(m: String, t: Throwable = None.orNull): Unit = scribe.info(m, t)
  def warning(m: String, t: Throwable = None.orNull): Unit = scribe.warn(m, t)
  def error(m: String, t: Throwable = None.orNull): Unit = scribe.error(m, t)
}

object Scribe {
  val INFO: String = "info"
  val DEBUG: String = "debug"
  val ERROR: String = "error"
  val WARNING: String = "warning"

  def apply[T](klass: Class[T]): Scribe = { Scribe(logOrigin = klass.toString) }
  def apply[T](klass: Class[T], formatter: Option[Formatter]): Scribe = { Scribe(logOrigin = klass.toString, formatter = formatter) }
  def apply[T](klass: Class[T], logLevel: String): Scribe = { Scribe(logOrigin = klass.toString, logLevel = logLevel) }
  def apply[T](klass: Class[T], logLevel: String, formatter: Option[Formatter]): Scribe = { Scribe(logOrigin = klass.toString, logLevel = logLevel, formatter = formatter) }
}
