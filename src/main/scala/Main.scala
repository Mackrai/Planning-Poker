import cats.effect._
import cats.effect.std.Queue
import cats.implicits._
import configs.ServerConfig
import controllers.ChatRouter
import fs2.Stream
import fs2.concurrent.Topic
import models.{AppState, InputMessage, OutputMessage}
import org.http4s._
import org.http4s.blaze.server._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.websocket.WebSocketBuilder2
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import sttp.tapir.server.http4s.Http4sServerInterpreter

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = start[IO]

  private def start[F[_]: Async: Concurrent: MonadCancelThrow]: F[ExitCode] = {
    implicit val logger: Logger[F] = Slf4jLogger.getLogger[F]
    for {
      serverConf <-
        Concurrent[F].delay(ConfigSource.default.loadOrThrow[ServerConfig]) <* Logger[F].info("Loaded server config")
      chatQueue  <- Queue.unbounded[F, InputMessage]
      chatTopic  <- Topic[F, OutputMessage]
      appState   <- Ref[F].of(AppState.singleSession)
      routes      = httpApp(chatQueue, chatTopic, appState)

      queueStream =
        Stream
          .fromQueueUnterminated(chatQueue)
          .evalMap(inputMsg => appState.modify(_.processInputMessage(inputMsg)))
          .flatMap(Stream.emits)
          .evalTap(outputMessage => Logger[F].info(s"OUTGOING: [${outputMessage.stringify}]"))
          .through(chatTopic.publish)

      serverStream <-
        fs2.Stream(httpServer(serverConf, routes), queueStream).parJoinUnbounded.compile.drain.as(ExitCode.Success)
    } yield serverStream
  }

  private def httpServer[F[_]: Async](
      conf: ServerConfig,
      routesApp: WebSocketBuilder2[F] => HttpRoutes[F]
  ): fs2.Stream[F, ExitCode] =
    BlazeServerBuilder[F]
      .bindHttp(conf.port, conf.host)
      .withHttpWebSocketApp(wsb => routesApp(wsb).orNotFound)
      .serve

  private def httpApp[F[_]: Async: Logger](
      queue: Queue[F, InputMessage],
      topic: Topic[F, OutputMessage],
      state: Ref[F, AppState]
  ): WebSocketBuilder2[F] => HttpRoutes[F] = {
    val chatRouter = new ChatRouter[F](queue, topic, state)

    Http4sServerInterpreter[F]().toWebSocketRoutes(List(chatRouter).flatMap(_.endpoints))
  }
}
