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

ThisBuild / organization := "za.co.absa"

lazy val scala213 = "2.13.10"

ThisBuild / scalaVersion := scala213

// Hint: helps to keep future Scala 3 compatibility
ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature")

Global / concurrentRestrictions += Tags.limit(Tags.Test, 1)

import Dependencies._
import com.github.sbt.jacoco.report.JacocoReportSettings

val mergeStrategy: Def.SettingsDefinition = assembly / assemblyMergeStrategy := {
  case PathList("META-INF", _) => MergeStrategy.discard
  case _                       => MergeStrategy.first
}

// MUnit does not support running individual test cases in parallel.
// Set true to allow parallel run across suites.
Test / parallelExecution := false
Test / fork := true

val MUnitFramework = new TestFramework("munit.Framework")

lazy val commonJacocoReportSettings: JacocoReportSettings = JacocoReportSettings(
  formats = Seq(JacocoReportFormats.HTML, JacocoReportFormats.XML)
)

lazy val commonJacocoExcludes: Seq[String] = Seq(
  "africa.absa.testing.scapi.logging.*"
)

lazy val scAPI = (project in file("."))
  .settings(
    name := "ScAPI",
    // No need to publish the aggregation [empty] artifact
    publishArtifact := false,
    publish := {},
    publishLocal := {}
  )
  .aggregate(testApi)

lazy val testApi = project
  .settings(
    name := "testApi",
    libraryDependencies ++= BaseDependencies :+ getScalaDependency(scalaVersion.value) ,
    assembly / mainClass := Some("africa.absa.testing.scapi.ScAPIRunnerJob"),
    assembly / test := {},
    scalacOptions += "-Yrangepos",
    testFrameworks += MUnitFramework,
    mergeStrategy,
    Compile / assembly / artifact := {
      val art = (Compile / assembly / artifact).value
      art.withClassifier(Some("assembly"))
    },
    addArtifact(Compile / assembly / artifact, assembly)
  ).settings(
    jacocoReportSettings := commonJacocoReportSettings.withTitle(s"ScAPI - scala:${scalaVersion.value}"),
    jacocoExcludes := commonJacocoExcludes
  )
  .enablePlugins(AutomateHeaderPlugin)


