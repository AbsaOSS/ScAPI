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

import java.io.{ByteArrayOutputStream, PrintStream}

class ScAPIRunnerIsolatedTest extends FunSuite {

  test("call main with minimum params - report of failures") {
    val baos = new ByteArrayOutputStream()
    val ps = new PrintStream(baos)
    val oldOut = System.out
    val oldErr = System.err

    System.setOut(ps)
    System.setErr(ps)

    val args: Array[String] = Array(
      "--env", getClass.getResource("/test_project/localhost.env.json").getPath,
      "--test-root-path", getClass.getResource("/test_project").getPath)
    ScAPIRunner.main(args)

    ps.flush()
    System.setOut(oldOut)
    System.setErr(oldErr)
    val output = baos.toString("UTF-8")
    baos.close()
    ps.close()

    assert(output.contains("* Simple Text Report *"))
    assert(output.contains("| getOwners Demo Suite    | SKIPPED                  |             0 | Failure | SKIPPED    |"))
    assert(output.contains("Before: getOwners Demo Before"))
    assert(output.contains("Error: Connection refused"))
    assert(output.contains("Test: SKIPPED"))
    assert(output.contains("Error: Problems during running before suite logic. Details: Suite-Before for Suite: getOwners Demo Suite has failed methods. Not executing main tests and Suite-After."))
  }
}
