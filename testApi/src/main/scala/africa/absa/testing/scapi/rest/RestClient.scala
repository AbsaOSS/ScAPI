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

package africa.absa.testing.scapi.rest

import africa.absa.testing.scapi.rest.request.sender.RequestSender
import africa.absa.testing.scapi.rest.response.Response

/**
 * A class to handle RESTful requests. The RestClient is instantiated with a specific requestSender
 * which is then used to perform HTTP requests.
 *
 * @param requestSender An instance of a class that can send HTTP requests.
 */
class RestClient(requestSender: RequestSender) {
  /**
   * Sends a RESTful request to the specified URL using the specified method.
   * The method supports 'get', 'post', 'put', 'delete'. Other methods will throw an exception.
   *
   * @param method         HTTP method as a string. Supported methods are 'get', 'post', 'put', 'delete'.
   * @param url            The URL to which the request should be sent.
   * @param body           The body content to be included in the request.
   * @param headers        Map of header keys and values to be included in the request.
   * @param params         Map of parameters to be included in the request URL.
   * @param verifySslCerts A boolean flag indicating whether to verify SSL certificates.
   *                       Default is set to false.
   * @return Response The response from the server.
   * @throws IllegalArgumentException If the method is not 'get', 'post', 'put' or 'delete'.
   */
  def sendRequest(method: String, url: String, body: String, headers: Map[String, String], params: Map[String, String], verifySslCerts: Boolean = false): Response = {
    method.toLowerCase match {
      case "get" => requestSender.get(url = url, headers = headers, verifySslCerts = verifySslCerts, data = body, params = params)
      case "post" => requestSender.post(url = url, headers = headers, verifySslCerts = verifySslCerts, data = body, params = params)
      case "put" => requestSender.put(url = url, headers = headers, verifySslCerts = verifySslCerts, data = body, params = params)
      case "delete" => requestSender.delete(url = url, headers = headers, verifySslCerts = verifySslCerts, data = body, params = params)
      case _ => throw new IllegalArgumentException("RestClient:sendRequest - unexpected action method called")
    }
  }
}
