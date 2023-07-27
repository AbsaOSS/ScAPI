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

/** RuntimeCache is a utility object that provides a caching functionality.
 * It provides methods to store, retrieve, update and remove key-value pairs,
 * as well as reset the cache. Each key-value pair is associated with an expiration level.
 * There are methods to remove all pairs at a certain level and reset the entire cache.
 */
object RuntimeCache {
  private val cache: mutable.Map[String, (String, String)] = mutable.Map.empty[String, (String, String)]
  private var loggingFunctions: Option[Scribe] = None

  val GLOBAL: String = "global"
  val SUITE: String = "suite"
  val TEST: String = "test"

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
  def put(key: String, value: String, level: String = TEST): Unit = {
    cache.put(key, (value, level))
  }

  /**
   * Retrieves the value for a given key.
   *
   * @param key the key
   * @return the value, or None if the key is not in the cache
   */
  def get(key: String): Option[String] = cache.get(key).map(_._1)

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
  def update(key: String, value: String, level: String): Unit = {
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
  def expire(level: String): Unit = {
    cache.filterInPlace { case (_, (_, cacheLevel)) => cacheLevel != level }
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
  def determineLevel(level: String): String = {
    level.toLowerCase match {
      case GLOBAL => GLOBAL
      case SUITE => SUITE
      case _ => {
        if (loggingFunctions.nonEmpty)
          this.loggingFunctions.get.warning(s"Not known expiration cache level: '$level'. Used default TEST level.")
        TEST
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
// TODO - add logic call to expire after test run
// TODO - add logic call to expire after suite run
