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

import io.restassured.RestAssured.`given`
import io.restassured.http.ContentType
import io.restassured.module.scala.RestAssuredSupport.AddThenToResponse

object SuiteRunnerJob {

  def runSuites(suites: Set[Suite]): Unit = {
    // Note: this is initial example logic to in touch with target library - Rest Assured
    given()
      .headers(
        "Authorization",
        "Bearer " + "bearerToken",
        "Content-Type",
        ContentType.JSON
      )
      .accept("")
    .when()
//      .get("http://localhost:8080/restcontroller/AULGUI/user")
      .get("http://google.com")
    .Then()
      .statusCode(200)
      .extract()
      .response().body()
  }
}
