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
import africa.absa.testing.scapi.json.Environment
import africa.absa.testing.scapi.json.factory.{EnvironmentFactory, SuiteFactory}
import africa.absa.testing.scapi.logging.Logger
import africa.absa.testing.scapi.model.suite.{Suite, SuiteResult}
import africa.absa.testing.scapi.reporter.StdOutReporter
import africa.absa.testing.scapi.rest.RestClient
import africa.absa.testing.scapi.rest.request.sender.ScAPIRequestSender
import africa.absa.testing.scapi.suite.runner.SuiteRunner
import org.apache.logging.log4j.Level

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
  def run(args: Array[String]): String = {
    val cmd = ScAPIRunnerConfig.getCmdLineArguments(args) match {
      case Success(value) => value
      case Failure(exception) => throw exception
    }

    System.setProperty("log4j.configurationFile", "log4j2.xml")
    cmd match {
      case c if c.trace => Logger.setLevel(Level.TRACE)
      case c if c.debug => Logger.setLevel(Level.DEBUG)
      case _ => Logger.setLevel(Level.INFO)
    }
    cmd.logConfigInfo()

    val suitesPath = Paths.get(cmd.testRootPath, "suites")
    if (!Files.exists(suitesPath)) throw SuiteLoadFailedException("'suites' directory have to exist in project root.")

    // jsons to objects
    val environment: Environment = EnvironmentFactory.fromFile(cmd.envPath)
    val suiteBundles: Set[Suite] = SuiteFactory.fromFiles(environment, suitesPath, cmd.filter, cmd.fileFormat)
    // return as 2. & 3. param from method?
      // ask for them separately?

    SuiteFactory.validateSuiteContent(suiteBundles)
    // 2x validation for Alls

    // run tests and result reporting - use categories for test filtering
    if (cmd.validateOnly) {
      Logger.info("Validate only => end run.")
      ""
    } else {
      Logger.info("Running tests")
      // call BeforeAll
      // deal with possible negative result
      val suiteResults: List[SuiteResult] = SuiteRunner.runSuites(suiteBundles, environment, () => new RestClient(ScAPIRequestSender))
      // call AfterAll
      StdOutReporter.printReport(suiteResults)
    }
  }

  def main(args: Array[String]): Unit = {
    val output = run(args)
    Logger.info(s"ScAPI Runner output:\n$output")
  }
}
