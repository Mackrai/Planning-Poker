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
import org.typelevel.log4cats.Logger
import sttp.capabilities
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import sttp.ws.WebSocketFrame
import sttp.ws.WebSocketFrame.{Close, Text}

class ChatRouter[F[_]: Async: Logger: Functor: Monad](
    queue: Queue[F, InputMessage],
    topic: Topic[F, OutputMessage],
    appState: Ref[F, AppState]
) extends Router[F]
    with Http4sDsl[F] {

  private def processIncomingData(user: String): Pipe[F, WebSocketFrame, WebSocketFrame] = {
    val toClient =
      topic
        .subscribe(1000)
        .filter(_.forUser(UserId(user)))
        .map(msg => Text(msg.stringify, finalFragment = true, None))

    _.collect {
      case Text(text, _, _) => InputMessage.parse(text)
      case Close(_, _)      => Disconnect()
    }
      .evalTap(Logger[F].logMessage)
      .evalTap(queue.offer)
      .flatMap(_ => toClient)
  }

  private val websocket: ServerEndpoint[Fs2Streams[F] with capabilities.WebSockets, F] =
    Endpoints.wsEndpoint[F].serverLogicSuccess[F](user => Async[F].pure(processIncomingData(user)))

  private val sessions: ServerEndpoint[Any, F] =
    Endpoints.sessions.serverLogic { _ =>
      appState.get.map { state =>
        ListSessionsResponse(state.sessions.toSeq.map(x => SessionIdsDTO(x._1.raw, x._2.map(_.raw).toSeq))).asRight
      }
    }

  override val endpoints = List(websocket, sessions)

}