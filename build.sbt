val groupId = "eu.timepit"
val projectName = "crjdt"
val rootPkg = s"$groupId.$projectName"
val gitPubUrl = s"https://github.com/fthomas/$projectName.git"
val gitDevUrl = s"git@github.com:fthomas/$projectName.git"

name := projectName
description := ""

organization := groupId
homepage := Some(url(s"https://github.com/fthomas/$projectName"))
startYear := Some(2016)
licenses := Seq(
  "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))
scmInfo := Some(
  ScmInfo(homepage.value.get,
          s"scm:git:$gitPubUrl",
          Some(s"scm:git:$gitDevUrl")))

scalaVersion := "2.11.8"
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
)
scalacOptions in (Compile, console) -= "-Ywarn-unused-import"
scalacOptions in (Test, console) -= "-Ywarn-unused-import"

val catsVersion = "0.7.0"
val scalaCheckVersion = "1.12.5"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-laws" % catsVersion % "test",
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test"
)

initialCommands += s"""
  import $rootPkg._
  import $rootPkg.syntax._
"""

reformatOnCompileSettings

val validateCommands = Seq(
  "clean",
  "scalafmtTest",
  "coverage",
  "compile",
  "test",
  "coverageReport",
  "coverageOff",
  "doc"
)
addCommandAlias("validate", validateCommands.mkString(";", ";", ""))
