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

import spray.json._

import scala.util.{Failure, Try}

object RequestBody {

  def buildBody(jsonBody: Option[String] = None): String = {
    jsonBody match {
      case Some(body) if body.trim.nonEmpty => body.parseJson.toString()
      case _ => "{}"
    }
  }

  def validateContent(jsonBody: Option[String]): Unit = {
    jsonBody match {
      // check for non json input
      case Some(body) if body.trim.nonEmpty =>
        Try {
          body.parseJson
        } match {
          case Failure(e) =>
            throw ContentValidationFailed(body, s"Received value cannot be parsed to json: ${e.getMessage}")
          case _ => ()
        }
      case _ => ()
    }
  }
}
