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

package africa.absa.testing.scapi.config

import munit.FunSuite

import scala.util.{Failure, Success}

class ScAPIRunnerConfigTest extends FunSuite {
  private val randomPath = "/alfa/beta"
  private val randomEnvJsonPath = "/alfa/gamma/env.json"

  /*
    getCmdLineArguments
   */

  test("getCmdLineArguments - positive minimal") {
    val cmd = ScAPIRunnerConfig.getCmdLineArguments(Array(
      "--env", randomEnvJsonPath,
      "--test-root-path", randomPath
    )) match {
      case Success(value) => value
      case Failure(exception) => fail("Failed to get cmd line arguments.", exception)
    }

    assert(clue(randomEnvJsonPath) == clue(cmd.envPath))
    assert(clue(randomPath) == clue(cmd.testRootPath))
    assert(clue(cmd.filter) == clue(ScAPIRunnerConfig.DefaultFilter))
    assert(clue(cmd.categories) == clue(ScAPIRunnerConfig.DefaultCategories))
    assert(clue(cmd.threadCount) == clue(ScAPIRunnerConfig.DefaultThreadCount))
    assert(clue(cmd.fileFormat) == clue(ScAPIRunnerConfig.DefaultFileFormat))
    assert(clue(cmd.report) == clue(ScAPIRunnerConfig.DefaultReport))
    assert(!cmd.validateOnly)
  }

  test("getCmdLineArguments - missing test definition") {
    val actualException = ScAPIRunnerConfig.getCmdLineArguments(Array()) match {
      case Success(_) => fail("Command line parsing passed but shouldn't")
      case Failure(exception) => exception
    }

    assert(clue("Wrong options provided. List can be found above\n")  == clue(actualException.getMessage))
  }

  test("getCmdLineArguments - positive full") {
    val cmd = ScAPIRunnerConfig.getCmdLineArguments(Array(
      "--env", randomEnvJsonPath,
      "--test-root-path", randomPath,
      "--filter", "(.*)Test(.*)",
      "--categories", "SMOKE,ANOTHER",
      "--thread-count", "4",
      "--file-format", "txt",
      "--validate-only",
      "--report", "/some/report/path",
      "--debug"
    )) match {
      case Success(value) => value
      case Failure(exception) => fail("Command line parsing passed but shouldn't", exception)
    }

    assert(clue(randomEnvJsonPath) == clue(cmd.envPath))
    assert(clue(randomPath) == clue(cmd.testRootPath))
    assert(clue("(.*)Test(.*)") == clue(cmd.filter))
    assert(clue(Set("SMOKE", "ANOTHER")) == clue(cmd.categories))
    assert(clue(4) == clue(cmd.threadCount))
    assert(clue("txt") == clue(cmd.fileFormat))
    assert(clue("/some/report/path") == clue(cmd.report))
    assert(cmd.validateOnly)
  }
}
