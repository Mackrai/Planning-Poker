package io.ppoker.core

import io.ppoker.models.UserId

// Исходящие сообщения, которые получат пользователи
trait OutputMessage extends Message {
  override val messageType: MessageType = Outgoing
  def forUser(userId: UserId): Boolean
}

case class ToUser(userId: UserId, text: String) extends OutputMessage {
  override def stringify: String = this.toString
  override def forUser(userId: UserId): Boolean = userId == this.userId
}

case class ToUsers(userIds: Seq[UserId], text: String) extends OutputMessage {
  override def stringify: String = this.toString
  override def forUser(userId: UserId): Boolean = userIds.contains(userId)
}
