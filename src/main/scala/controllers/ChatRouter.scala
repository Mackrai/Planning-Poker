package controllers

import cats.effect.ConcurrentEffect
import fs2.concurrent.{Queue, Topic}
import fs2.{Pipe, Stream}
import models.{Disconnect, InputMessage, OutputMessage}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}
import org.typelevel.log4cats.Logger

class ChatRouter[F[_]: ConcurrentEffect: Logger](
    queue: Queue[F, InputMessage],
    topic: Topic[F, OutputMessage]
) extends Router[F]
    with Http4sDsl[F] {

  // Хз как сделать это через tapir
  val wsRoute: HttpRoutes[F] =
    HttpRoutes.of[F] { case GET -> Root / "ws" =>
      val toClient: Stream[F, WebSocketFrame.Text] =
        topic.subscribe(1000).map(msg => Text(msg.stringify))

      val fromClientPipe: Pipe[F, WebSocketFrame, Unit] = { fromClient =>
        val parsedInput: Stream[F, InputMessage] = fromClient.collect {
          case Text(text, _) => InputMessage.parse(text)
          case Close(_)      => Disconnect()
        }

        // Вставляем в очередь запарсенное сообщение
        parsedInput.evalTap(msg => Logger[F].info(msg.toString)).through(queue.enqueue)
      }

      WebSocketBuilder[F].build(toClient, fromClientPipe)
    }

  override val endpoints = List()

}
