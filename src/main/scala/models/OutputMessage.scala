package models

// Исходящие сообщения, которые получат пользователи
trait OutputMessage {
  def stringify: String
}

case class OutChatMessage(text: String) extends OutputMessage {
  override def stringify: String = text
}
