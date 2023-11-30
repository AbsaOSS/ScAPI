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

import munit.FunSuite
import za.co.absa.commons.io.TempDirectory

import java.io.File
import java.nio.file.{Path, Paths}

class FileUtilsTest extends FunSuite {
  private def prepareTestData: (Path, Set[String]) = {
    val tmpDir: Path = TempDirectory().deleteOnExit().path
    val subDir: File = new File(tmpDir.toString + "/sub/another")
    subDir.mkdirs()

    val files = List(
      new File(Paths.get(tmpDir.toString, "filename.txt").toString),
      new File(Paths.get(tmpDir.toString, "filename.json").toString),
      new File(Paths.get(subDir.toString, "filename.xml").toString)
    )
    files.foreach(file => file.createNewFile())
    (tmpDir, files.map(_.toString).toSet)
  }

  /*
    findMatchingFiles
   */
  test("find matching files") {
    val (tmpDir, expectedFiles): (Path, Set[String]) = prepareTestData

    val matchingFiles = FileUtils.findMatchingFiles(tmpDir)

    assert(clue(expectedFiles) == clue(matchingFiles))
  }

  test("find matching files - custom filter") {
    val (tmpDir, files) = prepareTestData
    val expectedFiles = files.filter(file => file.endsWith(".json"))

    val matchingFiles = FileUtils.findMatchingFiles(tmpDir, "(.*).json")

    assert(clue(expectedFiles) == clue(matchingFiles))
  }

  test("find matching files - no found") {
    val (tmpDir, files) = prepareTestData
    val expectedFiles = Set.empty[String]

    val matchingFiles = FileUtils.findMatchingFiles(tmpDir, "(.*).nonsense")

    assert(clue(expectedFiles) == clue(matchingFiles))
  }

  test("find matching files - subdirectories") {
    val (tmpDir, files) = prepareTestData
    val expectedFiles = files.filter(file => file.endsWith(".xml"))

    val matchingFiles = FileUtils.findMatchingFiles(tmpDir, "(.*).xml")

    assert(clue(expectedFiles) == clue(matchingFiles))
  }

  /*
    splitPathAndFileName
   */
  test("split to path and fileName") {
    val filePath = "/path/to/file.txt"
    val expectedPath = "/path/to"
    val expectedFileName = "file.txt"

    val (actualPath, actualFileName) = FileUtils.splitPathAndFileName(filePath)

    assert(clue(expectedPath) == clue(actualPath))
    assert(clue(expectedFileName) == clue(actualFileName))
  }

  test("split to path and fileName - empty input") {
    val filePath = ""

    val (actualPath, actualFileName) = FileUtils.splitPathAndFileName(filePath)

    assert("".==(clue(actualPath)))
    assert("".==(clue(actualFileName)))
  }

  test("split to path and fileName - fileName only") {
    val filePath = "filename.txt"

    val (path, fileName) = FileUtils.splitPathAndFileName(filePath)

    assert("".==(clue(path)))
    assert("filename.txt".==(clue(fileName)))
  }

  test("split to path and fileName - dir path only") {
    val filePath = "/path/to"
    val filePathEndSlash = "/path/to/"
    val expectedPath = "/path"

    val (actualPath, actualFileName) = FileUtils.splitPathAndFileName(filePath)
    val (actualPathEndSlash, actualFileNameEndSlash) = FileUtils.splitPathAndFileName(filePathEndSlash)

    assert(clue(expectedPath) == clue(actualPath))
    assert(clue(expectedPath) == clue(actualPathEndSlash))
    assert("to".==(clue(actualFileName)))
    assert("to".==(clue(actualFileNameEndSlash)))
  }
}
