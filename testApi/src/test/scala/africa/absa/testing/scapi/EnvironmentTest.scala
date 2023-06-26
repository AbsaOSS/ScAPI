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

import munit.FunSuite

class EnvironmentTest extends FunSuite {

  val constants: Map[String, String] = Map(
    "notUsed" -> "never used",
    "port" -> "8080",
    "server" -> "localhost"
  )
  val constantsPrefixed: Map[String, String] = Map(
    "env.notUsed" -> "never used",
    "env.port" -> "8080",
    "env.server" -> "localhost"
  )
  val propertiesResolved: Map[String, String] = Map(
    "url" -> "http://localhost:8080/restcontroller"
  )
  val propertiesNotResolved: Map[String, String] = Map(
    "url" -> "http://{{ server}}:{{ port }}/restcontroller"
  )
  val propertiesResolvedPrefixed: Map[String, String] = Map(
    "env.url" -> "http://localhost:8080/restcontroller"
  )

  /*
    apply
   */
  test("apply - access to properties") {
    val env: Environment = Environment(constants, propertiesResolved)
    val expected_value_from_properties: String = "http://localhost:8080/restcontroller"
    val expected_value_from_constants: String = "localhost"

    val actual_value_from_properties: String = env("url")
    val actual_value_from_constants: String = env("server")

    assertEquals(clue(expected_value_from_properties), clue(actual_value_from_properties))
    assertEquals(clue(expected_value_from_constants), clue(actual_value_from_constants))
  }

  test("apply - properties does not exist") {
    val env: Environment = Environment(constants, propertiesResolved)
    interceptMessage[PropertyNotFound]("Property not found: 'no_exist'.") {
      env("no_exist")
    }
  }

  test("apply - same key in properties") {
    val propertyPort = "8090"
    val env: Environment = Environment(constants, propertiesResolved ++ Map("port" -> propertyPort))

    val expected: String = propertyPort
    val actual: String = env("port")

    assertEquals(clue(expected), clue(actual))
  }

  /*
    asMap
   */
  test("asMap - successful env.json load - as map") {
    val expected: Map[String, String] = propertiesResolvedPrefixed ++ constantsPrefixed
    val envPath: String = getClass.getResource("/mini_env.json").getPath

    val actual: Map[String, String] = EnvironmentFactory.fromFile(envPath).asMap()

    assert(clue(expected) == clue(actual))
  }

  /*
    resolveReferences
   */
  test("resolveReferences") {
    val expected: Environment = Environment(constants, propertiesResolved)
    val notResolverEnv: Environment = Environment(constants, propertiesNotResolved)

    val actual: Environment = notResolverEnv.resolveReferences

    assertEquals(clue(expected), clue(actual))
  }
}
