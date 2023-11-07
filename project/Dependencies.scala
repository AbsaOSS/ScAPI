/*
 * Copyright 2023 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._

object Dependencies {

  private val scribeVersion = "2.8.6"
  private val munitVersion = "1.0.0-M7"
  private val scoptVersion = "4.1.0"
  private val sprayJsonVersion = "1.3.6"
  private val jsonSchemaValidatorVersion = "1.0.83"
  private val absaCommonsVersion = "1.3.4"
  private val commonsIoVersion = "2.13.0"
  private val requestsVersion = "0.8.0"
  private val loggerVersion = "2.14.1"
  private val scalaXmlVersion = "1.3.0"
  private val jsonPathVersion = "2.8.0"

  def getScalaDependency(scalaVersion: String): ModuleID = "org.scala-lang" % "scala-library" % scalaVersion

  val BaseDependencies: Seq[ModuleID] = Seq(
    "com.github.scopt"          %% "scopt"                  % scoptVersion,
    "com.outr"                  %% "scribe"                 % scribeVersion,
    "io.spray"                  %% "spray-json"             % sprayJsonVersion,
    "com.networknt"             %  "json-schema-validator"  % jsonSchemaValidatorVersion,
    "za.co.absa.commons"        %% "commons"                % absaCommonsVersion,
    "commons-io"                %  "commons-io"             % commonsIoVersion,
    // INFO: to used this library no imports needed. code is in root of src dir
    "com.lihaoyi"               %% "requests"               % requestsVersion,
    "org.apache.logging.log4j"  % "log4j-core"              % loggerVersion,
    "org.apache.logging.log4j"  % "log4j-api"               % loggerVersion,
    "org.scala-lang.modules"    %% "scala-xml"              % scalaXmlVersion,
    "com.jayway.jsonpath"       % "json-path"               % jsonPathVersion,

    // test
    "org.scalameta"             %% "munit"                  % munitVersion          % Test
  )
}
