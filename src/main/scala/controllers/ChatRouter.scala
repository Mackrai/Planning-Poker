package controllers

import api.{CreateChatRequest, ListSessionsResponse, SessionResponse}
import cats.Applicative
import cats.effect.ConcurrentEffect
import cats.effect.concurrent.Ref
import cats.implicits._
import fs2.Stream
import fs2.concurrent.{Queue, Topic}
import models._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}
import org.typelevel.log4cats.Logger
import sttp.model.StatusCode
import sttp.tapir.server.ServerEndpoint
import util.Endpoints

class ChatRouter[F[_]: ConcurrentEffect: Logger: Applicative](
    queue: Queue[F, InputMessage],
    topic: Topic[F, OutputMessage],
    appState: Ref[F, AppState]
) extends Router[F]
    with Http4sDsl[F] {

  val ws: HttpRoutes[F] =
    HttpRoutes.of[F] { case GET -> Root / "ws" =>
      val user           = User(name = "User-1", role = Role.Host)
      val toClient       = topic.subscribe(1000).map(msg => Text(msg.stringify))
      val fromClientPipe = { (fromClient: Stream[F, WebSocketFrame]) =>
        val parsedInput: Stream[F, InputMessage] = fromClient.collect {
          case Text(text, _) => InputMessage.parse(text)
          case Close(_)      => Disconnect()
        }
        parsedInput.evalTap(msg => Logger[F].info(msg.toString)).through(queue.enqueue)
      }

      WebSocketBuilder[F].build(toClient, fromClientPipe)
    }

  val sessions: ServerEndpoint[Unit, (StatusCode, String), ListSessionsResponse, Any, F] =
    Endpoints.sessions
      .serverLogic(_ =>
        appState.get.map(state =>
          api
            .ListSessionsResponse(state.sessions.toSeq.map { case (sessionId, _) =>
              SessionResponse(sessionId)
            })
            .asRight
        )
      )

  val createChat: ServerEndpoint[CreateChatRequest, (StatusCode, String), Unit, Any, F] =
    Endpoints.createChat
      .serverLogic { req =>
        val newSession = Session[F](title = req.title)
        appState
          .update(state => AppState(state.sessions + (newSession.uuid.toString -> newSession)))
          .map(u => Either.right(u))
      }

  val joinChat =
    Endpoints.joinChat
      .serverLogic { req =>
        req.chatId
        req.userId
      }

  override val endpoints = List(sessions, createChat)

}
