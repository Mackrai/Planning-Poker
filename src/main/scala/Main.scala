import cats.effect._
import configs.ServerConfig
import org.http4s._
import org.http4s.blaze.server._
import org.http4s.implicits._
import org.http4s.server.Server
import pureconfig.ConfigSource
import services.SomeService
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    startServer[IO].use(_ => IO.never)

  def startServer[F[_]: ConcurrentEffect: Timer: ContextShift]: Resource[F, Server] =
    for {
      conf   <- Resource.eval(ConcurrentEffect[F].pure(ConfigSource.default.loadOrThrow[ServerConfig]))
      server <- BlazeServerBuilder
                  .apply(global)
                  .bindHttp(conf.port, conf.host)
                  .withHttpApp(app)
                  .resource
    } yield server

  def app[F[_]: ConcurrentEffect: Timer: ContextShift]: HttpApp[F] =
    Http4sServerInterpreter[F]().toRoutes(new SomeRouter[F](new SomeService[F]).endpoints).orNotFound

}

object Endpoints {

  val base: Endpoint[Unit, (StatusCode, String), Unit, Any] =
    endpoint.in("api").errorOut(statusCode).errorOut(stringBody)

  val helloEndpoint: Endpoint[Unit, (StatusCode, String), String, Any] =
    base.get
      .in("hello")
      .out(jsonBody[String])

  val errorEndpoint: Endpoint[Unit, (StatusCode, String), Int, Any] =
    base.get
      .in("error")
      .out(jsonBody[Int])

}

class SomeRouter[F[_]: ConcurrentEffect](someService: SomeService[F]) {

  val hello: ServerEndpoint[Unit, (StatusCode, String), String, Any, F] =
    Endpoints.helloEndpoint.serverLogic(_ => someService.hello.value)

  val error: ServerEndpoint[Unit, (StatusCode, String), Int, Any, F] =
    Endpoints.errorEndpoint.serverLogic(_ => someService.error.value)

  val endpoints =
    List(
      hello,
      error
    )

}
