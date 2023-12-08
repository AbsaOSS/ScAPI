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

package africa.absa.testing.scapi.json.factory

import africa.absa.testing.scapi._
import africa.absa.testing.scapi.json._
import africa.absa.testing.scapi.json.schema.{JsonSchemaValidator, ScAPIJsonSchema}
import africa.absa.testing.scapi.logging.Logger
import africa.absa.testing.scapi.model._
import africa.absa.testing.scapi.model.suite._
import africa.absa.testing.scapi.rest.request.{RequestBody, RequestHeaders, RequestParams}
import africa.absa.testing.scapi.rest.response.Response
import africa.absa.testing.scapi.rest.response.action.types.ResponseActionGroupType
import africa.absa.testing.scapi.utils.file.{FileUtils, JsonUtils}
import spray.json._

import java.net.URL
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
  def fromFiles(environment: Environment,
                testRootPath: Path,
                filter: String,
                format: String): (Option[BeforeAllSet], Set[Suite], Option[AfterAllSet]) = {
    // NOTE: format not used as json is only supported format in time od development

    val beforeAllSet: Option[BeforeAllSet] = {
      val beforeAllFile = testRootPath.resolve("beforeAll.json").toString
      Try(loadJsonBeforeAllSet(beforeAllFile, environment.asMap()))
    }.get

    val suiteLoadingResults: Map[String, Try[Suite]] = {
      val suiteJsonFiles = findSuiteJsonFiles(testRootPath, filter)
      val suiteTries = suiteJsonFiles.map { file =>
        val suiteTry = Try(loadJsonSuiteBundle(file, environment.asMap()))
        (file.replace(".json", ".bundle"), suiteTry)
      }
      suiteTries.toMap
    }

    if (suiteLoadingResults.values.forall(_.isSuccess)) {
      val suiteBundles: Set[Suite] = suiteLoadingResults.values.collect {
        case Success(suiteBundle) => suiteBundle
      }.toSet

      val afterAllSet: Option[AfterAllSet] = {
        val afterAllFile = testRootPath.resolve("afterAll.json").toString
        Try(loadJsonAfterAllSet(afterAllFile, environment.asMap()))
      }.get

      Logger.info("All suites loaded.")
      (beforeAllSet, filterOnlyOrAll(suiteBundles), afterAllSet)

    } else {
      val failedSuites: Map[String, String] = suiteLoadingResults.collect {
        case (key, Failure(exception)) => (key, s"Message: ${exception.getMessage}\nStackTrace:\n${exception.getStackTrace.mkString("\n")}")
      }

      Logger.error("Not all suites loaded. Failed suites:")
      failedSuites.foreach { case (key, value) =>
        Logger.error(s"$key => $value")
      }
      throw ProjectLoadFailedException()
    }
  }

  /**
   * This method uses the 'only' attribute in SuiteBundles to filter test cases.
   * It separates suite bundles into two categories: those with 'only' tests and others.
   * If a suite has more than one 'only' test, an error is logged.
   * If more than one suite has 'only' tests, an error is logged and an empty set is returned.
   * If only one suite has 'only' tests, only that suite is returned.
   * If no suites have 'only' tests, all suites are returned.
   *
   * @param suiteBundles The set of SuiteBundles to be filtered.
   * @return A set of filtered SuiteBundles based on 'only' attribute.
   */
  def filterOnlyOrAll(suiteBundles: Set[Suite]): Set[Suite] = {
    val (suitesWithOnlyTest, others) = suiteBundles.foldLeft((List.empty[Suite], List.empty[Suite])) {
      case ((onlySuites, normalSuites), suiteBundle) =>
        val suite = suiteBundle.suite
        val onlyTests = suite.tests.filter(_.only.getOrElse(false))
        onlyTests.size match {
          case 0 => (onlySuites, suiteBundle :: normalSuites) // No 'only' test
          case 1 => (suiteBundle.copy(suite = suite.copy(tests = onlyTests)) :: onlySuites, normalSuites) // Exactly one 'only' test
          case _ =>
            Logger.error(s"Suite ${suite.name} has more than one test marked as only.")
            (onlySuites, normalSuites) // More than one 'only' test in a suite is an error
        }
    }

    suitesWithOnlyTest.size match {
      case 0 => others.toSet // If no suite with 'only' test(s), return all other suites
      case 1 => suitesWithOnlyTest.toSet // Only one 'only' test across all suites
      case _ => // More than one 'only' test across all suites is an error
        val testNames = suitesWithOnlyTest.flatMap(suiteBundle => suiteBundle.suite.tests.map(test => s"${suiteBundle.suite.name}.${test.name}")).mkString(", ")
        Logger.error(s"Detected more than one test with defined only option. Tests: $testNames")
        Set.empty[Suite]
    }
  }

  /**
   * Method to find all suite JSON files from the given path and filter.
   *
   * @param path   The directory path to search suite JSON files.
   * @param filter The filter string to be used for finding suite JSON files.
   * @return Set of paths to suite JSON files.
   */
  private def findSuiteJsonFiles(path: Path, filter: String): Set[String] = FileUtils.findMatchingFiles(path, filter + "\\.suite\\.json")

  /**
   * Method to load a SuiteBundle instance from the given suite JSON file path.
   *
   * @param suitePath      The path to the suite JSON file.
   * @param environmentMap The map containing environment variables.
   * @return A SuiteBundle instance.
   */
  private def loadJsonSuiteBundle(suitePath: String, environmentMap: Map[String, String]): Suite = {
    val (suiteFilePath, suiteFileName) = FileUtils.splitPathAndFileName(suitePath)
    val suiteName = suiteFileName.stripSuffix(".suite.json")

    val suiteConstants: SuiteConstants = loadJsonSuiteConstants(suiteFilePath, suiteName, environmentMap)
    // TODO - code proposal - will be solved in #4
    // val functions: Map[String, String] = loadJsonSuiteFunctions(suiteFilePath, environmentMap)

    val beforeSuiteActions: Option[BeforeSuiteSet] = loadJsonSuite[BeforeSuiteSet](
      suiteFilePath,
      suiteName,
      environmentMap ++ suiteConstants.constants,
      ScAPIJsonSchema.BEFORE_SUITE,
      "beforeSuite",
      parseToBeforeSuite
    )
    val afterSuiteActions: Option[AfterSuiteSet] = loadJsonSuite[AfterSuiteSet](
      suiteFilePath,
      suiteName,
      environmentMap ++ suiteConstants.constants,
      ScAPIJsonSchema.AFTER_SUITE,
      "afterSuite",
      parseToAfterSuite
    )

    JsonSchemaValidator.validate(suitePath, ScAPIJsonSchema.SUITE)
    val jsonString: String = JsonUtils.stringFromPath(suitePath)
    val notResolvedSuite: TestSet = parseToSuite(jsonString)
    val resolvedSuite: TestSet = notResolvedSuite.resolveReferences(environmentMap ++ suiteConstants.constants)
    Suite(resolvedSuite, beforeSuiteActions, afterSuiteActions)
  }

  private def loadJsonBeforeAllSet(suitePath: String, environmentMap: Map[String, String]): Option[BeforeAllSet] = {
    val (filePath, fileName) = FileUtils.splitPathAndFileName(suitePath)

    val setConstants: SuiteConstants = loadJsonSuiteConstants(filePath, "", environmentMap)

    loadJsonSuite[BeforeAllSet](
      filePath,
      "",
      environmentMap ++ setConstants.constants,
      ScAPIJsonSchema.BEFORE_ALL,
      "beforeAll",
      parseToBeforeAll
    )
  }
  private def loadJsonAfterAllSet(suitePath: String, environmentMap: Map[String, String]): Option[AfterAllSet] = {
    val (filePath, fileName) = FileUtils.splitPathAndFileName(suitePath)

    val setConstants: SuiteConstants = loadJsonSuiteConstants(filePath, "", environmentMap)

    loadJsonSuite[AfterAllSet](
      filePath,
      "",
      environmentMap ++ setConstants.constants,
      ScAPIJsonSchema.AFTER_ALL,
      "afterAll",
      parseToAfterAll
    )
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
    val fileName = if (suiteName.nonEmpty) s"$suiteName.constants.json" else s"constants.json"
    val constantsFilePath: Path = Paths.get(suiteFilePath, fileName)
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
   * Loads a Suite instance from the given constants JSON file path.
   *
   * @param suiteFilePath The path to the constants JSON file.
   * @param suiteName     The name of the suite for which constants are to be loaded.
   * @param properties    The map containing properties variables.
   * @param jsonSchema    The URL to the schema to be used for validation.
   * @param extension     The file extension.
   * @param parser        The parser function used to parse JSON string.
   * @return A Suite instance.
   */
  private def loadJsonSuite[T <: SuitePreAndPostProcessing](suiteFilePath: String,
                                                            suiteName: String,
                                                            properties: Map[String, String],
                                                            jsonSchema: URL,
                                                            extension: String,
                                                            parser: String => T): Option[T] = {
    val fileName = if (suiteName.nonEmpty) s"$suiteName.$extension.json" else s"$extension.json"
    val filePath: Path = Paths.get(suiteFilePath, fileName)
    if (!Files.exists(filePath)) {
      None
    } else {
      JsonSchemaValidator.validate(filePath.toString, jsonSchema)
      val jsonString: String = JsonUtils.stringFromPath(filePath.toString)
      val notResolvedSuite: T = parser(jsonString)
      Some(notResolvedSuite.resolveReferences(properties).asInstanceOf[T])
    }
  }

  /**
   * Method to parse a SuiteConstants instance from the given JSON string.
   *
   * @param jsonString The JSON string to be parsed.
   * @return A SuiteConstants instance.
   */
  private def parseToSuiteConstant(jsonString: String): SuiteConstants = {
    import SuiteConstantJsonProtocol.suiteConstantFormat
    jsonString.parseJson.convertTo[SuiteConstants]
  }

  private def parseToBeforeAll(jsonString: String): BeforeAllSet = {
    import BeforeAllJsonProtocol.beforeAllFormat
    jsonString.parseJson.convertTo[BeforeAllSet]
  }

  /**
   * Method to parse a BeforeSuite instance from the given JSON string.
   *
   * @param jsonString The JSON string to be parsed.
   * @return A BeforeSuite instance.
   */
  private def parseToBeforeSuite(jsonString: String): BeforeSuiteSet = {
    import BeforeSuiteJsonProtocol.beforeSuiteFormat
    jsonString.parseJson.convertTo[BeforeSuiteSet]
  }

  /**
   * Method to parse a AfterSuite instance from the given JSON string.
   *
   * @param jsonString The JSON string to be parsed.
   * @return A AfterSuite instance.
   */
  private def parseToAfterSuite(jsonString: String): AfterSuiteSet = {
    import AfterSuiteJsonProtocol.afterSuiteFormat
    jsonString.parseJson.convertTo[AfterSuiteSet]
  }

  private def parseToAfterAll(jsonString: String): AfterAllSet = {
    import AfterAllJsonProtocol.afterAllFormat
    jsonString.parseJson.convertTo[AfterAllSet]
  }

  /**
   * Method to parse a Suite instance from the given JSON string.
   *
   * @param jsonString The JSON string to be parsed.
   * @return A Suite instance.
   */
  private def parseToSuite(jsonString: String): TestSet = {
    import SuiteJsonProtocol.suiteFormat
    jsonString.parseJson.convertTo[TestSet]
  }

  /**
   * This method validates the content of each SuiteBundle in the set.
   * It checks each suite's headers, body, parameters, and assertions.
   *
   * @param suiteBundles The set of SuiteBundles to be validated.
   */
  def validateSuiteContent(suiteBundles: Set[Suite]): Unit = suiteBundles.foreach(validateSuiteContent)

  /**
   * This method validates the content of a SuiteBundle.
   * It checks the suite's headers, body, parameters, and response action.
   *
   * @param suiteBundle The SuiteBundle to be validated.
   */
  def validateSuiteContent(suiteBundle: Suite): Unit = {
    Logger.debug(s"Validation content of suite: ${suiteBundle.suite.name}")
    suiteBundle.suite.tests.foreach(test => {
      test.headers.foreach(header => RequestHeaders.validateContent(header))
      RequestBody.validateContent(test.action.body)
      RequestParams.validateContent(test.action.params)
      test.responseActions.foreach(responseAction => Response.validate(responseAction))
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
  implicit val responseActionsFormat: RootJsonFormat[ResponseAction] = ResponseActionJsonProtocol.ResponseActionJsonFormat
  implicit val suiteTestFormat: RootJsonFormat[SuiteTestScenario] = jsonFormat6(SuiteTestScenario)
  implicit val methodFormat: RootJsonFormat[Method] = jsonFormat4(Method)
  implicit val suiteFormat: RootJsonFormat[TestSet] = jsonFormat2(TestSet)
}

/**
 * Object that provides implicit JSON format for SuiteConstants class.
 */
object SuiteConstantJsonProtocol extends DefaultJsonProtocol {
  implicit val suiteConstantFormat: RootJsonFormat[SuiteConstants] = jsonFormat1(SuiteConstants)
}

object BeforeAllJsonProtocol extends DefaultJsonProtocol {
  implicit val headerFormat: RootJsonFormat[Header] = jsonFormat2(Header)
  implicit val paramFormat: RootJsonFormat[Param] = jsonFormat2(Param)
  implicit val testActionFormat: RootJsonFormat[Action] = jsonFormat4(Action)
  implicit val responseActionFormat: RootJsonFormat[ResponseAction] = ResponseActionJsonProtocol.ResponseActionJsonFormat
  implicit val methodFormat: RootJsonFormat[Method] = jsonFormat4(Method)
  implicit val beforeAllFormat: RootJsonFormat[BeforeAllSet] = jsonFormat2(BeforeAllSet)
}

/**
 * Object that provides implicit JSON format for BeforeSuite class.
 */
object BeforeSuiteJsonProtocol extends DefaultJsonProtocol {
  implicit val headerFormat: RootJsonFormat[Header] = jsonFormat2(Header)
  implicit val paramFormat: RootJsonFormat[Param] = jsonFormat2(Param)
  implicit val testActionFormat: RootJsonFormat[Action] = jsonFormat4(Action)
  implicit val responseActionFormat: RootJsonFormat[ResponseAction] = ResponseActionJsonProtocol.ResponseActionJsonFormat
  implicit val methodFormat: RootJsonFormat[Method] = jsonFormat4(Method)
  implicit val beforeSuiteFormat: RootJsonFormat[BeforeSuiteSet] = jsonFormat2(BeforeSuiteSet)
}

/**
 * Object that provides implicit JSON format for AfterSuite class.
 */
object AfterSuiteJsonProtocol extends DefaultJsonProtocol {
  implicit val headerFormat: RootJsonFormat[Header] = jsonFormat2(Header)
  implicit val paramFormat: RootJsonFormat[Param] = jsonFormat2(Param)
  implicit val testActionFormat: RootJsonFormat[Action] = jsonFormat4(Action)
  implicit val responseActionFormat: RootJsonFormat[ResponseAction] = ResponseActionJsonProtocol.ResponseActionJsonFormat
  implicit val methodFormat: RootJsonFormat[Method] = jsonFormat4(Method)
  implicit val afterSuiteFormat: RootJsonFormat[AfterSuiteSet] = jsonFormat2(AfterSuiteSet)
}

object AfterAllJsonProtocol extends DefaultJsonProtocol {
  implicit val headerFormat: RootJsonFormat[Header] = jsonFormat2(Header)
  implicit val paramFormat: RootJsonFormat[Param] = jsonFormat2(Param)
  implicit val testActionFormat: RootJsonFormat[Action] = jsonFormat4(Action)
  implicit val responseActionFormat: RootJsonFormat[ResponseAction] = ResponseActionJsonProtocol.ResponseActionJsonFormat
  implicit val methodFormat: RootJsonFormat[Method] = jsonFormat4(Method)
  implicit val afterAllFormat: RootJsonFormat[AfterAllSet] = jsonFormat2(AfterAllSet)
}

/**
 * Object that provides implicit JSON format for ResponseAction class.
 */
object ResponseActionJsonProtocol extends DefaultJsonProtocol {

  implicit object ResponseActionJsonFormat extends RootJsonFormat[ResponseAction] {
    def write(a: ResponseAction): JsObject = {
      val fixedFields = Seq(
        "group" -> JsString(a.group.toString),
        "name" -> JsString(a.name)
      )

      val paramFields = a.params.view.mapValues(JsString(_)).toSeq

      JsObject(fixedFields ++ paramFields: _*)
    }

    def read(value: JsValue): ResponseAction = {
      value.asJsObject.getFields("method") match {
        case Seq(JsString(method)) =>
          val splitter: Seq[String] = method.split("\\.").toSeq
          val group = ResponseActionGroupType.fromString(splitter.head).getOrElse(
            throw new IllegalArgumentException(s"Invalid action group: ${splitter.head}"))
          val name = splitter.tail.head
          val params = value.asJsObject.fields.view.toMap
          ResponseAction(group, name, params.map { case (k, v) => k -> v.convertTo[String] })
        case _ => throw DeserializationException("Assertion expected")
      }
    }
  }
}
