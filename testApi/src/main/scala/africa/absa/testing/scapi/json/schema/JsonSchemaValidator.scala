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

package africa.absa.testing.scapi.json.schema

import africa.absa.testing.scapi.JsonInvalidSchemaException
import africa.absa.testing.scapi.utils.file.JsonUtils
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.networknt.schema.{JsonSchema, JsonSchemaFactory, SpecVersion}

import java.net.URL

/**
 * Class that represents a JSON Schema Validator. This validator takes a JSON schema file
 * and a JSON file and provides a functionality to validate the JSON file against the schema.
 *
 * @param jsonPath Path to the JSON file to be validated.
 * @param schemaPath Path to the JSON Schema file.
 */
case class JsonSchemaValidator(jsonPath: String, schemaPath: URL) {
  protected val mapper: ObjectMapper = new ObjectMapper()

  /**
   * Method to read JSON data from the specified file path.
   *
   * @return JsonNode object representing the JSON data.
   */
  def jsonNode: JsonNode = mapper.readTree(JsonUtils.stringFromPath(jsonPath))

  /**
   * Method to create a JsonSchemaFactory instance.
   *
   * @return JsonSchemaFactory instance.
   */
  def jsonSchemaFactory: JsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)

  /**
   * Method to generate a JsonSchema from the provided schema file path.
   *
   * @return JsonSchema object representing the JSON schema.
   */
  def jsonSchema: JsonSchema = jsonSchemaFactory.getSchema(JsonUtils.stringFromPath(schemaPath))

  /**
   * Method to validate the JSON data against the provided schema.
   */
  def validate(): Unit = {
    val errors = jsonSchema.validate(jsonNode)

    import scala.jdk.CollectionConverters._
    if (!errors.isEmpty) throw JsonInvalidSchemaException(jsonPath, errors.asScala)
  }
}

/**
 * Object that represents the JSON Schema Validator.
 */
object JsonSchemaValidator {

  /**
   * Method to validate a JSON file against a schema file.
   *
   * @param jsonPath   The path to the JSON file to be validated.
   * @param schemaPath The path to the JSON schema file.
   */
  def validate(jsonPath: String, schemaPath: URL): Unit = {
    new JsonSchemaValidator(jsonPath, schemaPath).validate()
  }
}
