package services

import cats.effect._

object SomeService {
    def foo[F[_]: Sync]: F[Unit] = Sync[F].pure(println("123"))
}
