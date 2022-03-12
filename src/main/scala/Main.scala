import cats.effect._
import cats.implicits._
import configs.ServerConfig
import controllers.SomeRouter
import fs2.concurrent.{Queue, Topic}
import org.http4s._
import org.http4s.blaze.server._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import services.{ChatService, SomeService}
import sttp.tapir.server.http4s.Http4sServerInterpreter

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {

  implicit val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] = start[IO]

  private def start[F[_]: ConcurrentEffect: Timer: ContextShift: Logger]: F[ExitCode] =
    for {
      serverConf <-
        ConcurrentEffect[F].pure(ConfigSource.default.loadOrThrow[ServerConfig]) <*
          Logger[F].info("Loaded server config")
      chatQueue  <- Queue.unbounded[F, Unit]
      chatTopic  <- Topic[F, Unit]()
      routes      = httpApp(chatQueue, chatTopic)
      server     <- httpServer(serverConf, routes).compile.drain.as(ExitCode.Success)
    } yield server

  private def httpServer[F[_]: ConcurrentEffect: Timer](
      conf: ServerConfig,
      routes: HttpApp[F]
  ): fs2.Stream[F, ExitCode] =
    BlazeServerBuilder(global)
      .bindHttp(conf.port, conf.host)
      .withHttpApp(routes)
      .serve

  private def httpApp[F[_]: ConcurrentEffect: Timer: ContextShift: Logger](
      queue: Queue[F, Unit],
      topic: Topic[F, Unit]
  ): HttpApp[F] = {
    val someService = new SomeService[F]
    val chatService = new ChatService[F](queue, topic)

    val someRouter = new SomeRouter[F](someService)

    val routers = List(someRouter)

    Http4sServerInterpreter[F]()
      .toRoutes(routers.flatMap(_.endpoints))
      .orNotFound
  }

}
