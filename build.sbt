/// variables

val groupId = "eu.timepit"
val projectName = "crjdt"
val rootPkg = s"$groupId.$projectName"
val gitHubOwner = "fthomas"
val gitPubUrl = s"https://github.com/$gitHubOwner/$projectName.git"
val gitDevUrl = s"git@github.com:$gitHubOwner/$projectName.git"
val modulesDir = "modules"

val catsVersion = "0.8.1"
val circeVersion = "0.6.1"
val scalaCheckVersion = "1.13.4"

val allSubprojects = Seq("core", "circe")
val allSubprojectsJVM = allSubprojects.map(_ + "JVM")
val allSubprojectsJS = allSubprojects.map(_ + "JS")

/// projects

lazy val root = project
  .in(file("."))
  .aggregate(coreJVM, coreJS, circeJVM, circeJS, docs)
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
  .dependsOn(core)
  .settings(moduleName := s"$projectName-circe")
  .settings(commonSettings: _*)
  .settings(publishSettings: _*)
  .jsSettings(commonJsSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % circeVersion
    )
  )

lazy val circeJVM = circe.jvm
lazy val circeJS = circe.js

lazy val docs = project
  .in(file(s"$modulesDir/docs"))
  .enablePlugins(MicrositesPlugin)
  .settings(commonSettings)
  .settings(noPublishSettings)
  .settings(micrositeSettings)
  .settings(
    Def.settings(
      unidocSettings,
      UnidocKeys.unidocProjectFilter in (ScalaUnidoc, UnidocKeys.unidoc) :=
        inAnyProject -- inProjects(
          allSubprojectsJS.map(LocalProject.apply): _*)
    )
  )

/// settings

lazy val commonSettings = Def.settings(
  metadataSettings,
  compileSettings,
  scaladocSettings,
  releaseSettings,
  styleSettings,
  miscSettings
)

lazy val commonJsSettings = Def.settings()

lazy val metadataSettings = Def.settings(
  name := projectName,
  description := "A conflict-free replicated JSON datatype (CRDT) in Scala",
  organization := groupId,
  homepage := Some(url(s"https://github.com/$gitHubOwner/$projectName")),
  startYear := Some(2016),
  licenses := Seq(
    "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
  scmInfo := Some(
    ScmInfo(homepage.value.get,
            s"scm:git:$gitPubUrl",
            Some(s"scm:git:$gitDevUrl"))),
  developers := List(
    Developer(id = "fthomas",
              name = "Frank S. Thomas",
              email = "",
              url("https://github.com/fthomas"))),
  bintrayPackageLabels := Seq("JSON", "CRDT", "Scala")
)

lazy val compileSettings = Def.settings(
  scalaVersion := "2.12.0",
  crossScalaVersions := Seq(scalaVersion.value, "2.11.8"),
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
  autoAPIMappings := true,
  apiURL := Some(
    url(s"http://$gitHubOwner.github.io/$projectName/latest/api/"))
)

lazy val publishSettings = Def.settings(
  publishMavenStyle := true,
  pomIncludeRepository := { _ =>
    false
  }
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
    val oldVersion = extracted.get(latestVersion)

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
      releaseStepTask(publishMicrosite in "docs"),
      setLatestVersion,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  )
}

lazy val styleSettings = Def.settings(
  reformatOnCompileSettings,
  scalafmtConfig := Some(file(".scalafmt.conf"))
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

lazy val micrositeSettings = Def.settings(
  micrositeName := projectName,
  micrositeBaseUrl := projectName,
  micrositeDocumentationUrl := "latest/api",
  micrositeGithubOwner := gitHubOwner,
  micrositeGithubRepo := projectName,
  micrositeExtraMdFiles := Map(
    file("README.md") -> microsites.ExtraMdFileConfig("index.md", "home")),
  organizationName := "Frank S. Thomas",
  addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc),
                       micrositeDocumentationUrl)
)

/// commands

def addCommandsAlias(name: String, cmds: Seq[String]) =
  addCommandAlias(name, cmds.mkString(";", ";", ""))

addCommandsAlias("testJS", allSubprojectsJS.map(_ + "/test"))
addCommandsAlias("testJVM", allSubprojectsJVM.map(_ + "/test"))

addCommandsAlias("validate",
                 Seq(
                   "clean",
                   "scalafmtTest",
                   "test:scalafmtTest",
                   "testJS",
                   "coverage",
                   "testJVM",
                   "coverageReport",
                   "coverageOff",
                   "unidoc"
                 ))

addCommandsAlias("syncMavenCentral",
                 allSubprojectsJVM.map(_ + "/bintraySyncMavenCentral"))
