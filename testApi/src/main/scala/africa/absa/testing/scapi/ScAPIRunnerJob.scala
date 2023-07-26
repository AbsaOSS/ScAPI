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
import africa.absa.testing.scapi.data.{SuiteBundle, SuiteResults}
import africa.absa.testing.scapi.json.{Environment, EnvironmentFactory, SuiteFactory, SuiteRunnerJob}
import africa.absa.testing.scapi.logging.LoggerConfig
import africa.absa.testing.scapi.logging.functions.Scribe
import africa.absa.testing.scapi.reporter.TxtReporter
import africa.absa.testing.scapi.utils.cache.RuntimeCache

import java.nio.file.{Files, Paths}
import scala.util.{Failure, Success}

/**
 * Object `ScAPIRunnerJob` serves as the main entry point for the ScAPI runner.
 */
object ScAPIRunnerJob {
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
    LoggerConfig.logLevel = if (cmd.debug) Scribe.DEBUG else Scribe.INFO
    implicit val loggingFunctions: Scribe = Scribe(this.getClass, LoggerConfig.logLevel)
    cmd.logConfigInfo

    RuntimeCache.initLogging(loggingFunctions)

    if (!Files.exists(Paths.get(cmd.testRootPath, "suites"))) throw SuiteLoadFailed("'suites' directory have to exist in project root.")

    // jsons to objects
    val environment: Environment = EnvironmentFactory.fromFile(cmd.envPath)
    val suiteBundles: Set[SuiteBundle] = SuiteFactory.fromFiles(environment, cmd.testRootPath, cmd.filter, cmd.fileFormat)(Scribe(SuiteFactory.getClass, LoggerConfig.logLevel))
    SuiteFactory.validateSuiteContent(suiteBundles)

    // run tests and result reporting - use categories for test filtering
    if (cmd.validateOnly) {
      loggingFunctions.info("Validate only => end run.")
    } else {
      loggingFunctions.info("Running tests")
      val testResults: Set[SuiteResults] = SuiteRunnerJob.runSuites(suiteBundles, environment)
      TxtReporter.printReport(testResults)
    }
  }
}
