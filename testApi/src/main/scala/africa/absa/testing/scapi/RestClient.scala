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

import africa.absa.testing.scapi.logging.functions.Scribe

object RestClient {
  implicit val loggingFunctions: Scribe = Scribe(this.getClass)

  def sendRequest(method: String, url: String, body: Option[String], headers: Map[String, String], verifySslCerts: Boolean = false): Response = {
    loggingFunctions.debug(s"RestClient:sendRequest url: '$url', body: '$body', headers: '$headers', verifySslCerts: '$verifySslCerts'")

    // TODO - commented part will require :" import ujson._

    val response = method.toLowerCase match {
      case "get" => requests.get(url = url, headers = headers, verifySslCerts = verifySslCerts)
//      case "put" => requests.put(url, data = body.map(ujson.Str).getOrElse(ujson.Obj()), headers = headers)
//      case "delete" => requests.delete(url)
      case _ => throw new IllegalArgumentException("Invalid action method")
    }
    Response(response.statusCode, response.text(), response.headers.toMap)
  }
}
