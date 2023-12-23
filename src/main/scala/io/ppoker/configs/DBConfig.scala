package io.ppoker.configs

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class DBConfig(url: String,
                    driver: String,
                    host: String,
                    port: Int,
                    user: String,
                    pass: String)

object DBConfig {
  implicit val reader: ConfigReader[DBConfig] = deriveReader
}