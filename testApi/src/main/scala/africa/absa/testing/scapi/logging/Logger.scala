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

import org.apache.logging.log4j.{Level, LogManager}

object Logger {
  private var level: Level = Level.INFO

  def setLevel(newLevel: Level): Unit = {
    level = newLevel
  }

  private def log(messageLevel: Level, message: String): Unit = {
    if (messageLevel.isMoreSpecificThan(level)) {
      val callerClassName = new Exception().getStackTrace.drop(1)
        .find(_.getClassName != getClass.getName)
        .map(_.getClassName)
        .getOrElse("unknown")
      val logger = LogManager.getLogger(callerClassName)

      if (logger.isEnabled(messageLevel)) logger.log(messageLevel, message)
    }
  }

  def debug(message: String): Unit = {
    log(Level.DEBUG, message)
  }

  def info(message: String): Unit = {
    log(Level.INFO, message)
  }

  def warn(message: String): Unit = {
    log(Level.WARN, message)
  }

  def error(message: String): Unit = {
    log(Level.ERROR, message)
  }
}
