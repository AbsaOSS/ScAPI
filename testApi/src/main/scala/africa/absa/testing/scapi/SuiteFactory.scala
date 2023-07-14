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

import africa.absa.testing.scapi.logging.functions.Scribe
import africa.absa.testing.scapi.utils.{FileUtils, JsonUtils}
import spray.json._

import java.nio.file.{Files, Path, Paths}
import scala.util.{Failure, Success, Try}

/**
 * Object that creates a set of Suite instances from the given test root path.
 */
object SuiteFactory {
  implicit val loggingFunctions: Scribe = Scribe(this.getClass)

  /**
   * Method to create a set of Suite instances from the given test root path.
   *
   * @param environment  The environment properties the Suite instances should use for value references.
   * @param testRootPath The root directory from where suite JSON files should be searched for.
   * @param filter       The filter string to be used for finding suite JSON files.
   * @param format       The format of suite JSON files.
   * @return Set of Suite instances.
   */
  def fromFiles(environment: Environment, testRootPath: String, filter: String, format: String)
               (implicit loggingFunctions: Scribe): Set[Suite] = {
    // NOTE: format not used as json is only supported format in time od development
    val suiteResults: Map[String, Try[Suite]] = {
      val suiteJsonFiles = findSuiteJsonFiles(testRootPath, filter)
      val suiteTries = suiteJsonFiles.map { file =>
        val suiteTry = Try(loadJsonSuite(file, environment.asMap()))
        (file, suiteTry)
      }
      suiteTries.toMap
    }

    if (suiteResults.values.forall(_.isSuccess)) {
      loggingFunctions.info("All suites loaded.")
      suiteResults.values.collect {
        case Success(suite) => suite
      }.toSet
    } else {
      val failedSuites: Map[String, String] = suiteResults.collect {
        case (key, Failure(exception)) => (key, exception.getMessage)
      }

      loggingFunctions.error("Not all suites loaded. Failed suites:")
      failedSuites.foreach { case (key, value) =>
        loggingFunctions.error(s"$key => $value")
      }
      throw ProjectLoadFailed()
    }
  }

  /**
   * Method to find all suite JSON files from the given path and filter.
   *
   * @param path   The directory path to search suite JSON files.
   * @param filter The filter string to be used for finding suite JSON files.
   * @return Set of paths to suite JSON files.
   */
  private def findSuiteJsonFiles(path: String, filter: String): Set[String] = FileUtils.findMatchingFiles(path, filter + "\\.suite\\.json")

  /**
   * Method to load a Suite instance from the given suite JSON file path.
   *
   * @param suitePath      The path to the suite JSON file.
   * @param environmentMap The map containing environment variables.
   * @return A Suite instance.
   */
  def loadJsonSuite(suitePath: String, environmentMap: Map[String, String]): Suite = {
    val (suiteFilePath, suiteFileName) = FileUtils.splitPathAndFileName(suitePath)
    val suiteName = suiteFileName.stripSuffix(".suite.json")

    val suiteConstants: SuiteConstants = loadJsonSuiteConstants(suiteFilePath, suiteName, environmentMap)
    // TODO - code proposal - will be solved in #4
    // val functions: Map[String, String] = loadJsonSuiteFunctions(suiteFilePath, environmentMap)
    // TODO - code proposal - will be solved in #3
    // val beforeActions: Map[String, String] = loadJsonSuiteBeforeActions(suiteFilePath, propertiesSum)
    // val afterActions: Map[String, String] = loadJsonSuiteAfterActions(suiteFilePath, propertiesSum)

    JsonSchemaValidator.validate(suitePath, ScAPIJsonSchema.SUITE)
    val jsonString: String = JsonUtils.stringFromPath(suitePath)
    val notResolvedSuite: Suite = parseToSuite(jsonString)
    notResolvedSuite.resolveReferences(environmentMap ++ suiteConstants.constants)
  }

  /**
   * Method to load a SuiteConstants instance from the given constants JSON file path.
   *
   * @param suiteFilePath The path to the constants JSON file.
   * @param suiteName     The name of the suite for which constants are to be loaded.
   * @param properties    The map containing properties variables.
   * @return A SuiteConstants instance.
   */
  def loadJsonSuiteConstants(suiteFilePath: String, suiteName: String, properties: Map[String, String]): SuiteConstants = {
    val constantsFilePath: Path = Paths.get(suiteFilePath, s"$suiteName.constants.json")
    if (!Files.exists(constantsFilePath)) {
      SuiteConstants(Map.empty[String, String])
    } else {
      JsonSchemaValidator.validate(constantsFilePath.toString, ScAPIJsonSchema.SUITE_CONSTANTS)
      val jsonString: String = JsonUtils.stringFromPath(constantsFilePath.toString)
      val notResolvedConstants: SuiteConstants = parseToSuiteConstant(jsonString)
      notResolvedConstants.resolveReferences(properties)
    }
  }

  /**
   * Method to parse a SuiteConstants instance from the given JSON string.
   *
   * @param jsonString The JSON string to be parsed.
   * @return A SuiteConstants instance.
   */
  def parseToSuiteConstant(jsonString: String): SuiteConstants = {
    import africa.absa.testing.scapi.SuiteConstantJsonProtocol.suiteConstantFormat
    jsonString.parseJson.convertTo[SuiteConstants]
  }

  /**
   * Method to parse a Suite instance from the given JSON string.
   *
   * @param jsonString The JSON string to be parsed.
   * @return A Suite instance.
   */
  def parseToSuite(jsonString: String): Suite = {
    import africa.absa.testing.scapi.SuiteJsonProtocol.suiteFormat
    jsonString.parseJson.convertTo[Suite]
  }
}

/**
 * Object that provides implicit JSON format for various Suite related classes.
 */
object SuiteJsonProtocol extends DefaultJsonProtocol {
  implicit val preparationFormat: RootJsonFormat[Header] = jsonFormat2(Header)
  implicit val testActionFormat: RootJsonFormat[Action] = jsonFormat3(Action)
  implicit val assertionFormat: RootJsonFormat[Assertion] = jsonFormat2(Assertion)
  implicit val suiteTestFormat: RootJsonFormat[SuiteTestScenario] = jsonFormat5(SuiteTestScenario)
  implicit val suiteFormat: RootJsonFormat[Suite] = jsonFormat2(Suite)
}

/**
 * Object that provides implicit JSON format for SuiteConstants class.
 */
object SuiteConstantJsonProtocol extends DefaultJsonProtocol {
  implicit val suiteConstantFormat: RootJsonFormat[SuiteConstants] = jsonFormat1(SuiteConstants)
}
