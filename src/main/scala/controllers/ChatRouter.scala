package controllers

import cats.effect.ConcurrentEffect
import fs2.concurrent.{Queue, Topic}
import fs2.{Pipe, Stream}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Text
import org.typelevel.log4cats.Logger

class ChatRouter[F[_]: ConcurrentEffect: Logger](
    queue: Queue[F, String],
    topic: Topic[F, String]
) extends Router[F]
    with Http4sDsl[F] {

  // Хз как сделать это через tapir
  val wsRoute: HttpRoutes[F] =
    HttpRoutes.of[F] { case GET -> Root / "chat" =>
      val toClient: Stream[F, WebSocketFrame.Text] =
        topic.subscribe(1000).map(msg => Text(msg))

      val fromClientPipe: Pipe[F, WebSocketFrame, Unit] = { fromClient =>
        val parsedInput = fromClient.collect {
          case Text(text, _) => text
          case _             => "???"
        }

        parsedInput.through(topic.publish)
//        parsedInput.through(queue.enqueue)
      }

      WebSocketBuilder[F].build(toClient, fromClientPipe)
    }

  override val endpoints = List()

}
