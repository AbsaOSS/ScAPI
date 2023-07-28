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

package africa.absa.testing.scapi.model

/**
 * Case class that represents a suite before methods.
 *
 * @param name The name of the before methods.
 * @param methods The set of suite before methods.
 */
case class SuiteBefore(name: String, methods: Set[Method]) {
  /**
   * Method to resolve references within the before methods instance.
   *
   * @param references A map containing the references to be resolved.
   * @return A new SuiteBefore instance where all references are resolved.
   */
  def resolveReferences(references: Map[String, String]): SuiteBefore = {
    SuiteBefore(
      name,
      methods.map(c => c.resolveReferences(references))
    )
  }
}
