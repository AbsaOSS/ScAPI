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

package africa.absa.testing.scapi

import munit.FunSuite

class ScAPIRunnerJobTest extends FunSuite {

  test("call main without params") {
    interceptMessage[IllegalArgumentException]("Wrong options provided. List can be found above\n") {
      ScAPIRunnerJob.main(Array())
    }
  }

  test("call main with minimum params") {
    val args: Array[String] = Array(
      "--env", getClass.getResource("/test_project/localhost.env.json").getPath,
      "--test-root-path", getClass.getResource("/test_project").getPath)
    ScAPIRunnerJob.main(args)

    // TODO - add check of generated report presence a and values #13
  }

  test("call main with minimum params - validate only".only) {
    val args: Array[String] = Array(
      "--env", getClass.getResource("/test_project/localhost.env.json").getPath,
      "--test-root-path", getClass.getResource("/test_project").getPath,
      "--validate-only"
    )
    ScAPIRunnerJob.main(args)
  }

  test("call main with full params - validate only") {
    val args: Array[String] = Array(
      "--env", getClass.getResource("/mini_env.json").getPath,
      "--test-root-path", getClass.getResource("/test_project").getPath,
      "--filter", "(.*)NoTest",
      "--categories", "NEGATIVE",
      "--thread-count", "3",
      "--file-format", "json",
      "--report", "/path/to/report",
      "--validate-only",
      "--debug"
    )
    ScAPIRunnerJob.main(args)
  }

  test("no suite folder in project") {
    val args: Array[String] = Array(
      "--env", "localhost.env.json",
      "--test-root-path", "/random/path/without/suite")

    interceptMessage[SuiteLoadFailed]("Problems during project loading. Details: 'suites' directory have to exist in project root.") {
      ScAPIRunnerJob.main(args)
    }
  }
}
