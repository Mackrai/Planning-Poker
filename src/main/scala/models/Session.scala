package models

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import java.util.UUID

case class Session[F[_]](
    uuid: UUID = UUID.randomUUID(),
    title: String,
    description: Option[String] = None,
    currentTask: Option[Task] = None,
    tasks: Set[Task] = Set.empty,
    users: Set[User] = Set.empty
) {
  def addUser(user: User): Session[F]    = this.copy(users = users + user)
  def removeUser(user: User): Session[F] = this.copy(users = users - user)
}

case class SessionId(raw: String) extends AnyVal

object SessionId {
  implicit val codec: Codec[SessionId] = deriveCodec
}