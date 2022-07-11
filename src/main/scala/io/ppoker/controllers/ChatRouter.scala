package io.ppoker.controllers

import cats.effect.std.Queue
import cats.effect.{Async, Ref}
import cats.implicits._
import cats.{Functor, Monad}
import fs2.Pipe
import fs2.concurrent.Topic
import io.ppoker.api._
import io.ppoker.core.{AppState, Disconnect, InputMessage, OutputMessage}
import io.ppoker.models.UserId
import io.ppoker.util.LoggerOps.LoggerOps
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}
import org.http4s.{HttpRoutes, Response}
import org.typelevel.log4cats.Logger
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

class ChatRouter[F[_]: Async: Logger: Functor: Monad](
    webSocketBuilder2: WebSocketBuilder2[F],
    queue: Queue[F, InputMessage],
    topic: Topic[F, OutputMessage],
    appState: Ref[F, AppState]
) extends Router[F]
    with Http4sDsl[F] {

  private def websocketLogic(user: UserId): F[Response[F]] = {
    val sentToClient: fs2.Stream[F, WebSocketFrame] =
      topic
        .subscribe(1000)
        .filter(_.forUser(user))
        .map(msg => Text(msg.stringify))

    val receiveFromClient: Pipe[F, WebSocketFrame, Unit] =
      _.collect {
        case Text(text, _) => InputMessage.parse(text)
        case Close(_)      => Disconnect(user)
      }
        .evalTap(Logger[F].logMessage)
        .evalMap(queue.offer)

    webSocketBuilder2.build(sentToClient, receiveFromClient)
  }

  private val websocket: HttpRoutes[F] =
    HttpRoutes.of[F] { case _ @GET -> Root / "ws" / (user: String) =>
      websocketLogic(UserId(user))
    }

  private val sessions: ServerEndpoint[Any, F] =
    Endpoints.sessions.serverLogic { _ =>
      appState.get.map { state =>
        ListSessionsResponse(state.sessions.toSeq.map(x => SessionIdsDTO(x._1.raw, x._2.map(_.raw).toSeq))).asRight
      }
    }

  private val endpoints = List(sessions)

  override val routes: HttpRoutes[F] =
    Http4sServerInterpreter[F]().toWebSocketRoutes(endpoints)(webSocketBuilder2) <+> websocket

}
