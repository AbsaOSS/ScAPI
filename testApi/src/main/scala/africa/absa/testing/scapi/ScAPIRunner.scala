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

import africa.absa.testing.scapi.config.ScAPIRunnerConfig
import africa.absa.testing.scapi.model.{Suite, TestResults}
import africa.absa.testing.scapi.json.{Environment, EnvironmentFactory, SuiteFactory}
import africa.absa.testing.scapi.logging.functions.Scribe
import africa.absa.testing.scapi.reporter.StdOutReporter

import java.nio.file.{Files, Paths}
import scala.util.{Failure, Success}

/**
 * Object `ScAPIRunner` serves as the main entry point for the ScAPI runner.
 */
object ScAPIRunner {

  /**
   * The main method that is being invoked to run the ScAPI runner.
   *
   * @param args Command-line arguments.
   */
  def main(args: Array[String]): Unit = {
    val cmd = ScAPIRunnerConfig.getCmdLineArguments(args) match {
      case Success(value) => value
      case Failure(exception) => throw exception
    }
    val logLevel = if (cmd.debug) Scribe.DEBUG else Scribe.INFO
    implicit val loggingFunctions: Scribe = Scribe(this.getClass, logLevel)
    cmd.logConfigInfo

    if (!Files.exists(Paths.get(cmd.testRootPath, "suites"))) throw SuiteLoadFailed("'suites' directory have to exist in project root.")

    // jsons to objects
    val environment: Environment = EnvironmentFactory.fromFile(cmd.envPath)
    val suites: Set[Suite] = SuiteFactory.fromFiles(environment, cmd.testRootPath, cmd.filter, cmd.fileFormat)(Scribe(SuiteFactory.getClass, logLevel))
    SuiteFactory.validateSuiteContent(suites)

    // run tests and result reporting - use categories for test filtering
    if (cmd.validateOnly) {
      loggingFunctions.info("Validate only => end run.")
    } else {
      loggingFunctions.info("Running tests")
      val testResults: Set[TestResults] = SuiteRunner.runSuites(suites, environment)
      StdOutReporter.printReport(testResults)
    }
  }
}