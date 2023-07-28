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
 * Represents a suite of tests, with optional "before" and "after" setup/teardown.
 *
 * @constructor Create a new suite bundle with a suite and optional "before" and "after" actions.
 * @param suite The core suite of tests to be run.
 * @param suiteBefore An optional SuiteBefore object, representing any setup actions to be run before the suite.
 * @param suiteAfter An optional SuiteAfter object, representing any teardown actions to be run after the suite.
 */
case class SuiteBundle(suite: Suite, suiteBefore: Option[SuiteBefore] = None, suiteAfter: Option[SuiteAfter] = None)
