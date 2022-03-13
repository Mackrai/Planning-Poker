package controllers

import cats.effect.ConcurrentEffect
import cats.implicits._
import org.typelevel.log4cats.Logger
import services.SomeService
import sttp.model.StatusCode
import sttp.tapir.server.ServerEndpoint

class SomeRouter[F[_]: ConcurrentEffect: Logger](someService: SomeService[F]) extends Router[F] {

  private val hello: ServerEndpoint[Unit, (StatusCode, String), String, Any, F] =
    Endpoints.helloEndpoint.serverLogic { _ =>
      someService.hello.value <* Logger[F].info("hello")
    }

  private val error: ServerEndpoint[Unit, (StatusCode, String), Int, Any, F] =
    Endpoints.errorEndpoint.serverLogic(_ => someService.error.value)

  override val endpoints =
    List(
      hello,
      error
    )

}
