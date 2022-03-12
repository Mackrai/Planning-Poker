package services

import cats.Functor
import cats.effect._
import fs2.concurrent.{Queue, Topic}

class ChatService[F[_]: Sync: Functor](
    queue: Queue[F, Unit],
    topic: Topic[F, Unit]
) {}
