import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import configs.ServerConfig
import controllers.ChatRouter
import fs2.concurrent.{Queue, Topic}
import models._
import org.http4s._
import org.http4s.blaze.server._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.middleware._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
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
      chatQueue  <- Queue.unbounded[F, InputMessage]
      chatTopic  <- Topic[F, OutputMessage](OutChatMessage("Start topic"))

      state <- Ref[F].of(AppState(Map.empty[String, Session[F]], Map.empty[String, User]))

      routes        = httpApp(chatQueue, chatTopic, state)
      queueStream   =
        // Достаем из очереди InputMessage
        chatQueue.dequeue
          // Пока что план такой:
          //    Тут будем обрабатывать InputMessage, преобразовывать в OutputMessage
          .map(inputMessage => OutChatMessage(inputMessage.stringify))
          //        и отправлять в topic
          .through(chatTopic.publish)
      serverStream <-
        fs2.Stream(httpServer(serverConf, routes), queueStream).parJoinUnbounded.compile.drain.as(ExitCode.Success)
    } yield serverStream

  private def httpServer[F[_]: ConcurrentEffect: Timer](
      conf: ServerConfig,
      routes: HttpApp[F]
  ): fs2.Stream[F, ExitCode] =
    BlazeServerBuilder(global)
      .bindHttp(conf.port, conf.host)
      .withHttpApp(routes)
      .serve

  private def httpApp[F[_]: ConcurrentEffect: Timer: ContextShift: Logger](
      queue: Queue[F, InputMessage],
      topic: Topic[F, OutputMessage],
      state: Ref[F, AppState[F]]
  ): HttpApp[F] = {
    val chatRouter = new ChatRouter[F](queue, topic, state)

    val routers = List(chatRouter)

    CORS(
      Http4sServerInterpreter[F]()
        .toRoutes(routers.flatMap(_.endpoints)) <+> chatRouter.joinChat
    ).orNotFound
  }

}
