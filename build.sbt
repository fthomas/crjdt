name := "crjdt"
licenses := Seq(
  "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))

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

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "0.7.0",
  "org.scalacheck" %% "scalacheck" % "1.13.2" % "test"
)

initialCommands += """
  import eu.timepit.crjdt._
  import eu.timepit.crjdt.syntax._
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
