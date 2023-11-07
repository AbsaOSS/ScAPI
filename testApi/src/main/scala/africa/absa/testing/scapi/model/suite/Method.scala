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

import africa.absa.testing.scapi.json.{Action, Header, Requestable, ResponseAction}

/**
 * Case class that represents a single method.
 *
 * @param name The name of the method.
 * @param headers The set of header options for the method.
 * @param action The action object for the method.
 * @param responseActions The set of responseAction objects for the method.
 */
case class Method(name: String,
                  headers: Seq[Header],
                  action: Action,
                  responseActions: Seq[ResponseAction]) extends Requestable {
  /**
   * Method to resolve references within the Method instance.
   *
   * @param references A map containing the references to be resolved.
   * @return A new Method instance where all references are resolved.
   */
  def resolveReferences(references: Map[String, String]): Method = {
    Method(
      name,
      headers.map(c => c.resolveReferences(references)),
      action.resolveReferences(references),
      responseActions.map(c => c.resolveReferences(references))
    )
  }
}
