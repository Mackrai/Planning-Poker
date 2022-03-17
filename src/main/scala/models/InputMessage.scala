package models

// Входящие в систему сообщения
sealed trait InputMessage {
  def stringify: String
}

//   Простой текст
case class ChatMessage(message: String) extends InputMessage {
  override def stringify: String = message
}

//   /join chatId userId
case class JoinChat(chatId: String, userId: String) extends InputMessage {
  override def stringify: String = s"/join $chatId $userId"
}

//   /leave userId
case class LeaveChat(userId: String) extends InputMessage {
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
      case ("/join", chatName, userName, _) => JoinChat(chatName, userName)
      case ("/leave", userName, _, _)       => LeaveChat(userName)
      case ("/help", _, _, _)               => Help()
      case _                                => ChatMessage(raw)
    }

  // TODO Нормальный парсер
  private def extractParts(raw: String) =
    raw.split(" ").toList match {
      case first :: second :: third :: tail => (first, second, third, tail.mkString(" "))
      case first :: second :: third :: Nil  => (first, second, third, "")
    }

}
