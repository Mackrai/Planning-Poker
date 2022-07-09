package controllers

import cats.effect.Async
import cats.{Functor, Monad}
import cats.effect.std.Queue
import fs2.concurrent.Topic
import fs2.{Pipe, Stream}
import models.{Disconnect, InputMessage, OutputMessage}
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
import org.typelevel.log4cats.Logger
import sttp.capabilities
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import sttp.ws.WebSocketFrame
import sttp.ws.WebSocketFrame.{Close, Text}
import sttp.model.StatusCode
import sttp.tapir.server.ServerEndpoint
import util.Endpoints


class ChatRouter[F[_]: Async: Logger: Functor: Monad](
    queue: Queue[F, InputMessage],
    topic: Topic[F, OutputMessage],
    appState: Ref[F, AppState]
) extends Router[F]
    with Http4sDsl[F] {

  // Пример с эндпоинтом через тапир
  // https://github.com/softwaremill/tapir/blob/master/examples/src/main/scala/sttp/tapir/examples/WebSocketHttp4sServer.scala
  val inputToOutput: Pipe[F,  WebSocketFrame, WebSocketFrame] = { in =>
    topic.subscribe(1000).map(msg => Text(msg.stringify, finalFragment = false, Some(1))) // скорее всего это как-то по другому должно использоваться здесь

    val parsedInput: Stream[F, InputMessage] = in.collect {
      case Text(text, _, _) => InputMessage.parse(text)
      case Close(_, _)      => Disconnect()
    }
    // Вставляем в очередь запарсенное сообщение
    parsedInput.evalTap(msg => Logger[F].info(msg.toString)).foreach(queue.offer)
  }
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

  private val end1: ServerEndpoint[Fs2Streams[F] with capabilities.WebSockets, F] =
    Endpoints.wsEndpoint[F].serverLogicSuccess[F](_ => Async[F].pure(inputToOutput))

  override val endpoints = List(end1)
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
