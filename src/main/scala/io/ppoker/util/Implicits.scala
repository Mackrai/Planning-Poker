package io.ppoker.util

import io.ppoker.core.Message
import org.typelevel.log4cats.Logger

object Implicits {

  object Logger {
    implicit class LoggerOps[F[_]](logger: Logger[F]) {
      def logMessage(message: Message): F[Unit] = logger.info(s"${message.messageType}: [${message.toString}]")
    }
  }

}
