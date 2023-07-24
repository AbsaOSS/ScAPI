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

import africa.absa.testing.scapi.SuiteTestScenario

/**
 * A suite case class that represents a collection of test scenarios for a specific endpoint.
 *
 * @param endpoint  The endpoint which the suite of tests is targeting.
 * @param tests     A set of `SuiteTestScenario` which define the tests in this suite.
 * @constructor Creates a new instance of a Suite.
 */
case class Suite(endpoint: String, tests: Set[SuiteTestScenario]) {

  /**
   * Method to resolve references in the test scenarios using a provided map of references.
   *
   * @param references A map of string keys and values which will be used to resolve references in the test scenarios.
   * @return Returns a new Suite with resolved references in its test scenarios.
   */
  def resolveReferences(references: Map[String, String]): Suite = {
    Suite(endpoint, tests.map(c => c.resolveReferences(references)))
  }
}
