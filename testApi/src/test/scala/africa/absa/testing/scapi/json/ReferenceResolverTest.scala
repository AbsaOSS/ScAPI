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

package africa.absa.testing.scapi.json

import africa.absa.testing.scapi.rest.response.action.types.{AssertResponseActionType, ResponseActionGroupType}
import africa.absa.testing.scapi.utils.cache.{GlobalLevel, RuntimeCache}
import munit.FunSuite

class ReferenceResolverTest extends FunSuite {

  val action: Action = Action(
    method = "get",
    url = "nice/{{ cache.urlValue }}",
    body = Option("body {{ cache.surpriseValue }}"),
    params = Option(Set(Param("message", "value nr.: {{ cache.numberValue }}")))
  )

  val header: Header = Header(
    name = "name",
    value = "value+{{ cache.surpriseValue }}"
  )

  val responseAction: ResponseAction = ResponseAction(
    group = ResponseActionGroupType.Assert,
    name = AssertResponseActionType.ResponseTimeIsBelow.toString,
    Map("limit" -> "{{ cache.responseTime }}"))

  /*
    Header
   */

  test("Header - resolveByRuntimeCache - reference present") {
    RuntimeCache.put("surpriseValue", "boo boo", GlobalLevel)

    val resolvedHeader = header.resolveByRuntimeCache()

    assert("value+boo boo" == clue(resolvedHeader.value))

    RuntimeCache.reset()
  }

  test("Header - resolveByRuntimeCache - reference not present") {
    interceptMessage[NoSuchElementException]("Key not found in cache: surpriseValue") {
      header.resolveByRuntimeCache()
    }
  }

  /*
    Action
   */

  test("Action - resolveByRuntimeCache - reference present") {
    RuntimeCache.put("urlValue", "url", GlobalLevel)
    RuntimeCache.put("surpriseValue", "boo boo", GlobalLevel)
    RuntimeCache.put("numberValue", "123", GlobalLevel)

    val resolvedAction = action.resolveByRuntimeCache()

    assert("nice/url" == clue(resolvedAction.url))
    assert("body boo boo" == clue(resolvedAction.body.get))
    assert("value nr.: 123" == clue(resolvedAction.params.get.head.value))

    RuntimeCache.reset()
  }

  test("Action - resolveByRuntimeCache - reference not present") {
    interceptMessage[NoSuchElementException]("Key not found in cache: urlValue") {
      action.resolveByRuntimeCache()
    }
  }

  /*
    ResponseAction
   */

  test("ResponseAction - resolveByRuntimeCache - reference present") {
    RuntimeCache.put("responseTime", "987", GlobalLevel)

    val resolvedResponseAction = responseAction.resolveByRuntimeCache()

    assert("987" == clue(resolvedResponseAction.params("limit")))

    RuntimeCache.reset()
  }

  test("ResponseAction - resolveByRuntimeCache - reference not present") {
    interceptMessage[NoSuchElementException]("Key not found in cache: responseTime") {
      responseAction.resolveByRuntimeCache()
    }
  }

}
