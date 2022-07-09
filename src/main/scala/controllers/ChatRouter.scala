package controllers

import cats.effect.std.Queue
import cats.effect.{Async, Ref}
import cats.implicits._
import cats.{Functor, Monad}
import fs2.concurrent.Topic
import fs2.{Pipe, Stream}
import models._
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger
import sttp.capabilities
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import sttp.ws.WebSocketFrame
import sttp.ws.WebSocketFrame.{Close, Text}
import util.Endpoints

class ChatRouter[F[_]: Async: Logger: Functor: Monad](
    queue: Queue[F, InputMessage],
    topic: Topic[F, OutputMessage],
    appState: Ref[F, AppState]
) extends Router[F]
    with Http4sDsl[F] {

  // Пример с эндпоинтом через тапир
  /* https://github.com/softwaremill/tapir/blob/master/examples/src/main/scala/sttp/tapir/examples/WebSocketHttp4sServer.scala */
  val inputToOutput: Pipe[F, WebSocketFrame, WebSocketFrame] = { in =>
    // скорее всего это как-то по другому должно использоваться здесь
    topic.subscribe(1000).map(msg => Text(msg.stringify, finalFragment = false, Some(1)))

    val parsedInput: Stream[F, InputMessage] = in.collect {
      case Text(text, _, _) => InputMessage.parse(text)
      case Close(_, _)      => Disconnect()
    }
    // Вставляем в очередь запарсенное сообщение
    parsedInput.evalTap(msg => Logger[F].info(msg.toString)).foreach(queue.offer)
  }

//  val ws: HttpRoutes[F] =
//    HttpRoutes.of[F] { case GET -> Root / "ws" =>
//      val user           = User(name = "User-1", role = Role.Host)
//      val toClient       = topic.subscribe(1000).map(msg => Text(msg.stringify))
//      val fromClientPipe = { (fromClient: Stream[F, WebSocketFrame]) =>
//        val parsedInput: Stream[F, InputMessage] = fromClient.collect {
//          case Text(text, _, _) => InputMessage.parse(text)
//          case Close(_, _)      => Disconnect()
//        }
//        parsedInput.evalTap(msg => Logger[F].info(msg.toString)).through(queue.enqueue)
//      }
//    }

  private val websocket: ServerEndpoint[Fs2Streams[F] with capabilities.WebSockets, F] =
    Endpoints.wsEndpoint[F].serverLogicSuccess[F](_ => Async[F].pure(inputToOutput))

  private val sessions: ServerEndpoint[Any, F] =
    Endpoints.sessions.serverLogic { _ =>
      appState.get.map(state => api.ListSessionsResponse(state.sessions.keys.toSeq).asRight)
    }

  override val endpoints = List(websocket, sessions)

}
