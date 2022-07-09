package models

// Входящие в систему сообщения
sealed trait InputMessage {
  def stringify: String
}

//   Простой текст
case class ChatMessage(message: String) extends InputMessage {
  override def stringify: String = message
}

//   /join sessionsId userId
case class JoinChat(sessionsId: SessionId, userId: UserId) extends InputMessage {
  override def stringify: String = s"/join $sessionsId $userId"
}

//   /leave userId
case class LeaveChat(userId: UserId) extends InputMessage {
  override def stringify: String = s"/leave $userId"
}

//   /help
case class Help() extends InputMessage {
  override def stringify: String = "/help"
}

//   При отключении сокета
case class Disconnect() extends InputMessage {
  override def stringify: String = "WS disconnected"
}

object InputMessage {
  def parse(raw: String): InputMessage =
    extractParts(raw) match {
      case ("/join", session, user, _) => JoinChat(SessionId(session), UserId(user))
      case ("/leave", user, _, _)      => LeaveChat(UserId(user))
      case ("/help", _, _, _)          => Help()
      case _                           => ChatMessage(raw)
    }

  // TODO Нормальный парсер
  private def extractParts(raw: String) =
    raw.split(" ").toList match {
      case first :: second :: third :: tail => (first, second, third, tail.mkString(" "))
      case first :: second :: third :: Nil  => (first, second, third, "")
    }

}
