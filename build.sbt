/// variables

val groupId = "eu.timepit"
val projectName = "crjdt"
val rootPkg = s"$groupId.$projectName"
val gitPubUrl = s"https://github.com/fthomas/$projectName.git"
val gitDevUrl = s"git@github.com:fthomas/$projectName.git"
val modulesDir = "modules"

val catsVersion = "0.7.2"
val circeVersion = "0.5.1"
val scalaCheckVersion = "1.12.5"

/// projects

lazy val root = project
  .in(file("."))
  .aggregate(coreJVM, coreJS, circeJVM, circeJS)
  .settings(commonSettings)
  .settings(noPublishSettings)
  .settings(
    console := console.in(coreJVM, Compile).value,
    console.in(Test) := console.in(coreJVM, Test).value
  )

lazy val core = crossProject
  .crossType(CrossType.Pure)
  .in(file(s"$modulesDir/core"))
  .settings(moduleName := s"$projectName-core")
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .jsSettings(commonJsSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % catsVersion,
      "org.typelevel" %%% "cats-laws" % catsVersion % "test",
      "org.scalacheck" %%% "scalacheck" % scalaCheckVersion % "test"
    )
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val circe = crossProject
  .crossType(CrossType.Pure)
  .in(file(s"$modulesDir/circe"))
  .settings(moduleName := s"$projectName-circe")
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .jsSettings(commonJsSettings: _*)
  .dependsOn(core)
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % circeVersion
    )
  )

lazy val circeJVM = circe.jvm
lazy val circeJS = circe.js

/// settings

lazy val commonSettings = Def.settings(
  metadataSettings,
  compileSettings,
  scaladocSettings,
  releaseSettings,
  styleSettings,
  miscSettings
)

lazy val commonJsSettings = Def.settings(
  scalaJSUseRhino in Global := false
)

lazy val metadataSettings = Def.settings(
  name := projectName,
  description := "A conflict-free replicated JSON datatype (CRDT) in Scala",
  organization := groupId,
  homepage := Some(url(s"https://github.com/fthomas/$projectName")),
  startYear := Some(2016),
  licenses := Seq(
    "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
  scmInfo := Some(
    ScmInfo(homepage.value.get,
            s"scm:git:$gitPubUrl",
            Some(s"scm:git:$gitDevUrl"))),
  bintrayPackageLabels := Seq("JSON", "CRDT", "Scala")
)

lazy val compileSettings = Def.settings(
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:experimental.macros",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xfuture",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused-import",
    "-Ywarn-value-discard"
  ),
  scalacOptions in (Compile, console) -= "-Ywarn-unused-import",
  scalacOptions in (Test, console) -= "-Ywarn-unused-import"
)

lazy val scaladocSettings = Def.settings(
  scalacOptions in (Compile, doc) ++= Seq(
    "-doc-source-url",
    scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala",
    "-sourcepath",
    baseDirectory.in(LocalRootProject).value.getAbsolutePath
  ),
  autoAPIMappings := true
)

lazy val publishSettings = Def.settings(
  publishMavenStyle := true,
  pomIncludeRepository := { _ =>
    false
  },
  pomExtra :=
    <developers>
      <developer>
        <id>fthomas</id>
        <name>Frank S. Thomas</name>
        <url>https://github.com/fthomas</url>
      </developer>
    </developers>
)

lazy val noPublishSettings = Def.settings(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val releaseSettings = {
  import sbtrelease.ReleaseStateTransformations._

  lazy val updateVersionInReadme: ReleaseStep = { st: State =>
    val extracted = Project.extract(st)
    val newVersion = extracted.get(version)
    val oldVersion = "git describe --abbrev=0".!!.trim.replaceAll("^v", "")

    val readme = "README.md"
    val oldContent = IO.read(file(readme))
    val newContent = oldContent.replaceAll(oldVersion, newVersion)
    IO.write(file(readme), newContent)
    s"git add $readme" !! st.log

    st
  }

  Def.settings(
    releaseCrossBuild := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      updateVersionInReadme,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )
}

lazy val styleSettings = Def.settings(
  reformatOnCompileSettings
)

lazy val miscSettings = Def.settings(
  initialCommands += s"""
    import $rootPkg.core._
    import $rootPkg.core.syntax._
  """,
  initialCommands in Test += s"""
    import $rootPkg.core.arbitrary._
    import org.scalacheck.Arbitrary
  """
)

/// commands

val validateCommands = Seq(
  "clean",
  "scalafmtTest",
  "coreJS/test",
  "coverage",
  "coreJVM/test",
  "coverageReport",
  "coverageOff",
  "doc"
)
addCommandAlias("validate", validateCommands.mkString(";", ";", ""))
