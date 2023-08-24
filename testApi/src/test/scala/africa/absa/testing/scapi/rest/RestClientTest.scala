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
import munit.FunSuite

class RestClientTest extends FunSuite {

  val mockRequestSender: RequestSender = new RequestSender {
    private def action(url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response = {
      assert(url == "testUrl")
      assert(headers == Map("Authorization" -> "Bearer testToken"))
      assert(verifySslCerts == false)
      assert(data == "testData")
      assert(params == Map("param1" -> "value1"))

      Response(200, "test response", "", "", Map.empty[String, Seq[String]], Map.empty, 100)
    }

    override def get(url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response = {
      action(url, headers, verifySslCerts, data, params)
    }

    override def post(url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response = {
      action(url, headers, verifySslCerts, data, params)
    }

    override def put(url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response = {
      action(url, headers, verifySslCerts, data, params)
    }

    override def delete(url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response = {
      action(url, headers, verifySslCerts, data, params)
    }
  }

  /*
    sendRequest
   */

  test("sendRequest - call get with correct parameters") {
    val restClient: RestClient = new RestClient(mockRequestSender)
    val response: Response = restClient.sendRequest("get", "testUrl", "testData", Map("Authorization" -> "Bearer testToken"), Map("param1" -> "value1"), false)

    assert(response.statusCode == 200)
    assert(response.body == "test response")
  }

  test("sendRequest - call post with correct parameters") {
    val restClient: RestClient = new RestClient(mockRequestSender)
    val response: Response = restClient.sendRequest("post", "testUrl", "testData", Map("Authorization" -> "Bearer testToken"), Map("param1" -> "value1"), false)

    assert(response.statusCode == 200)
    assert(response.body == "test response")
  }

  test("sendRequest - call put with correct parameters") {
    val restClient: RestClient = new RestClient(mockRequestSender)
    val response: Response = restClient.sendRequest("put", "testUrl", "testData", Map("Authorization" -> "Bearer testToken"), Map("param1" -> "value1"), false)

    assert(response.statusCode == 200)
    assert(response.body == "test response")
  }

  test("sendRequest - call delete with correct parameters") {
    val restClient: RestClient = new RestClient(mockRequestSender)
    val response: Response = restClient.sendRequest("delete", "testUrl", "testData", Map("Authorization" -> "Bearer testToken"), Map("param1" -> "value1"), false)

    assert(response.statusCode == 200)
    assert(response.body == "test response")
  }

  test("sendRequest - call not supported action") {
    val restClient: RestClient = new RestClient(mockRequestSender)

    interceptMessage[IllegalArgumentException]("RestClient:sendRequest - unexpected action method called") {
      restClient.sendRequest("not supported", "testUrl", "testData", Map("Authorization" -> "Bearer testToken"), Map("param1" -> "value1"), false)
    }
  }
}
