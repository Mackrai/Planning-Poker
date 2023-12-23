package io.ppoker.configs

import zio.{Config, ZIO, ZLayer}
import zio.config.magnolia.deriveConfig

case class AppConfig(host: String,
                     port: Int)

object AppConfig {
  private val config: Config[AppConfig] =
    deriveConfig[AppConfig].nested("AppConfig")

  val live: ZLayer[Any, Config.Error, AppConfig] =
    ZLayer.fromZIO(
      ZIO.config[AppConfig](config).map { c =>
        AppConfig(host = c.host, port = c.port)
      }
    )
}
