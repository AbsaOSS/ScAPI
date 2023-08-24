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

package africa.absa.testing.scapi.rest.request.sender

import africa.absa.testing.scapi.rest.response.Response

import java.net.HttpCookie

/**
 * ScAPIRequestSender is an implementation of the RequestSender interface.
 * It provides the capability to send different types of HTTP requests including GET, POST, PUT, and DELETE.
 */
object ScAPIRequestSender extends RequestSender {

  private def sendRequest(requestFunc: => requests.Response, url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response = {
    val startTime = System.nanoTime()
    val response = requestFunc
    val endTime = System.nanoTime()

    val extractedCookies: Map[String, (String, Boolean)] = response.cookies.view.map {
      case (name, cookie: HttpCookie) =>
        (name, (cookie.getValue, cookie.getSecure))
    }.toMap

    Response(
      response.statusCode,
      response.text(),
      url = response.url,
      statusMessage = response.statusMessage,
      response.headers,
      cookies = extractedCookies,
      (endTime - startTime) / 1_000_000
    )
  }

  /**
   * Sends a GET request to the provided URL with the given headers, SSL certificate verification setting, data, and parameters.
   *
   * @param url            The URL to which the GET request is to be sent.
   * @param headers        The headers to include in the GET request.
   * @param verifySslCerts A boolean flag indicating whether to verify SSL certificates.
   * @param data           The data to send with the GET request.
   * @param params         The parameters to include in the GET request.
   * @return Response Returns the response from the GET request.
   */
  override def get(url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response = {
    sendRequest(requests.get(url = url, headers = headers, verifySslCerts = verifySslCerts, data = data, params = params), url, headers, verifySslCerts, data, params)
  }

  /**
   * Sends a POST request to the provided URL with the given headers, SSL certificate verification setting, data, and parameters.
   *
   * @param url            The URL to which the POST request is to be sent.
   * @param headers        The headers to include in the POST request.
   * @param verifySslCerts A boolean flag indicating whether to verify SSL certificates.
   * @param data           The data to send with the POST request.
   * @param params         The parameters to include in the POST request.
   * @return Response Returns the response from the POST request.
   */
  override def post(url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response = {
    sendRequest(requests.post(url = url, headers = headers, verifySslCerts = verifySslCerts, data = data, params = params), url, headers, verifySslCerts, data, params)
  }

  /**
   * Sends a PUT request to the provided URL with the given headers, SSL certificate verification setting, data, and parameters.
   *
   * @param url            The URL to which the PUT request is to be sent.
   * @param headers        The headers to include in the PUT request.
   * @param verifySslCerts A boolean flag indicating whether to verify SSL certificates.
   * @param data           The data to send with the PUT request.
   * @param params         The parameters to include in the PUT request.
   * @return Response Returns the response from the PUT request.
   */
  override def put(url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response = {
    sendRequest(requests.put(url = url, headers = headers, verifySslCerts = verifySslCerts, data = data, params = params), url, headers, verifySslCerts, data, params)
  }

  /**
   * Sends a DELETE request to the provided URL with the given headers, SSL certificate verification setting, data, and parameters.
   *
   * @param url            The URL to which the DELETE request is to be sent.
   * @param headers        The headers to include in the DELETE request.
   * @param verifySslCerts A boolean flag indicating whether to verify SSL certificates.
   * @param data           The data to send with the DELETE request.
   * @param params         The parameters to include in the DELETE request.
   * @return Response Returns the response from the DELETE request.
   */
  override def delete(url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response = {
    sendRequest(requests.delete(url = url, headers = headers, verifySslCerts = verifySslCerts, data = data, params = params), url, headers, verifySslCerts, data, params)
  }
}
