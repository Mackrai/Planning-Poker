package services

import cats.data.EitherT
import cats.effect._
import sttp.model.StatusCode

class SomeService[F[_]: Sync]() {

  def hello: EitherT[F, (StatusCode, String), String] =
    EitherT.fromOptionF(
      Sync[F].pure(Some("Hello").asInstanceOf[Option[String]]),
      (StatusCode.InternalServerError, "failure")
    )

  def error: EitherT[F, (StatusCode, String), Int] =
    EitherT.fromOptionF(
      Sync[F].pure(None.asInstanceOf[Option[Int]]),
      (StatusCode.InternalServerError, "failure")
    )

}
