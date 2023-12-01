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

import africa.absa.testing.scapi.ContentValidationFailedException
import africa.absa.testing.scapi.utils.cache.RuntimeCache
import spray.json.JsonParser.ParsingException
import spray.json._

import scala.util.{Failure, Try}

/**
 * The RequestBody object provides methods for constructing and validating JSON-based HTTP request bodies.
 */
object RequestBody {

  /**
   * Constructs the request body for a HTTP request from an optional JSON body.
   * If the JSON body is not provided or is an empty string, it defaults to an empty JSON object ("{}").
   *
   * @param jsonBody An optional string containing the JSON body. If None or empty string, an empty JSON object ("{}") is returned.
   * @return A string representing the HTTP request body.
   * @throws ContentValidationFailedException if JSON body parsing to a string fails at runtime.
   */
  def buildBody(jsonBody: Option[String] = None): String = {
    jsonBody match {
      case Some(body) if body.trim.nonEmpty =>
        try {
          val jsonAst = body.parseJson // Attempt to parse the JSON
          RuntimeCache.resolve(jsonAst.toString()) // If successful, resolve
        } catch {
          case e: ParsingException => throw new IllegalArgumentException("Invalid JSON string provided in Action body.")
        }
      case _ => "{}"
    }
  }

  /**
   * Validates the provided JSON body by attempting to parse it.
   * If the JSON body cannot be parsed, a ContentValidationFailed exception is thrown.
   *
   * @param jsonBody An optional string containing the JSON body to validate. If None or empty, the method simply returns.
   * @throws ContentValidationFailedException if the provided JSON body cannot be parsed.
   */
  def validateContent(jsonBody: Option[String]): Unit = {
    jsonBody match {
      // check for non json input
      case Some(body) if body.trim.nonEmpty =>
        Try {
          body.parseJson
        } match {
          case Failure(e) =>
            throw ContentValidationFailedException(body, s"Received value cannot be parsed to json: ${e.getMessage}")
          case _ => ()
        }
      case _ => ()
    }
  }
}
