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

package africa.absa.testing.scapi.json

import africa.absa.testing.scapi._
import africa.absa.testing.scapi.data.SuiteBundle
import africa.absa.testing.scapi.json.schema.{JsonSchemaValidator, ScAPIJsonSchema}
import africa.absa.testing.scapi.logging.functions.Scribe
import africa.absa.testing.scapi.rest.request.{RequestBody, RequestHeaders, RequestParams}
import africa.absa.testing.scapi.rest.response.Response
import africa.absa.testing.scapi.utils.{FileUtils, JsonUtils}
import spray.json._

import java.nio.file.{Files, Path, Paths}
import scala.util.{Failure, Success, Try}

/**
 * Object that creates a set of Suite instances from the given test root path.
 */
object SuiteFactory {

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
               (implicit loggingFunctions: Scribe): Set[SuiteBundle] = {
    // NOTE: format not used as json is only supported format in time od development

    val suiteLoadingResults: Map[String, Try[SuiteBundle]] = {
      val suiteJsonFiles = findSuiteJsonFiles(testRootPath, filter)
      val suiteTries = suiteJsonFiles.map { file =>
        val suiteTry = Try(loadJsonSuiteBundle(file, environment.asMap()))
        (file.replace(".json", ".bundle"), suiteTry)
      }
      suiteTries.toMap
    }

    if (suiteLoadingResults.values.forall(_.isSuccess)) {
      loggingFunctions.info("All suites loaded.")
      val suiteBundles: Set[SuiteBundle] = suiteLoadingResults.values.collect {
        case Success(suiteBundle) => suiteBundle
      }.toSet

      filterOnlyOrAll(suiteBundles)

    } else {
      val failedSuites: Map[String, String] = suiteLoadingResults.collect {
        case (key, Failure(exception)) => (key, s"Message: ${exception.getMessage}\nStackTrace:\n${exception.getStackTrace.mkString("\n")}")
      }

      loggingFunctions.error("Not all suites loaded. Failed suites:")
      failedSuites.foreach { case (key, value) =>
        loggingFunctions.error(s"$key => $value")
      }
      throw ProjectLoadFailed()
    }
  }

  def filterOnlyOrAll(suiteBundles: Set[SuiteBundle])
                     (implicit loggingFunctions: Scribe): Set[SuiteBundle] = {
    val suiteBundlesWithOnlyTest: Set[SuiteBundle] = suiteBundles.filter(bundle => bundle.suite.tests.exists(_.only.getOrElse(false)))

    val updatedSuiteBundles = if (suiteBundlesWithOnlyTest.isEmpty) {
      suiteBundles
    } else {
      val filteredSuiteBundles: Set[SuiteBundle] = suiteBundlesWithOnlyTest.map { bundle =>
        bundle.suite.copy(tests = bundle.suite.tests.filter(_.only.getOrElse(false)))
        bundle
      }

      val numberOfOnlyTests = filteredSuiteBundles.foldLeft(0)((acc, bundle) => acc + bundle.suite.tests.size)

      if (numberOfOnlyTests == 1) {
        filteredSuiteBundles
      } else {
        val filteredTestNames = filteredSuiteBundles.flatMap(bundle => bundle.suite.tests.map(test => s"${bundle.suite.endpoint}.${test.name}")).toList
        loggingFunctions.error(s"Detected more than one test with defined only option. Tests: ${filteredTestNames.mkString(",")}")
        Set.empty[SuiteBundle]
      }
    }

    updatedSuiteBundles
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
   * Method to load a SuiteBundle instance from the given suite JSON file path.
   *
   * @param suitePath      The path to the suite JSON file.
   * @param environmentMap The map containing environment variables.
   * @return A SuiteBundle instance.
   */
  def loadJsonSuiteBundle(suitePath: String, environmentMap: Map[String, String])
                         (implicit loggingFunctions: Scribe): SuiteBundle = {
    val (suiteFilePath, suiteFileName) = FileUtils.splitPathAndFileName(suitePath)
    val suiteName = suiteFileName.stripSuffix(".suite.json")

    val suiteConstants: SuiteConstants = loadJsonSuiteConstants(suiteFilePath, suiteName, environmentMap)
    // TODO - code proposal - will be solved in #4
    // val functions: Map[String, String] = loadJsonSuiteFunctions(suiteFilePath, environmentMap)

    val beforeActions: Option[SuiteBefore] = loadJsonSuiteBefore(suiteFilePath, suiteName, environmentMap ++ suiteConstants.constants)
    val afterActions: Option[SuiteAfter] = loadJsonSuiteAfter(suiteFilePath, suiteName, environmentMap ++ suiteConstants.constants)

    JsonSchemaValidator.validate(suitePath, ScAPIJsonSchema.SUITE)
    val jsonString: String = JsonUtils.stringFromPath(suitePath)
    val notResolvedSuite: Suite = parseToSuite(jsonString)
    val resolvedSuite: Suite = notResolvedSuite.resolveReferences(environmentMap ++ suiteConstants.constants)
    SuiteBundle(resolvedSuite, beforeActions, afterActions)
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
   * Method to load a Suite Before instance from the given constants JSON file path.
   *
   * @param suiteFilePath The path to the constants JSON file.
   * @param suiteName     The name of the suite for which constants are to be loaded.
   * @param properties    The map containing properties variables.
   * @return A SuiteBefore instance.
   */
  def loadJsonSuiteBefore(suiteFilePath: String, suiteName: String, properties: Map[String, String]): Option[SuiteBefore] = {
    val beforeFilePath: Path = Paths.get(suiteFilePath, s"$suiteName.before.json")
    if (!Files.exists(beforeFilePath)) {
      None
    } else {
      JsonSchemaValidator.validate(beforeFilePath.toString, ScAPIJsonSchema.SUITE_BEFORE)
      val jsonString: String = JsonUtils.stringFromPath(beforeFilePath.toString)
      val notResolvedBefore: SuiteBefore = parseToSuiteBefore(jsonString)
      Some(notResolvedBefore.resolveReferences(properties))
    }
  }

  /**
   * Method to load a Suite After instance from the given constants JSON file path.
   *
   * @param suiteFilePath The path to the constants JSON file.
   * @param suiteName     The name of the suite for which constants are to be loaded.
   * @param properties    The map containing properties variables.
   * @return A SuiteAfter instance.
   */
  def loadJsonSuiteAfter(suiteFilePath: String, suiteName: String, properties: Map[String, String]): Option[SuiteAfter] = {
    val afterFilePath: Path = Paths.get(suiteFilePath, s"$suiteName.after.json")
    if (!Files.exists(afterFilePath)) {
      None
    } else {
      JsonSchemaValidator.validate(afterFilePath.toString, ScAPIJsonSchema.SUITE_AFTER)
      val jsonString: String = JsonUtils.stringFromPath(afterFilePath.toString)
      val notResolvedAfter: SuiteAfter = parseToSuiteAfter(jsonString)
      Some(notResolvedAfter.resolveReferences(properties))
    }
  }

  /**
   * Method to parse a SuiteConstants instance from the given JSON string.
   *
   * @param jsonString The JSON string to be parsed.
   * @return A SuiteConstants instance.
   */
  def parseToSuiteConstant(jsonString: String): SuiteConstants = {
    import SuiteConstantJsonProtocol.suiteConstantFormat
    jsonString.parseJson.convertTo[SuiteConstants]
  }

  /**
   * Method to parse a SuiteBefore instance from the given JSON string.
   *
   * @param jsonString The JSON string to be parsed.
   * @return A SuiteBefore instance.
   */
  def parseToSuiteBefore(jsonString: String): SuiteBefore = {
    import SuiteBeforeJsonProtocol.suiteBeforeFormat
    jsonString.parseJson.convertTo[SuiteBefore]
  }

  /**
   * Method to parse a SuiteAfter instance from the given JSON string.
   *
   * @param jsonString The JSON string to be parsed.
   * @return A SuiteAfter instance.
   */
  def parseToSuiteAfter(jsonString: String): SuiteAfter = {
    import SuiteAfterJsonProtocol.suiteAfterFormat
    jsonString.parseJson.convertTo[SuiteAfter]
  }

  /**
   * Method to parse a Suite instance from the given JSON string.
   *
   * @param jsonString The JSON string to be parsed.
   * @return A Suite instance.
   */
  def parseToSuite(jsonString: String): Suite = {
    import SuiteJsonProtocol.suiteFormat
    jsonString.parseJson.convertTo[Suite]
  }

  def validateSuiteContent(suiteBundles: Set[SuiteBundle])
                          (implicit loggingFunctions: Scribe): Unit = suiteBundles.foreach(validateSuiteContent)

  def validateSuiteContent(suiteBundle: SuiteBundle)
                          (implicit loggingFunctions: Scribe): Unit = {
    loggingFunctions.debug(s"Validation content of suite: ${suiteBundle.suite.endpoint}")
    suiteBundle.suite.tests.foreach(test => {
      test.headers.foreach(header => RequestHeaders.validateContent(header))
      RequestBody.validateContent(test.actions.head.body)
      RequestParams.validateContent(test.actions.head.params)
      test.assertions.foreach(assertion => Response.validate(assertion))
    })
  }
}

/**
 * Object that provides implicit JSON format for various Suite related classes.
 */
object SuiteJsonProtocol extends DefaultJsonProtocol {
  implicit val headerFormat: RootJsonFormat[Header] = jsonFormat2(Header)
  implicit val paramFormat: RootJsonFormat[Param] = jsonFormat2(Param)
  implicit val testActionFormat: RootJsonFormat[Action] = jsonFormat4(Action)
  implicit val assertionFormat: RootJsonFormat[Assertion] = jsonFormat7(Assertion)
  implicit val suiteTestFormat: RootJsonFormat[SuiteTestScenario] = jsonFormat6(SuiteTestScenario)
  implicit val methodFormat: RootJsonFormat[Method] = jsonFormat4(Method)
  implicit val suiteFormat: RootJsonFormat[Suite] = jsonFormat2(Suite)
}

/**
 * Object that provides implicit JSON format for SuiteConstants class.
 */
object SuiteConstantJsonProtocol extends DefaultJsonProtocol {
  implicit val suiteConstantFormat: RootJsonFormat[SuiteConstants] = jsonFormat1(SuiteConstants)
}

/**
 * Object that provides implicit JSON format for SuiteBefore class.
 */
object SuiteBeforeJsonProtocol extends DefaultJsonProtocol {
  implicit val headerFormat: RootJsonFormat[Header] = jsonFormat2(Header)
  implicit val paramFormat: RootJsonFormat[Param] = jsonFormat2(Param)
  implicit val testActionFormat: RootJsonFormat[Action] = jsonFormat4(Action)
  implicit val assertionFormat: RootJsonFormat[Assertion] = jsonFormat7(Assertion)
  implicit val methodFormat: RootJsonFormat[Method] = jsonFormat4(Method)
  implicit val suiteBeforeFormat: RootJsonFormat[SuiteBefore] = jsonFormat2(SuiteBefore)
}

/**
 * Object that provides implicit JSON format for SuiteAfter class.
 */
object SuiteAfterJsonProtocol extends DefaultJsonProtocol {
  implicit val headerFormat: RootJsonFormat[Header] = jsonFormat2(Header)
  implicit val paramFormat: RootJsonFormat[Param] = jsonFormat2(Param)
  implicit val testActionFormat: RootJsonFormat[Action] = jsonFormat4(Action)
  implicit val assertionFormat: RootJsonFormat[Assertion] = jsonFormat7(Assertion)
  implicit val methodFormat: RootJsonFormat[Method] = jsonFormat4(Method)
  implicit val suiteAfterFormat: RootJsonFormat[SuiteAfter] = jsonFormat2(SuiteAfter)
}
