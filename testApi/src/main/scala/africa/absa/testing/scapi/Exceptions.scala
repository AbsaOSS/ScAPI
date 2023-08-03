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

import com.networknt.schema.ValidationMessage

import scala.collection.mutable

case class UndefinedConstantsInProperties(undefinedConstants: Set[String], source: String)
  extends Exception(s"Undefined constant(s): '${undefinedConstants.mkString(", ")}' in '$source'.")

case class PropertyNotFound(property: String) extends Exception(s"Property not found: '$property'.")

case class JsonInvalidSchema(filePath: String, messages: mutable.Set[ValidationMessage])
  extends Exception(s"Json file '$filePath' not valid to defined json schema. " + messages.mkString("\n"))

case class ProjectLoadFailed() extends Exception("Problems during project loading.")

case class SuiteLoadFailed(detail: String) extends Exception(s"Problems during project loading. Details: $detail")

case class UndefinedHeaderType(undefinedType: String)
  extends Exception(s"Undefined Header content type: '$undefinedType'")

case class UndefinedAssertionType(undefinedType: String)
  extends Exception(s"Undefined Assertion content type: '$undefinedType'")

case class ContentValidationFailed(value: String, message: String)
  extends Exception(s"Content validation failed for value: '$value': $message")
