import cats.effect._
import services.SomeService

object Main extends IOApp {

    override def run(args: List[String]): IO[ExitCode] =
        foo[IO] *> IO(ExitCode.Success)

    def foo[F[_]: Sync]: F[Unit] = SomeService.foo

}
