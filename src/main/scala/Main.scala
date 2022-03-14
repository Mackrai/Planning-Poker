import cats.effect._
import cats.implicits._
import configs.ServerConfig
import controllers.{ChatRouter, SomeRouter}
import fs2.concurrent.{Queue, Topic}
import models.{InputMessage, OutChatMessage, OutputMessage}
import org.http4s._
import org.http4s.blaze.server._
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import services.SomeService
import sttp.tapir.server.http4s.Http4sServerInterpreter

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {

  implicit val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] = start[IO]

  private def start[F[_]: ConcurrentEffect: Timer: ContextShift: Logger]: F[ExitCode] =
    for {
      serverConf   <-
        ConcurrentEffect[F].pure(ConfigSource.default.loadOrThrow[ServerConfig]) <*
          Logger[F].info("Loaded server config")
      chatQueue    <- Queue.unbounded[F, InputMessage] // TODO POK-3 Разобраться, надо ли использовать Queue (скорее всего надо)
      chatTopic    <- Topic[F, OutputMessage](OutChatMessage("Start topic"))
      routes        = httpApp(chatQueue, chatTopic)
      queueStream   =
        // Достаем из очереди InputMessage
        chatQueue.dequeue
          // Пока что план такой:
          //    Тут будем обрабатывать InputMessage, преобразовывать в OutputMessage
          .map(inputMessage => OutChatMessage(inputMessage.stringify)) // TODO POK-1 Состояние приложения
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
      topic: Topic[F, OutputMessage]
  ): HttpApp[F] = {
    val someService = new SomeService[F]

    val someRouter = new SomeRouter[F](someService)
    val chatRouter = new ChatRouter[F](queue, topic)

    val routers = List(someRouter, chatRouter)

    (Http4sServerInterpreter[F]()
      .toRoutes(routers.flatMap(_.endpoints)) <+> chatRouter.wsRoute).orNotFound
  }

}
