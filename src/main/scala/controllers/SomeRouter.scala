package controllers

import cats.implicits._
import org.typelevel.log4cats.Logger
import services.SomeService
import sttp.tapir.server.ServerEndpoint
import cats.effect.Async
import sttp.capabilities.fs2.Fs2Streams

class SomeRouter[F[_]: Async: Logger](someService: SomeService[F]) extends Router[F] {

  private val hello: ServerEndpoint[Fs2Streams[F], F] =
    Endpoints.helloEndpoint.serverLogic { _ =>
      someService.hello.value <* Logger[F].info("hello")
    }

  private val error: ServerEndpoint[Fs2Streams[F], F] =
    Endpoints.errorEndpoint.serverLogic(_ => someService.error.value)

  override val endpoints =
    List(
      hello,
      error
    )

}
