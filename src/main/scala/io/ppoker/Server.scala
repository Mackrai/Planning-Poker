package io.ppoker

import com.comcast.ip4s.{Host, Port}
import io.ppoker.configs.AppConfig
import io.ppoker.controllers.ChatRouter
import io.ppoker.core.{AppState, HubManager, InputMessage, MessageProcessor, OutputMessage}
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.websocket.WebSocketBuilder2
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import zio._
import zio.interop.catz.asyncInstance

object Server {
  // private val routes = ???

  private def wsRoutes(chatRouter: ChatRouter): WebSocketBuilder2[AppRIO] => HttpRoutes[AppRIO] =
    ZHttp4sServerInterpreter().fromWebSocket(chatRouter.ws).toRoutes

  def run(appConfig: AppConfig, appState: Ref[AppState]) =
    for {
      host <- ZIO.from(Host.fromString(appConfig.host))
      port <- ZIO.from(Port.fromInt(appConfig.port))

      hubManager <- ZIO.service[HubManager]
      messageProcessor <- ZIO.service[MessageProcessor]
      chatRouter = new ChatRouter(hubManager, messageProcessor)

      _    <-
        EmberServerBuilder
          .default[AppRIO]
          .withHost(host)
          .withPort(port)
//          .withHttpApp(Router("/" -> routes).orNotFound)
          .withHttpWebSocketApp(wsb => Router("/" -> wsRoutes(chatRouter)(wsb)).orNotFound)
          .build
          .useForever
    } yield ()

}
