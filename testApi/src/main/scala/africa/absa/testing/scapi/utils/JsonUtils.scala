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

package africa.absa.testing.scapi.utils

import java.net.URL
import scala.io.Source
import scala.util.Using

/**
 * Utility object that provides JSON related operations.
 */
object JsonUtils {

  /**
   * Method to read a file from a given path and returns its contents as a String.
   *
   * @param path The file path as a String.
   * @return The file contents as a String.
   */
  def stringFromPath(path: URL): String = {
    Using.resource(Source.fromInputStream(path.openStream())) { source =>
      source.getLines().mkString
    }
  }

  def stringFromPath(path: String): String = {
    Using.resource(Source.fromFile(path)) { source =>
      source.getLines().mkString
    }
  }
}
