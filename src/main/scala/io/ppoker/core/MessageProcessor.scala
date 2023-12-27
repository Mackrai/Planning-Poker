package io.ppoker.core

import io.ppoker.models.UserId
import zio.{Task, ZIO, ZLayer}

trait MessageProcessor {
  def processInputMessage(inputMessage: InputMessage): Task[OutputMessage]
}

final class MessageProcessorImpl(hubManager: HubManager) extends MessageProcessor {
  override def processInputMessage(inputMessage: InputMessage): Task[OutputMessage] =
    inputMessage match {
      case ChatMessage(topic, userId, text) =>
        getUsers(topic).map(_.filter(_ != userId)).map(ToUsers(_, text))

      case message =>
        throw new Exception(s"Invalid InputMessage type: [${message.getClass.toString}]")
    }

  private def getUsers(topic: String): Task[List[UserId]] =
    hubManager.getSubscribers(topic)
}

object MessageProcessor {
  val live: ZLayer[HubManager, Nothing, MessageProcessorImpl] =
    ZLayer {
      ZIO.service[HubManager].map(new MessageProcessorImpl(_))
    }
}
