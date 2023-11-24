# ScAPI

  - [How to run api tests with ScAPI](#how-to-run-api-tests-with-scapi)
  - [How to run unit tests](#how-to-run-unit-tests)
  - [How to run integration tests](#how-to-run-integration-test)
  - [How to generate JaCoCo code coverage report](#how-to-generate-jacoco-code-coverage-report)
  - [How to generate jars](#how-to-generate-jars)
  - [How to run tests from jar file](#how-to-run-tests-from-jar-file)
  - [How to create env.json file](#how-to-create-envjson-file)
    - [Example of env.json file](#example-of-envjson-file)
    - [Rules to follow](#rules-to-follow)
  - [How to create suite.json file](#how-to-create-suitejson-file)
    - [Headers](#headers)
    - [Action](#action)
    - [Response actions](#response-actions)
      - [Assertions - response](#assertions---response)
      - [Assertions - status code](#assertions---status-code)
      - [Assertions - headers](#assertions---headers)
      - [Assertions - content](#assertions---content)
      - [Assertions - cookies](#assertions---cookies)
      - [Assertions - body](#assertions---body)
      - [Assertions - body - json](#assertions---body---json)
      - [Logging](#logging)
      - [Extract from JSON](#extract-from-json)

## How to run api tests with ScAPI
TODO - work in progress


---
## How to run unit tests
Run unit tests from path `{project-root}`
```
sbt test
```

## How to run integration test
Prepare testing environment:
- build assembly jar file of testApi module
- get running instance of [spring-petclinic-rest](https://github.com/spring-petclinic/spring-petclinic-rest) project
  - accessible swagger on path `http://localhost:9966/petclinic/swagger-ui/index.html`
  - re-run of instance is required after each test run (to reset database)

Run integration test from path `{project-root}/testApi/target/scala-2.13`
```
scala testApi-assembly-0.1.0-SNAPSHOT.jar --env ./../../src/test/resources/test_project/localhost.env.json --test-root-path ./../../src/test/resources/test_project/
```

Check report printed into console. All tests should be passed.

## How to generate JaCoCo code coverage report
Run command from path `{project-root}`
```
sbt jacoco
```
Report should be available on path `{project-root}/testApi/target/scala-2.13/jacoco/report/html`

## How to generate jars 
Run command from path `{project-root}`
```
sbt assembly
```
Jar files should be available on path `{project-root}/testApi/target/scala-2.13`

## How to run tests from jar file
Expect that you are in folder with test json files.
```
scala <path-to-assembly-jar>/testApi-assembly-0.1.0-SNAPSHOT.jar --env pc_1.json --test-root-path "."
```
```
scala testapi_2.13-assembly.jar --help                                                                                                                                                                                                ─╯
ScAPI Test Runner
Usage: ScAPI.jar [lib options] [options]

--env <value>                   Path to a file with an environment definition.
--test-root-path <value>        Path to a root directory of test definitions.
--filter <value>                Filter rule to select test definitions file (recursive) to include into test suite. Default is all '(.*)'.
--categories <v1>,<v2>          Select which test categories will be included into test suite. Default is all '[*]'
--thread-count <value>          Maximum count of thread used to run test suite. Default is '1'
--file-format <value>           Format of definition files. Default is all 'json'. Supported formats [json].
--report <value>                Path to a report output directory.
--validate-only                 Validate input definitions only. Default is 'false'
--debug                         Activate debug logging. Default is 'false'
--extra-vars k1=v1,k2=v2...     Extra variables that will be merged into the test definition json files. Overwrites the ones from env.

--help                          prints this usage text
```

Filtering examples
```
(.*)            .... [default] Find all suite files.
(.*)User(.*)    .... find all suite files which contain 'User'
User(.*)        .... find all suite files which begin with 'User'
(.*)User        .... find all suite files which end with 'User'
```

## How to create env.json file
### Example of env.json file
```json
{
  "constants": {
    "server": "localhost",
    "port": "8080"
  },
  "properties": {
    "url": "http://{{ server}}:{{ port }}/restcontroller"
  }
}
```

### Rules to follow
- **Constants** and **Properties** can contain only `String` type of values.
- **Constants** cannot contain reference `{{ key }}`.
- **Properties** can reference from **Constants** only.
- Multilevel reference `{{key_{{key_part_2}}}}` are not supported.
- Json elements follows `camelCase`.
- Json element methods follows `this-case`. 

## How to create suite.json file
#### Headers
- methods
  - "content-type"
  - "authorization"
#### Action
- methods
    - `get | post | put | delete`
  - arguments:
    - url: url string
    - body: json string (can be null)
    - params: list of url parameters in format [name, value] (can be null)

### Response actions
#### Assertions - response
- `assert.response-time-is-below`
  - description: Checks if the response time is below the specified maximum time.
  - arguments:
    - maxTimeMillis: The maximum allowed time in milliseconds.

- `assert.response-time-is-above`
  - description: Checks if the response time is above the specified minimum time.
  - arguments:
    - minTimeMillis: The minimum required time in milliseconds.

#### Assertions - status code
- `assert.status-code-equals`
  - description: Checks if the status code of the response matches the expected status code.
  - arguments:
    - expectedCode: The expected status code.

- `assert.status-code-is-success`
  - description: Checks if the status code of the response is in the success range (200-299).

- `assert.status-code-is-client-error`
  - description: Checks if the status code of the response is in the client error range (400-499).

- `assert.status-code-is-server-error`
  - description: Checks if the status code of the response is in the server error range (500-599).

#### Assertions - headers
- `assert.header-exists`
  - description: Checks if the specified header exists in the response.
  - arguments:
    - headerName: The name of the header to check for.

- `assert.header-value-equals`
  - description: Checks if the value of the specified header in the response matches the expected value.
  - arguments:
    - headerName: The name of the header to check.
    - expectedValue: The expected value of the header.

#### Assertions - content
- `assert.content-type-is-json`
  - description: Checks if the "Content-Type" header value is "application/json" and the body is valid JSON.

- `assert.content-type-is-xml`
  - description: Checks if the "Content-Type" header value is "application/xml" and the body is valid XML.

- `assert.content-type-is-html`
  - description: Checks if the "Content-Type" header value is "text/html".

#### Assertions - cookies
- `assert.cookie-exists`
  - description: Checks if the specified cookie exists in the response.
  - arguments:
    - cookieName: The name of the cookie to check for existence.

- `assert.cookie-value-equals`
  - description: Checks if the value of the specified cookie in the response equals the expected value.
  - arguments:
    - cookieName: The name of the cookie to check.
    - expectedValue: The expected value of the cookie.

- `assert.cookie-is-secured`
  - description: Checks if the specified cookie in the response is secured.
  - arguments:
    - cookieName: The name of the cookie to check.

- `assert.cookie-is-not-secured`
  - description: Checks if the specified cookie in the response is not secured.
  - arguments:
    - cookieName: The name of the cookie to check.

#### Assertions - body
- `assert.body-equals`
  - description: Checks if the body of the response is equal to the expected body.
  - arguments:
    - expectedBody: The expected body content.

- `assert.body-contains-text`
  - description: Checks if the body of the response contains the expected content.
  - arguments:
    - text: The expected text present in the response body.

- `assert.body-is-empty`
  - description: Checks if the body of the response is empty.

- `assert.body-is-not-empty`
  - description: Checks if the body of the response is not empty.

- `assert.body-length-equals`
  - description: Checks if the length of the response body is equal to the length of the expected body.
  - arguments:
    - length: The expected body length.

- `assert.body-starts-with`
  - description: Checks if the body of the response starts with the expected prefix.
  - arguments:
    - prefix: The expected prefix of the response body.

- `assert.body-ends-with`
  - description: Checks if the body of the response ends with the expected suffix.
  - arguments:
    - suffix: The expected suffix of the response body.

- `assert.body-matches-regex`
  - description: Checks if the body of the response matches the provided regex pattern.
  - arguments:
    - regexPattern: The regex pattern to match against the response body.
  - Note: Logic using the [Regex](https://www.scala-lang.org/api/2.13.x/scala/util/matching/Regex.html) scala implementation.
  - Regex examples:
```
    [
        {
            "id": 1,
            "name": "radiology"
        },
        {
            "id": 2,
            "name": "surgery"
        },
        {
            "id": 3,
            "name": "dentistry"
        }
    ]

"name": "radiology"                       ....    Matches the exact string "name": "radiology".
"name": "Radiology"                       ....    Matches the exact string "name": "Radiology" (case-sensitive). Fails on example!
sur\*ery                                  ....    Matches the string "sur*ery" where the asterisk is a literal character.
"id": \d                                  ....    Matches the string "id": followed by a single digit.
"name": ".*y"                             ....    Matches the string "name": followed by any sequence of characters ending with a 'y' and a closing double quote.
\bsurgery\b                               ....    Matches the standalone word "surgery".
\{\[,:\]\}                                ....    Matches the sequence of special JSON characters {[,:]}.
"emptyObject": \{\}                       ....    Matches the string "emptyObject": {}.
"key": null                               ....    Matches the string "key": null.
こんにちは                                  ....    Matches the Unicode string "こんにちは".
"level3": "value"                         ....    Matches the string "level3": "value".
"key": "value"                            ....    Matches the string "key": "value".
line break:\\nAnd a tab:\\tEnd            ....    Matches the string "line break:\nAnd a tab:\tEnd".
"boolean": true                           ....    Matches the string "boolean": true.
\^\$\.\*\+\?\(\)\[\]\{\}\|                ....    Matches the sequence of regex meta characters ^$.*+?()[]{}|.
```

#### Assertions - body - json
- `assert.body-json-is-json-array`
  - description: Checks if the body of the response is a JSON array.

- `assert.body-json-is-json-object`
  - description: Checks if the body of the response is a JSON object.

- `assert.body-json-path-exists`
  - description: Checks if the specified JSON path exists in the response body.
  - arguments:
    - jsonPath: The JSON path to check for existence.

#### Logging
- `log.error`
  - description: Logs a message at the ERROR level.
  - arguments:
    - message: The message to be logged.

- `log.warn`
  - description: Logs a message at the WARN level.
  - arguments:
    - message: The message to be logged.

- `log.info`
  - description: Logs a message at the INFO level.
  - arguments:
    - message: The message to be logged.

- `log.debug`
  - description: Logs a message at the DEBUG level.
  - arguments:
    - message: The message to be logged.

#### Extract from JSON
- `extractJson.string-from-list`
  - description: Extracts a string from a JSON array response at a given index and stores it in a runtime cache with a given key and expiration level.
  - arguments:
    - cacheKey: The key to use when storing the extracted string in the runtime cache.
    - listIndex: The index in the JSON array from which to extract the string. [0+]
    - jsonKey: The key in the JSON object from which to extract the string.
    - runtimeCacheLevel: The expiration level to use when storing the extracted string in the runtime cache. [Global, Suite, Test]

- `extractJson.string-from-json-path`
  - description: Extracts a string from a JSON response at a given json path and stores it in a runtime cache with a given key and expiration level.
  - arguments:
    - cacheKey: The key to use when storing the extracted string in the runtime cache.
    - jsonPath: The json path in the JSON from which to extract the string.
    - runtimeCacheLevel: The expiration level to use when storing the extracted string in the runtime cache. [Global, Suite, Test]


## Known issue
- Error in Response.perform.
  - Missing logic for retrieve of data from cache.
  - `{{ cache.neme }}` is not replaced by value from cache.
- Login in debug regime even if debug is not enabled.
- Missing several useful methods for building api tests.