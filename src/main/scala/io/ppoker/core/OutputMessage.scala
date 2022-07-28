package io.ppoker.core

import io.ppoker.models.{TaskId, UserId}

// Исходящие сообщения, которые получат пользователи
trait OutputMessage extends Message {
  override val messageType: MessageType = Outgoing
  def forUser(userId: UserId): Boolean
}

case class TaskUpdated(userIds: Seq[UserId], taskId: TaskId) extends OutputMessage {
  override def forUser(userId: UserId): Boolean = userIds.contains(userId)
}

case class ToUser(userId: UserId, text: String) extends OutputMessage {
  override def forUser(userId: UserId): Boolean = userId == this.userId
}

case class ToUsers(userIds: Seq[UserId], text: String) extends OutputMessage {
  override def forUser(userId: UserId): Boolean = userIds.contains(userId)
}
