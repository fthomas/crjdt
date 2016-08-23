name := "json-crdt"

scalaVersion := "2.11.8"

initialCommands += """
  import syntax._
"""

reformatOnCompileSettings

val validateCommands = Seq(
  "clean",
  "scalafmtTest",
  "compile",
  "test",
  "doc"
)
addCommandAlias("validate", validateCommands.mkString(";", ";", ""))
