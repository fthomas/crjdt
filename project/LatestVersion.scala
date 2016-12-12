import sbt.{AutoPlugin, SettingKey, settingKey}

object LatestVersion extends AutoPlugin {
  object autoImport {
    lazy val latestVersion: SettingKey[String] =
      settingKey[String]("latest released version")
  }
}
