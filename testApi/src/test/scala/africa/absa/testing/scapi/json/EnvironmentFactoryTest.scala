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

import africa.absa.testing.scapi.json.schema.{JsonSchemaValidator, ScAPIJsonSchema}
import africa.absa.testing.scapi.{JsonInvalidSchemaException, UndefinedConstantsInPropertiesException}
import munit.FunSuite

import java.net.URL

class EnvironmentFactoryTest extends FunSuite {

  val correctJsonString: String =
    """{
      |  "constants": {
      |    "server": "localhost",
      |    "port": "8080",
      |    "notUsed": "never used"
      |  },
      |  "properties": {
      |    "url": "http://{{ server}}:{{ port }}/restcontroller"
      |  }
      |}
      |""".stripMargin
  val constants: Map[String, String] = Map(
    "notUsed" -> "never used",
    "port" -> "8080",
    "server" -> "localhost"
  )
  val propertiesNotResolved: Map[String, String] = Map(
    "url" -> "http://{{ server}}:{{ port }}/restcontroller"
  )
  val propertiesResolved: Map[String, String] = Map(
    "url" -> "http://localhost:8080/restcontroller"
  )

  /*
    fromFile
   */
  test("fromFile - successful env.json load") {
    val expectedEnvironment: Environment = Environment(constants, propertiesResolved)
    val envPath: String = getClass.getResource("/mini_env.json").getPath

    val actualEnvironment: Environment = EnvironmentFactory.fromFile(envPath)

    assert(clue(expectedEnvironment) == clue(actualEnvironment))
  }

  test("fromFile - missing referenced constant") {
    interceptMessage[UndefinedConstantsInPropertiesException]("Undefined constant(s): 'errPort' in ''Environment' action.'.") {
      val envPath: String = getClass.getResource("/missing_constant_env.json").getPath
      EnvironmentFactory.fromFile(envPath)
    }
  }

  /*
    json file validation
   */
  test("env json schema valid") {
    val envSchemaPath: URL = ScAPIJsonSchema.ENVIRONMENT
    val envPath: String = getClass.getResource("/test_project/localhost.env.json").getPath

    JsonSchemaValidator.validate(envPath, envSchemaPath)
  }

  def validateEnvJson(name: String, resourcePath: String): Unit = {
    test(name) {
      intercept[JsonInvalidSchemaException] {
        val envSchemaPath: URL = ScAPIJsonSchema.ENVIRONMENT
        val envPath: String = getClass.getResource(resourcePath).getPath

        JsonSchemaValidator.validate(envPath, envSchemaPath)
      }
    }
  }

  validateEnvJson("env json schema not valid - missing required root key", "/not_valid_env_1.json")
  validateEnvJson("env json schema not valid - exists unexpected root key", "/not_valid_env_2.json")
  validateEnvJson("env json schema not valid - wrong property types", "/not_valid_env_3.json")
}
