package io.ppoker.core

import io.ppoker.models.{TaskId, UserId}

trait OutputMessage extends Message {
  override val messageType: MessageType = Outgoing
  def forUser(userId: UserId): Boolean
}

final case class TaskUpdated(userIds: Seq[UserId], taskId: TaskId) extends OutputMessage {
  override def forUser(userId: UserId): Boolean = userIds.contains(userId)
}

final case class ToUser(userId: UserId, text: String) extends OutputMessage {
  override def forUser(userId: UserId): Boolean = userId == this.userId
}

final case class ToUsers(userIds: Seq[UserId], text: String) extends OutputMessage {
  override def forUser(userId: UserId): Boolean = userIds.contains(userId)
}
