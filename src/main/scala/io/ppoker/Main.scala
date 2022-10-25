package io.ppoker

import cats.effect._
import cats.effect.std.Queue
import cats.implicits._
import doobie.util.transactor.Transactor
import fs2.Stream
import fs2.concurrent.Topic
import io.ppoker.configs.{AppConfig, ServerConfig}
import io.ppoker.controllers._
import io.ppoker.core.{AppState, InputMessage, OutputMessage}
import io.ppoker.models.Auth
import io.ppoker.services.AuthService
import io.ppoker.util.Implicits.Logger.LoggerOps
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.websocket.WebSocketBuilder2
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = start[IO]

  private def start[F[_]: Async: Concurrent: MonadCancelThrow]: F[ExitCode] = {
    implicit val logger: Logger[F] = Slf4jLogger.getLogger[F]
    for {
      config <- Concurrent[F].delay(ConfigSource.default.loadOrThrow[AppConfig]) <* Logger[F].info("Loaded app config")
      dbConfig = config.db

      transactor = Transactor.fromDriverManager[F](dbConfig.driver, dbConfig.url, dbConfig.user, dbConfig.pass)

      authService = AuthService(transactor)

      chatQueue <- Queue.unbounded[F, InputMessage]
      chatTopic <- Topic[F, OutputMessage]
      appState  <- Ref[F].of(AppState.singleSession)

      exitCode <- {
        val httpServerStream = httpServer(config.server, httpApp(chatQueue, chatTopic, appState))
        val processingStream =
          Stream
            .fromQueueUnterminated(chatQueue)
            .evalMap(inputMsg => appState.modify(_.processInputMessage(inputMsg)))
            .flatMap(Stream.emits)
            .evalTap(Logger[F].logMessage)
            .through(chatTopic.publish)
            .handleErrorWith(error => Stream.eval(Logger[F].error(error.getMessage)))

        Stream(httpServerStream, processingStream).parJoinUnbounded.compile.drain.as(ExitCode.Success)
      }
    } yield exitCode
  }

  private def httpServer[F[_]: Async](
      conf: ServerConfig,
      buildRoutes: WebSocketBuilder2[F] => HttpRoutes[F]
  ): fs2.Stream[F, ExitCode] =
    BlazeServerBuilder[F]
      .bindHttp(conf.port, conf.host)
      .withHttpWebSocketApp(buildRoutes(_).orNotFound)
      .serve

  private def httpApp[F[_]: Async: Logger](
      queue: Queue[F, InputMessage],
      topic: Topic[F, OutputMessage],
      state: Ref[F, AppState]
  ): WebSocketBuilder2[F] => HttpRoutes[F] = { wsb =>
    val chatRouter = new ChatRouter[F](wsb, queue, topic, state)
    chatRouter.routes
  }

}
