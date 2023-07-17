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

object RestClient {
  def sendRequest(method: String, url: String, body: String, headers: Map[String, String], params: Map[String, String], verifySslCerts: Boolean = false): Response = {
    // TODO - how to send correct object instead of empty body json string

    val response = method.toLowerCase match {
      case "get" => requests.get(url = url, headers = headers, verifySslCerts = verifySslCerts, data = body, params = params)
      case "post" => requests.post(url = url, headers = headers, verifySslCerts = verifySslCerts, data = body, params = params)
      case "put" => requests.put(url = url, headers = headers, verifySslCerts = verifySslCerts, data = body, params = params)
      case "delete" => requests.delete(url = url, headers = headers, verifySslCerts = verifySslCerts, data = body, params = params)
      case _ => throw new IllegalArgumentException("RestClient:sendRequest - unexpected action method called")
    }
    Response(response.statusCode, response.text(), response.headers.toMap)
  }
}
