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

trait RequestSender {
  def get(url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response
  def post(url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response
  def put(url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response
  def delete(url: String, headers: Map[String, String], verifySslCerts: Boolean, data: String, params: Map[String, String]): Response
}
