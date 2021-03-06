package io.ppoker.core

import io.ppoker.models.{SessionId, UserId}

// Входящие в систему сообщения
sealed trait InputMessage extends Message {
  override val messageType: MessageType = Incoming
}

//   текст
case class GlobalMessage(text: String) extends InputMessage {
  override def stringify: String = this.toString
}

//   /chat текст
case class ChatMessage(fromUser: UserId, text: String) extends InputMessage {
  override def stringify: String = this.toString
}

//   /join sessionsId userId
case class Join(sessionsId: SessionId, userId: UserId) extends InputMessage {
  override def stringify: String = this.toString
}

//   /leave userId
case class Leave(userId: UserId) extends InputMessage {
  override def stringify: String = this.toString
}

//   /help
case class Help() extends InputMessage {
  override def stringify: String = this.toString
}

//   При отключении сокета
case class Disconnect(userId: UserId) extends InputMessage {
  override def stringify: String = this.toString
}

object InputMessage {

  def parse(raw: String): InputMessage =
    parseCommand(raw) match {
      case ("/join", session, user, _)  => Join(SessionId(session), UserId(user))
      case ("/leave", user, _, _)       => Leave(UserId(user))
      case ("/help", _, _, _)           => Help()
      case ("/chat", fromUser, text, _) => ChatMessage(UserId(fromUser), text)
      case _                            => GlobalMessage(raw)
    }

  private def firstWord(raw: String): (String, String) = {
    val trimmed    = raw.trim
    val firstSpace = trimmed.indexOf(' ')
    if (firstSpace <= 0)
      (trimmed, "")
    else
      trimmed.splitAt(firstSpace)
  }

  private def parseCommand(raw: String): (String, String, String, String) = {
    val (command, arg1WithTail) = firstWord(raw)
    val (arg1, arg2withTail)    = firstWord(arg1WithTail)
    val (arg2, tail)            = firstWord(arg2withTail)
    (command, arg1, arg2, tail.trim)
  }

}
