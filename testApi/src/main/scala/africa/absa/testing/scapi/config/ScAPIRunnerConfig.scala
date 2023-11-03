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

import africa.absa.testing.scapi.config.ScAPIRunnerConfig._
import africa.absa.testing.scapi.logging.Logger
import scopt.OptionParser

import scala.util.{Failure, Success, Try}

/**
 * Case class that represents the configuration provided by command line parameters for ScAPIRunner
 *
 * @param envPath       Path to the environment definition. Default is empty string.
 * @param testRootPath  Path to the root directory of test definitions. Default is empty string.
 * @param filter        Filter rule for selecting test definitions files. Default is "(.*)".
 * @param categories    Test categories to include in the test suite. Default is all categories "*".
 * @param threadCount   Maximum number of threads used to run the test suite. Default is '1'.
 * @param fileFormat    Format of definition files. Default is 'json'.
 * @param report        Path to the report output directory. Default is empty string.
 * @param validateOnly  Validate input definitions only. Default is 'false'.
 *
 * @constructor Create a new configuration with a env, testRootPath, filter, categories, threadCount, fileFormat, report, validateOnly.
 */
case class ScAPIRunnerConfig(envPath: String = "",
                             testRootPath: String = "",
                             filter: String = DefaultFilter,
                             categories: Set[String] = DefaultCategories,
                             threadCount: Int = DefaultThreadCount,
                             fileFormat: String = DefaultFileFormat,
                             report: String = DefaultReport,
                             debug: Boolean = false,
                             validateOnly: Boolean = false) {
  /**
   * Method to log configuration information
   */
  def logConfigInfo(): Unit = {
  Logger.info(
    s"""
       |ScAPIRunner started with provided configuration:
       |--env: $envPath
       |--testRootDir: $testRootPath
       |--filter: $filter
       |--categories: $categories
       |--report: $report
       |--fileFormat: $fileFormat
       |--threadCount: $threadCount
       |--debug: $debug
       |--validate only: $validateOnly
  """.stripMargin)
  }
}

/**
 * Object that holds default values for ScAPIRunnerConfig and provides methods to parse command line arguments.
 */
object ScAPIRunnerConfig {

  /**
   * Default values for various configuration parameters
   */
  val DefaultFilter: String = "(.*)"
  val DefaultCategories: Set[String] = Set("*")
  val DefaultThreadCount: Int = 1
  val DefaultFileFormat: String = "json"
  val DefaultReport: String = ""

  /**
   * Method to parse and validate command line arguments and return a ScAPIRunnerConfig instance
   *
   * @param args Array of command line arguments to be parsed
   * @return A Try object containing a ScAPIRunnerConfig instance holding the parsed parameters, or an error if parsing fails
   */
  def getCmdLineArguments(args: Array[String]): Try[ScAPIRunnerConfig] = {
    val parser = new CmdParser("ScAPI.jar [lib options]")

    parser.parse(args, ScAPIRunnerConfig()) match {
      case Some(config) => Success(config)
      case _            => Failure(new IllegalArgumentException("Wrong options provided. List can be found above\n"))
    }
  }

  /**
   * Private class to parse command line options
   *
   * @param programName The name of the program
   */
  private class CmdParser(programName: String) extends OptionParser[ScAPIRunnerConfig](programName) {
    head("\nScAPI Test Runner")

    opt[String]("env")
      .required()
      .action((value, config) => { config.copy(envPath = value) })
      .text("Path to a file with an environment definition.")

    opt[String]("test-root-path")
      .required()
      .action((value, config) => { config.copy(testRootPath = value) })
      .text("Path to a root directory of test definitions.")

    opt[String]("filter")
      .optional()
      .action((value, config) => { config.copy(filter = value) })
      .text(s"Filter rule to select test definitions file (recursive) to include into test suite. Default is all '$DefaultFilter'")

    opt[Seq[String]]("categories")
      .optional()
      .valueName("<v1>,<v2>")
      .action((value, config) => { config.copy(categories = value.toSet) })
      .text(s"Select which test categories will be included into test suite. Default is all '$DefaultCategories'")

    opt[Int]("thread-count")
      .optional()
      .action((value, config) => { config.copy(threadCount = value) })
      .text(s"Maximum count of thread used to run test suite. Default is '$DefaultThreadCount'")

    opt[String]("file-format")
      .optional()
      .action((value, config) => { config.copy(fileFormat = value) })
      .text(s"Format of definition files. Default is all '$DefaultFileFormat'")

    opt[String]("report")
      .optional()
      .action((value, config) => { config.copy(report = value) })
      .text("Path to a report output directory.")

    opt[Unit]("debug")
      .optional()
      .action((_, config) => { config.copy(debug = true) })
      .text("Activate debug regime.")

    opt[Unit]("validate-only")
      .optional()
      .action((_, config) => { config.copy(validateOnly = true) })
      .text("Activate validation of input definitions only.")

    help("help").text("prints this usage text")
  }
}


