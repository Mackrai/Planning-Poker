package io.ppoker.core

import io.ppoker.models.UserId
import zio.{ZIO, ZLayer}

trait MessageProcessor {
  def processInputMessage(inputMessage: InputMessage): Seq[OutputMessage]
}

final class MessageProcessorImpl extends MessageProcessor {
  def processInputMessage(inputMessage: InputMessage): Seq[OutputMessage] =
    inputMessage match {
      case ChatMessage(fromUser, text) =>
        Seq(
          ToUser(UserId("Valera"), text)
        )

      case message =>
        throw new Exception(s"Invalid InputMessage type: [${message.getClass.toString}]")
    }
}

object MessageProcessor {
  val live: ZLayer[Any, Nothing, MessageProcessorImpl] =
    ZLayer {
      ZIO.succeed(new MessageProcessorImpl)
    }
}
