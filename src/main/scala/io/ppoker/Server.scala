package io.ppoker

import com.comcast.ip4s.{Host, Port}
import io.ppoker.configs.AppConfig
import io.ppoker.controllers.ChatRouter
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.websocket.WebSocketBuilder2
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import zio._
import zio.interop.catz.asyncInstance

object Server {
  private def routes(chatRouter: ChatRouter): WebSocketBuilder2[AppRIO] => HttpRoutes[AppRIO] =
    ZHttp4sServerInterpreter().fromWebSocket(chatRouter.endpoints).toRoutes

  def run(appConfig: AppConfig) =
    for {
      host       <- ZIO.from(Host.fromString(appConfig.host))
      port       <- ZIO.from(Port.fromInt(appConfig.port))
      chatRouter <- ZIO.service[ChatRouter]

      _ <-
        EmberServerBuilder
          .default[AppRIO]
          .withHost(host)
          .withPort(port)
          .withHttpWebSocketApp(wsb => Router("/" -> routes(chatRouter)(wsb)).orNotFound)
          .build
          .useForever
    } yield ()

}
