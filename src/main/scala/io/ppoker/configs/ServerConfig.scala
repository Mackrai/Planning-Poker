package io.ppoker.configs

import pureconfig._
import pureconfig.generic.semiauto.deriveReader

case class ServerConfig(
    host: String,
    port: Int
)

object ServerConfig {
  implicit val reader: ConfigReader[ServerConfig] = deriveReader
}
