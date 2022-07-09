import cats.effect._
import cats.effect.std.Queue
import cats.implicits._
import configs.ServerConfig
import controllers.ChatRouter
import fs2.concurrent.Topic
import models.{AppState, InputMessage, OutChatMessage, OutputMessage}
import org.http4s._
import org.http4s.blaze.server._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import sttp.tapir.server.http4s.Http4sServerInterpreter
import fs2.Stream
import org.http4s.server.websocket.WebSocketBuilder2

object Main extends IOApp {

  implicit val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] = start[IO]

  private def start[F[_]: Async: Concurrent: Logger: MonadCancelThrow]: F[ExitCode] =
    for {
      serverConf   <-
        Concurrent[F].delay(ConfigSource.default.loadOrThrow[ServerConfig]) <* Logger[F].info("Loaded server config")
      chatQueue    <- Queue.unbounded[F, InputMessage]
      streamFromChatQueue = Stream.fromQueueUnterminated(chatQueue)
      chatTopic    <- Topic[F, OutputMessage]
      appState     <- Ref[F].of(AppState.empty)
      routes        = httpApp(chatQueue, chatTopic, appState)
      queueStream   =
        streamFromChatQueue
          .map(inputMessage => OutChatMessage(inputMessage.stringify))
          .through(chatTopic.publish)
      serverStream <-
        fs2.Stream(httpServer(serverConf, routes), queueStream).parJoinUnbounded.compile.drain.as(ExitCode.Success)
    } yield serverStream

  private def httpServer[F[_]: Async](
      conf: ServerConfig,
      routesApp: WebSocketBuilder2[F] => HttpRoutes[F],
  ): fs2.Stream[F, ExitCode] =
    BlazeServerBuilder[F]
      .bindHttp(conf.port, conf.host)
      .withHttpWebSocketApp(wsb => routesApp(wsb).orNotFound)
      .serve

  private def httpApp[F[_]: Async: Logger](
      queue: Queue[F, InputMessage],
      topic: Topic[F, OutputMessage],
      state: Ref[F, AppState]
  ) = {
    val chatRouter = new ChatRouter[F](queue, topic, state)

    val routers = List(chatRouter)

    Http4sServerInterpreter[F]()
      .toWebSocketRoutes(routers.flatMap(_.endpoints))
  }
}
