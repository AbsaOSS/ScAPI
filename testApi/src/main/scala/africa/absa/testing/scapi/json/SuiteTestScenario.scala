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

/**
 * Case class that represents a suite test scenario.
 *
 * @param name The name of the test scenario.
 * @param categories The required test categories of the test scenario.
 * @param headers The set of header options for the test scenario.
 * @param actions The set of action objects for the test scenario.
 * @param assertions The set of assertion objects for the test scenario.
 * @param only The control if test should be only be running when set to true.
 */
case class SuiteTestScenario(name: String,
                             categories: Set[String],
                             headers: Set[Header],
                             actions: Set[Action],
                             assertions: Set[Assertion],
                             only: Option[Boolean] = Some(false)) extends Requestable {
  /**
   * Method to resolve references within the SuiteTestScenario instance.
   *
   * @param references A map containing the references to be resolved.
   * @return A new SuiteTestScenario instance where all references are resolved.
   */
  def resolveReferences(references: Map[String, String]): SuiteTestScenario = {
    SuiteTestScenario(
      name,
      categories,
      headers.map(c => c.resolveReferences(references)),
      actions.map(c => c.resolveReferences(references)),
      assertions.map(c => c.resolveReferences(references)),
      only
    )
  }
}
