package io.ppoker.configs

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class AppConfig(server: ServerConfig,
                     db: DBConfig)

object AppConfig {
  implicit val reader: ConfigReader[AppConfig] = deriveReader
}