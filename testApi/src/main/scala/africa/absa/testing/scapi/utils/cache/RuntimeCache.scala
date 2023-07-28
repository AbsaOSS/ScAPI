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

import africa.absa.testing.scapi.logging.functions.Scribe

import scala.collection.mutable

sealed trait RuntimeCacheLevel

case object GlobalLevel extends RuntimeCacheLevel

case object SuiteLevel extends RuntimeCacheLevel

case object TestLevel extends RuntimeCacheLevel

/** RuntimeCache is a utility object that provides a caching functionality.
 * It provides methods to store, retrieve, update and remove key-value pairs,
 * as well as reset the cache. Each key-value pair is associated with an expiration level.
 * There are methods to remove all pairs at a certain level and reset the entire cache.
 */
object RuntimeCache {
  private val cache: mutable.Map[String, (String, RuntimeCacheLevel)] = mutable.Map.empty[String, (String, RuntimeCacheLevel)]
  private var loggingFunctions: Option[Scribe] = None

  /**
   * Initializes logging functions.
   *
   * @param loggingFunctions the logging object to use
   */
  def initLogging(loggingFunctions: Scribe): Unit = this.loggingFunctions = Some(loggingFunctions)

  /**
   * Stores a key-value pair with a given expiration level.
   *
   * @param key the key
   * @param value the value
   * @param level the expiration level
   */
  def put(key: String, value: String, level: RuntimeCacheLevel = TestLevel): Unit = {
    if (!cache.contains(key)) {
      cache.put(key, (value, level))
    } else {
      println(s"Error - Put already existing key into cache is not allowed. No change applied.") // TODO - replace by logger call in Issue #11
    }
  }

  /**
   * Retrieves the value for a given key.
   *
   * @param key the key
   * @return the value associated with the key
   * @throws NoSuchElementException if the key is not found in the cache
   */
  def get(key: String): Option[String] = {
    // throw new NoSuchElementException(s"Key $key not found in cache")

    cache.get(key).map(_._1)
  }

  /**
   * Updates the value for a given key.
   * The expiration level is not changed.
   *
   * @param key the key
   * @param value the new value
   */
  def update(key: String, value: String): Unit = {
    cache.get(key) match {
      case Some((_, level)) => cache.update(key, (value, level))
      case None => throw new NoSuchElementException(s"Key $key not found in cache")
    }
  }

  /** Updates the value and the expiration level for a given key.
   *
   * @param key   the key
   * @param value the new value
   * @param level the new expiration level
   */
  def update(key: String, value: String, level: RuntimeCacheLevel): Unit = {
    cache.update(key, (value, level))
  }

  /**
   * Removes a key-value pair from the cache.
   *
   * @param key the key
   */
  def remove(key: String): Unit = {
    cache.remove(key)
  }

  /**
   * Removes all key-value pairs at a certain expiration level.
   *
   * @param level the expiration level
   */
  def expire(level: RuntimeCacheLevel): Unit = {
    level match {
      case TestLevel =>
        cache.filterInPlace { case (_, (_, cacheLevel)) => cacheLevel != TestLevel }

      case SuiteLevel =>
        cache.filterInPlace { case (_, (_, cacheLevel)) => cacheLevel != SuiteLevel && cacheLevel != TestLevel }

      case GlobalLevel => reset()
    }
  }

  /** Clears all entries from the cache.
   */
  def reset(): Unit = {
    cache.clear()
  }

  /** Determines the expiration level constant for a given string.
   *
   * @param level the input string
   * @return the defined constant for a known level, or TEST_LEVEL for an unknown level
   */
  def determineLevel(level: String): RuntimeCacheLevel = {
    level.toLowerCase match {
      case "global" => GlobalLevel
      case "suite" => SuiteLevel
      case "test" => TestLevel
      case _ => {
        if (loggingFunctions.nonEmpty)
          this.loggingFunctions.get.warning(s"Not known expiration cache level: '$level'. Used default TEST level.")
        TestLevel
      }
    }
  }

  /**
   * Performs replacement of cache keys wrapped in {{ }} with their respective values in the cache.
   * Throws exception if a key isn't found.
   *
   * @param toResolve the string that may contain cache keys to be resolved
   * @return the input string with all cache keys resolved
   * @throws NoSuchElementException when a cache key is not found in the cache
   */
  def resolve(toResolve: String): String = {
    val pattern = """\{\{\s*(.*?)\s*}}""".r

    pattern.replaceAllIn(toResolve, { matchResult =>
      val propertyKey: String = matchResult.group(1).trim

      if (propertyKey.startsWith("cache.")) {
        val cacheKey = propertyKey.split("\\.").drop(1).mkString(".")
        cache.get(cacheKey) match {
          case Some((value, _)) => value
          case None => throw new NoSuchElementException(s"Key not found in cache: $cacheKey")
        }
      } else {
        matchResult.group(0) // return the full match including the {{ and }}
      }
    })
  }
}
