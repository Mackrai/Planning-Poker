package io.ppoker

import io.ppoker.configs.AppConfig
import io.ppoker.core.{AppState, HubManager, MessageProcessor}
import zio._
import zio.config.typesafe.TypesafeConfigProvider
import zio.logging.LogFormat
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.setConfigProvider(TypesafeConfigProvider.fromResourcePath()) ++
      Runtime.removeDefaultLoggers ++
      SLF4J.slf4j(LogLevel.Info, LogFormat.colored)

  private val app =
    for {
      appConfig <- ZIO.service[AppConfig]
      appState <- Ref.make(AppState.singleSession)

      _ <- Server.run(appConfig, appState)
    } yield ()

  override def run: URIO[Any, ExitCode] =
    app
      .provide(
        AppConfig.live,
        HubManager.live,
        MessageProcessor.live
      )
      .exitCode

}
