package io.ppoker.core

import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, DecodingFailure, HCursor}
import io.ppoker.models.{SessionId, UserId}
import io.ppoker.util.Constant.CommandTypes

/**
 * Incoming messages (formatted as Json) are parsed to one of the [[InputMessage]] subclasses.
 *
 * The `command` field determines handling logic, `args` contains arguments for the said command.
 *
 * Later, parsed messages are handled by [[AppState.processInputMessage]]
 * @example [[Join]] {{{
 * {
 *    "command": "join",
 *    "args": {
 *        "userId": "981d632a17054da1b2f525fbb667d9d1",
 *        "sessionId": "4dc9e514bc4846d6a17ceb8574c784f8"
 *    } }}}
 * }
 */
sealed trait InputMessage extends Message {
  override val messageType: MessageType = Incoming
}

object InputMessage {
  implicit val decoder: Decoder[InputMessage] =
    (c: HCursor) =>
      c.get[String]("command").flatMap {
        case CommandTypes.AddTask       => c.get[AddTask]("args")
        case CommandTypes.Join          => c.get[Join]("args")
        case CommandTypes.Leave         => c.get[Leave]("args")
        case CommandTypes.ChatMessage   => c.get[ChatMessage]("args")
        case CommandTypes.GlobalMessage => c.get[GlobalMessage]("args")
        case unknown                    => Left(DecodingFailure(s"Invalid command: [$unknown]", Nil))
      }
}

final case class AddTask(sessionId: SessionId, title: String, description: Option[String]) extends InputMessage
object AddTask {
  implicit val decoder: Decoder[AddTask] = deriveDecoder
}

final case class GlobalMessage(text: String) extends InputMessage
object GlobalMessage {
  implicit val decoder: Decoder[GlobalMessage] = deriveDecoder
}

final case class ChatMessage(topic: String, fromUser: UserId, text: String) extends InputMessage
object ChatMessage {
  implicit val decoder: Decoder[ChatMessage] = deriveDecoder
}

final case class Join(userId: UserId, sessionId: SessionId) extends InputMessage
object Join {
  implicit val decoder: Decoder[Join] = deriveDecoder
}

final case class Leave(userId: UserId) extends InputMessage
object Leave {
  implicit val decoder: Decoder[Leave] = deriveDecoder
}

final case class Disconnect(userId: UserId) extends InputMessage
