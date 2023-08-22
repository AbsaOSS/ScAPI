# ScAPI

### How to run api tests
TODO - work in progress

### How to run unit tests
Run unit tests from path `{project-root}`
```
sbt test
```

### How to generate JaCoCo code coverage report
Run command from path `{project-root}`
```
sbt jacoco
```
Report should be available on path `{project-root}/testApi/target/scala-2.13/jacoco/report/html`

### How to generate jars 
Run command from path `{project-root}`
```
sbt assembly
```
Jar files should be available on path `{project-root}/testApi/target/scala-2.13`

### How to run tests from jar file
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

### How to create env.json file
#### Example of **env.json** file:
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

#### Rules to follow:
- **Constants** and **Properties** can contain only `String` type of values.
- **Constants** cannot contain reference `{{ key }}`.
- **Properties** can reference from **Constants** only.
- Multilevel reference `{{key_{{key_part_2}}}}` are not supported.
- Json elements follows `camelCase`.
- Json element methods follows `this-case`. 

### Supported options
#### Headers
- methods
  - "content-type"
  - "authorization"
#### Action
- methods
    - get | post | put | delete
  - arguments:
    - url: url string
    - body: json string (can be null)
    - params: list of url parameters in format [name, value] (can be null) 
#### Assertions
- Assert group
  - "status-code"
    - arguments:
      - param_1: status code string
  - "body-contains"
    - arguments:
      - param_1: string to find in body
- Log group
  - "info"
    - arguments:
      - param_1: string to log with info level
- Extract Json group
  - "string_from_list"
    - arguments:
      - param_1: RuntimeCache key string 
      - param_2: json array index [0+]
      - param_3: json element string to find
      - param_4: RuntimeCache expiration level [Global, Suite, Test]

