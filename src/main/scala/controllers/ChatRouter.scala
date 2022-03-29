package controllers

import cats.effect.Async
import cats.{Functor, Monad}
import cats.effect.std.Queue
import fs2.concurrent.Topic
import fs2.{Pipe, Stream}
import models.{Disconnect, InputMessage, OutputMessage}
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger
import sttp.capabilities
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import sttp.ws.WebSocketFrame
import sttp.ws.WebSocketFrame.{Close, Text}


class ChatRouter[F[_]: Async: Logger: Functor: Monad](
    queue: Queue[F, InputMessage],
    topic: Topic[F, OutputMessage]
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

  private val end1: ServerEndpoint[Fs2Streams[F] with capabilities.WebSockets, F] =
    Endpoints.wsEndpoint[F].serverLogicSuccess[F](_ => Async[F].pure(inputToOutput))

  override val endpoints = List(end1)

}
