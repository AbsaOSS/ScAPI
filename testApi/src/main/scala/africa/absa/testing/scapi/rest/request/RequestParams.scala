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
import africa.absa.testing.scapi.utils.validation.ContentValidator

object RequestParams {

  def buildParams(paramsSet: Option[Set[Param]]): Map[String, String] = {
    paramsSet.getOrElse(Set.empty[Param]).foldLeft(Map.empty[String, String]) {
      (acc, param) =>
        if (param.name.trim.nonEmpty && param.value.trim.nonEmpty) {
          acc + (param.name -> param.value)
        } else {
          acc
        }
    }
  }

  def validateContent(paramsSet: Option[Set[Param]]): Unit = {
    paramsSet.foreach { params =>
      params.foreach { param =>
        ContentValidator.validateNonEmptyString(param.name)
      }
    }
  }
}
