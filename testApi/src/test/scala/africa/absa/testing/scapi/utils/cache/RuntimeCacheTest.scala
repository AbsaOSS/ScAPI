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
    assertEquals(RuntimeCache.get("key"), Some("value"))
  }

  test("put - with level") {
    RuntimeCache.put("g", "g", GlobalLevel)
    RuntimeCache.put("s", "s", SuiteLevel)
    RuntimeCache.put("t", "t", TestLevel)
    assertEquals(RuntimeCache.get("g"), Some("g"))
    assertEquals(RuntimeCache.get("s"), Some("s"))
    assertEquals(RuntimeCache.get("t"), Some("t"))
  }

  test("put - already exists - on same level") {
    RuntimeCache.put("key", "valueA")
    RuntimeCache.put("key", "valueB")
    assertEquals(RuntimeCache.get("key"), Some("valueA"))
  }

  test("put - already exists - on different level") {
    RuntimeCache.put("key", "valueA", TestLevel)
    RuntimeCache.put("key", "valueB", SuiteLevel)
    assertEquals(RuntimeCache.get("key"), Some("valueA"))
  }

  /*
    get
   */
  // smoke possitive tested during put tests - skipped here

  test("get - nonexistent key") {
    assertEquals(None, RuntimeCache.get("nonexistent"))
  }

  /*
    update
   */

  test("update") {
    RuntimeCache.put("key", "value")
    RuntimeCache.update("key", "newValue")
    assertEquals(RuntimeCache.get("key"), Some("newValue"))
  }

  test("update - nonexistent key") {
    intercept[NoSuchElementException] {
      RuntimeCache.update("nonexistent", "value")
    }
  }

  test("update - with level up") {
    RuntimeCache.put("key", "value", TestLevel)
    RuntimeCache.update("key", "newValue", SuiteLevel)

    RuntimeCache.expire(TestLevel)

    assertEquals(RuntimeCache.get("key"), Some("newValue"))
  }

  test("update - with level down") {
    RuntimeCache.put("key", "value", GlobalLevel)
    RuntimeCache.update("key", "newValue", TestLevel)

    RuntimeCache.expire(TestLevel)

    assertEquals(None, RuntimeCache.get("nonexistent"))
  }

  /*
    remove
   */

  test("remove") {
    RuntimeCache.put("key", "value")
    RuntimeCache.remove("key")

    assertEquals(None, RuntimeCache.get("nonexistent"))
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

    assertEquals(None, RuntimeCache.get("key1"))
    assertEquals(None, RuntimeCache.get("key2"))
    assertEquals(None, RuntimeCache.get("key3"))
  }

  test("expire - suite level") {
    RuntimeCache.put("key1", "value1", SuiteLevel)
    RuntimeCache.put("key2", "value2", TestLevel)

    RuntimeCache.expire(SuiteLevel)

    assertEquals(None, RuntimeCache.get("key1"))
    assertEquals(None, RuntimeCache.get("key2"))
  }

  test("expire - test level") {
    RuntimeCache.put("key1", "value1", SuiteLevel)
    RuntimeCache.put("key2", "value2", TestLevel)

    RuntimeCache.expire(TestLevel)

    assertEquals(None, RuntimeCache.get("key2"))
    assertEquals(RuntimeCache.get("key1"), Some("value1"))
  }

  /*
    reset
   */

  test("reset") {
    RuntimeCache.put("key1", "value1", GlobalLevel)
    RuntimeCache.put("key2", "value2", SuiteLevel)
    RuntimeCache.put("key3", "value3", TestLevel)

    RuntimeCache.reset()

    assertEquals(None, RuntimeCache.get("key1"))
    assertEquals(None, RuntimeCache.get("key2"))
    assertEquals(None, RuntimeCache.get("key3"))
  }

  /*
    determineLevel
   */

  test("determineLevel") {
    assertEquals(RuntimeCache.determineLevel("global"), GlobalLevel)
    assertEquals(RuntimeCache.determineLevel("suite"), SuiteLevel)
    assertEquals(RuntimeCache.determineLevel("test"), TestLevel)
    assertEquals(RuntimeCache.determineLevel("unknown"), TestLevel)
  }

  /*
    resolve
   */

  test("resolve") {
    RuntimeCache.put("key", "value")
    assertEquals(RuntimeCache.resolve("{{ cache.key }}"), "value")
  }

  test("resolve - key not exist") {
    intercept[NoSuchElementException] {
      RuntimeCache.resolve("{{ cache.notExist }}")
    }
  }

  test("resolve - no placeholder to resolve") {
    RuntimeCache.put("key", "value")
    assertEquals(RuntimeCache.resolve("cache.key"), "cache.key")
  }
}
