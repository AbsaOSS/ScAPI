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

package africa.absa.testing.scapi.model.suite

/**
 * Case class that represents a suite after methods.
 *
 * @param name The name of the after methods.
 * @param methods The set of suite after methods.
 */
case class AfterTestSet(name: String, methods: Set[Method]) extends SuitePreAndPostProcessing(name, methods) {
  override def resolveReferences(references: Map[String, String]): SuitePreAndPostProcessing = {
    AfterTestSet(
      name,
      methods.map(c => c.resolveReferences(references))
    )
  }
}
