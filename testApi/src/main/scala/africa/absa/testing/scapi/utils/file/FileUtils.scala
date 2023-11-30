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

package africa.absa.testing.scapi.utils.file

import java.io.File
import java.nio.file.{Path, Paths}

/**
 * Utility object that provides file-related operations.
 */
object FileUtils {

  /**
   * Method to find files in a given path that match a certain pattern.
   *
   * @param path    A path where to search for files. The path can be absolute or relative.
   * @param pattern A regex pattern to match file names. The default value is "(.*)" which matches all files.
   * @return Set<String> A set of file paths as strings that match the provided pattern.
   */
  def findMatchingFiles(path: Path, pattern: String = "(.*)"): Set[String] = {
    def findFilesRecursive(directory: File): Set[File] = {
      val files = directory.listFiles.toSet
      val matchingFiles = files.filter(_.isFile).filter(_.getName.matches(pattern))
      val subDirectories = files.filter(_.isDirectory)

      matchingFiles ++ subDirectories.flatMap(findFilesRecursive)
    }

    val matchingFiles = findFilesRecursive(path.toFile)
    matchingFiles.map(_.getPath)
  }

  /**
   * Method to split a file path into directory path and file name.
   *
   * @param inputPath A string representing the input path which includes directory path and file name.
   * @return Tuple<String, String> A tuple where the first element is the directory path and the second element is the file name.
   */
  def splitPathAndFileName(inputPath: String): (String, String) = {
      val path: Path = Paths.get(inputPath)
      val parent: Option[Path] = Option(path.getParent)
      val directory: String = parent.fold("")(_.toString)
      val filename: String = path.getFileName.toString
      (directory, filename)
  }
}
