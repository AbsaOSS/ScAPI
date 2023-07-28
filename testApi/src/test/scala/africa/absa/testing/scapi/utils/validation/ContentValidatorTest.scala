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

package africa.absa.testing.scapi.utils.validation

import africa.absa.testing.scapi.ContentValidationFailed
import munit.FunSuite

class ContentValidatorTest extends FunSuite {

  /*
    validateIntegerString
   */

  test("validateIntegerString - succeeds with valid integer string") {
    ContentValidator.validateIntegerString("123", "unitTest")
  }

  test("validateIntegerString - fails with non-integer string") {
    intercept[ContentValidationFailed] {
      ContentValidator.validateIntegerString("not an integer", "unitTest")
    }
  }

  test("validateIntegerString - fails with empty string") {
    intercept[ContentValidationFailed] {
      ContentValidator.validateIntegerString("", "unitTest")
    }
  }

  /*
    validateIntegerString
   */

  test("validateNonEmptyString succeeds with non-empty string") {
    ContentValidator.validateNonEmptyString("non-empty string", "unitTest")
  }

  test("validateNonEmptyString - fails with empty string") {
    intercept[ContentValidationFailed] {
      ContentValidator.validateNonEmptyString("", "unitTest")
    }
  }
}
