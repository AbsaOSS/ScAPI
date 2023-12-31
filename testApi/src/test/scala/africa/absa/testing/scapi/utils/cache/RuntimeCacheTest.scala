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

package africa.absa.testing.scapi.utils.cache

import munit.FunSuite

class RuntimeCacheTest extends FunSuite {

  override def beforeEach(context: BeforeEach): Unit = {
    RuntimeCache.reset()
    super.beforeEach(context) // important to call this
  }

  /*
    put
   */

  test("put") {
    RuntimeCache.put("key", "value")
    assert(clue(Some("value")) == clue(RuntimeCache.get("key")))
  }

  test("put - with level") {
    RuntimeCache.put("g", "g", GlobalLevel)
    RuntimeCache.put("s", "s", SuiteLevel)
    RuntimeCache.put("t", "t", TestLevel)
    assert(clue(Some("g")) == clue(RuntimeCache.get("g")))
    assert(clue(Some("s")) == clue(RuntimeCache.get("s")))
    assert(clue(Some("t")) == clue(RuntimeCache.get("t")))
  }

  test("put - already exists - on same level") {
    RuntimeCache.put("key", "valueA")
    RuntimeCache.put("key", "valueB")
    assert(clue(Some("valueA")) == clue(RuntimeCache.get("key")))
  }

  test("put - already exists - on different level") {
    RuntimeCache.put("key", "valueA", TestLevel)
    RuntimeCache.put("key", "valueB", SuiteLevel)
    assert(clue(Some("valueA")) == clue(RuntimeCache.get("key")))
  }

  /*
    get
   */
  // smoke possitive tested during put tests - skipped here

  test("get - nonexistent key") {
    assert(clue(RuntimeCache.get("nonexistent")).isEmpty)
  }

  /*
    update
   */

  test("update") {
    RuntimeCache.put("key", "value")
    RuntimeCache.update("key", "newValue")
    assert(clue(Some("newValue")) == clue(RuntimeCache.get("key")))
  }

  test("update - nonexistent key") {
    interceptMessage[NoSuchElementException]("Key nonexistent not found in cache") {
      RuntimeCache.update("nonexistent", "value")
    }
  }

  test("update - with level up") {
    RuntimeCache.put("key", "value", TestLevel)
    RuntimeCache.update("key", "newValue", SuiteLevel)

    RuntimeCache.expire(TestLevel)

    assert(clue(Some("newValue")) == clue(RuntimeCache.get("key")))
  }

  test("update - with level down") {
    RuntimeCache.put("key", "value", GlobalLevel)
    RuntimeCache.update("key", "newValue", TestLevel)

    RuntimeCache.expire(TestLevel)

    assert(clue(RuntimeCache.get("nonexistent")).isEmpty)
  }

  /*
    remove
   */

  test("remove") {
    RuntimeCache.put("key", "value")
    RuntimeCache.remove("key")

    assert(RuntimeCache.get("nonexistent").isEmpty)
  }

  test("remove - key no exist") {
    // no exception thrown
    RuntimeCache.remove("key")
  }

  /*
    expire
   */

  test("expire - global level") {
    RuntimeCache.put("key1", "value1", GlobalLevel)
    RuntimeCache.put("key2", "value2", SuiteLevel)
    RuntimeCache.put("key3", "value3", TestLevel)

    RuntimeCache.expire(GlobalLevel)

    assert(clue(RuntimeCache.get("key1")).isEmpty)
    assert(clue(RuntimeCache.get("key2")).isEmpty)
    assert(clue(RuntimeCache.get("key3")).isEmpty)
  }

  test("expire - suite level") {
    RuntimeCache.put("key1", "value1", SuiteLevel)
    RuntimeCache.put("key2", "value2", TestLevel)

    RuntimeCache.expire(SuiteLevel)

    assert(clue(RuntimeCache.get("key1")).isEmpty)
    assert(clue(RuntimeCache.get("key2")).isEmpty)
  }

  test("expire - test level") {
    RuntimeCache.put("key1", "value1", SuiteLevel)
    RuntimeCache.put("key2", "value2", TestLevel)

    RuntimeCache.expire(TestLevel)

    assert(clue(RuntimeCache.get("key2")).isEmpty)
    assert(clue(Some("value1")) == clue(RuntimeCache.get("key1")))
  }

  /*
    reset
   */

  test("reset") {
    RuntimeCache.put("key1", "value1", GlobalLevel)
    RuntimeCache.put("key2", "value2", SuiteLevel)
    RuntimeCache.put("key3", "value3", TestLevel)

    RuntimeCache.reset()

    assert(clue(RuntimeCache.get("key1")).isEmpty)
    assert(clue(RuntimeCache.get("key2")).isEmpty)
    assert(clue(RuntimeCache.get("key3")).isEmpty)
  }

  /*
    determineLevel
   */

  test("determineLevel") {
    assert(GlobalLevel == clue(RuntimeCache.determineLevel("global")))
    assert(SuiteLevel == clue(RuntimeCache.determineLevel("suite")))
    assert(TestLevel == clue(RuntimeCache.determineLevel("test")))
    assert(TestLevel == clue(RuntimeCache.determineLevel("unknown")))
  }

  /*
    resolve
   */

  test("resolve") {
    RuntimeCache.put("key", "value")
    assert("value".==(clue(RuntimeCache.resolve("{{ cache.key }}"))))
  }

  test("resolve - key not exist") {
    interceptMessage[NoSuchElementException]("Key not found in cache: notExist") {
      RuntimeCache.resolve("{{ cache.notExist }}")
    }
  }

  test("resolve - multiple placeholders, some keys not exist") {
    RuntimeCache.put("key", "value")
    intercept[NoSuchElementException] {
      RuntimeCache.resolve("{{ cache.key }} and {{ cache.notExist }}")
    }
  }

  test("resolve - no placeholder to resolve") {
    RuntimeCache.put("key", "value")
    assert("cache.key" == clue(RuntimeCache.resolve("cache.key")))
  }

  test("resolve - mixed placeholders") {
    RuntimeCache.put("key", "value")
    assert("value and {{ not.cache.key }}" == clue(RuntimeCache.resolve("{{ cache.key }} and {{ not.cache.key }}")))
  }

  test("resolve - empty key") {
    interceptMessage[NoSuchElementException]("Key not found in cache: ") {
      RuntimeCache.resolve("{{ cache. }}")
    }
  }

  test("resolve - empty value") {
    RuntimeCache.put("key", "")
    assert("".==(clue(RuntimeCache.resolve("{{ cache.key }}"))))
  }
}
