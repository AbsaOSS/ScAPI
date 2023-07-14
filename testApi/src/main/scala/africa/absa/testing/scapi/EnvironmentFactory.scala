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

import africa.absa.testing.scapi.utils.JsonUtils
import spray.json._

import java.net.URL

/**
 * EnvironmentFactory object is used to create Environment objects.
 */
object EnvironmentFactory {

  /**
   * Method to create an environment from a JSON file.
   *
   * @param path This is the path to the JSON file.
   * @return Returns an environment object constructed from the JSON file.
   */
  def fromFile(path: String): Environment = {
    JsonSchemaValidator.validate(path, ScAPIJsonSchema.ENVIRONMENT)
    val jsonString: String = readJsonFile(path)
    val environment: Environment = parseToEnvironment(jsonString)
    environment.resolveReferences
  }

  /**
   * Method to read a JSON file from a given path.
   *
   * @param path This is the path to the JSON file.
   * @return Returns the JSON file content as a String.
   */
  private def readJsonFile(path: URL): String = JsonUtils.stringFromPath(path)
  private def readJsonFile(path: String): String = JsonUtils.stringFromPath(path)

  /**
   * Method to parse a JSON string into an Environment object.
   *
   * @param jsonString This is the JSON content in String format.
   * @return Returns an environment object constructed from the JSON string.
   */
  private def parseToEnvironment(jsonString: String): Environment = {
    import africa.absa.testing.scapi.EnvironmentJsonProtocol.environmentFormat
    jsonString.parseJson.convertTo[Environment]
  }
}

/**
 * EnvironmentJsonProtocol object is used to define JSON serialization and deserialization format for Environment objects.
 */
object EnvironmentJsonProtocol extends DefaultJsonProtocol {

  /**
   * This implicit value defines the JSON format for Environment object.
   *
   * @return Returns the JSON format for Environment object.
   */
  implicit val environmentFormat: RootJsonFormat[Environment] = jsonFormat2(Environment)
}
