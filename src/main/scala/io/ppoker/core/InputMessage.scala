package io.ppoker.core

import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, DecodingFailure, HCursor}
import io.ppoker.models.{SessionId, UserId}

sealed trait InputMessage extends Message {
  override val messageType: MessageType = Incoming
}

object InputMessage {
  implicit val decoder: Decoder[InputMessage] =
    (c: HCursor) =>
      c.get[String]("command").flatMap {
        case "addTask"    => c.get[AddTask]("args")
        case "join"       => c.get[Join]("args")
        case "leave"      => c.get[Leave]("args")
        case "chat"       => c.get[ChatMessage]("args")
        case "globalChat" => c.get[GlobalMessage]("args")
        case command      => Left(DecodingFailure(s"Invalid command: [$command]", Nil))
      }
}

case class AddTask(sessionId: SessionId,
                   title: String,
                   description: Option[String]) extends InputMessage
object AddTask {
  implicit val decoder: Decoder[AddTask] = deriveDecoder
}

case class GlobalMessage(text: String) extends InputMessage
object GlobalMessage {
  implicit val decoder: Decoder[GlobalMessage] = deriveDecoder
}

case class ChatMessage(fromUser: UserId, text: String) extends InputMessage
object ChatMessage {
  implicit val decoder: Decoder[ChatMessage] = deriveDecoder
}

case class Join(userId: UserId, sessionId: SessionId) extends InputMessage
object Join {
  implicit val decoder: Decoder[Join] = deriveDecoder
}

case class Leave(userId: UserId) extends InputMessage
object Leave {
  implicit val decoder: Decoder[Leave] = deriveDecoder
}

case class Disconnect(userId: UserId) extends InputMessage
