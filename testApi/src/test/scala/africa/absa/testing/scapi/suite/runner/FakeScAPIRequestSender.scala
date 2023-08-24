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

package africa.absa.testing.scapi.suite.runner

import africa.absa.testing.scapi.rest.request.sender.RequestSender
import africa.absa.testing.scapi.rest.response.Response

object FakeScAPIRequestSender extends RequestSender {
  /**
   * A predetermined response that this fake request sender will return
   */
  var mockResponse: Response = Response(
    200,
    "[{\"id\":\"efa01eeb-34cb-42da-b150-ca6dbe52xxx1\",\"domainName\":\"Domain1\"},{\"id\":\"382be85a-1f00-4c15-b607-cbda03ccxxx2\",\"domainName\":\"Domain2\"},{\"id\":\"65173a5b-b13c-4db0-bd1b-24b3e3abxxx3\",\"domainName\":\"Domain3\"}]",
    "no url",
    "fake status messsage",
    Map("Content-Type" -> Seq("application/json")),
    Map.empty,
    100
  )

  override def get(url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response = {
    mockResponse
  }

  override def post(url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response = {
    mockResponse
  }

  override def put(url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response = {
    mockResponse
  }

  override def delete(url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response = {
    mockResponse
  }
}
