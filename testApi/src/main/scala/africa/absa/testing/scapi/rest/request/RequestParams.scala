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

package africa.absa.testing.scapi.rest.request

import africa.absa.testing.scapi.json.Param
import africa.absa.testing.scapi.utils.cache.RuntimeCache
import africa.absa.testing.scapi.utils.validation.ContentValidator

/**
 * A singleton object that provides utility methods to build and validate request parameters.
 */
object RequestParams {

  /**
   * Builds a Map of parameters from an optional Set of Param objects.
   * If a parameter's name and value are non-empty, it gets added to the map, with its value resolved from the RuntimeCache.
   *
   * @param paramsSet An optional Set of Param objects.
   * @return A Map of parameter names to their resolved values.
   */
  def buildParams(paramsSet: Option[Set[Param]]): Map[String, String] = {
    paramsSet.getOrElse(Set.empty[Param]).foldLeft(Map.empty[String, String]) {
      (acc, param) =>
        if (param.name.trim.nonEmpty && param.value.trim.nonEmpty) {
          acc + (param.name -> RuntimeCache.resolve(param.value))
        } else {
          acc
        }
    }
  }

  /**
   * Validates the content of an optional Set of Param objects.
   * Specifically, it checks that the name of each parameter is a non-empty string.
   *
   * @param paramsSet An optional Set of Param objects.
   * @throws IllegalArgumentException if a param name is empty.
   */
  def validateContent(paramsSet: Option[Set[Param]]): Unit = {
    paramsSet.foreach { params =>
      params.foreach { param =>
        ContentValidator.validateNonEmptyString(param.name)
      }
    }
  }
}
