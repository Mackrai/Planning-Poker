package io.ppoker.models

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import sttp.tapir.Schema
import sttp.tapir.generic.auto.schemaForCaseClass

import java.util.UUID

case class Session[F[_]](
    id: SessionId = SessionId(),
    title: String,
    description: Option[String] = None,
    currentTask: Option[Task] = None,
    tasks: Set[Task] = Set.empty,
    users: Set[User] = Set.empty
) {
  def addUser(user: User): Session[F]    = this.copy(users = users + user)
  def removeUser(user: User): Session[F] = this.copy(users = users - user)
}

case class SessionId(raw: String = UUID.randomUUID().toString) extends AnyVal

object SessionId {
  implicit val codec: Codec[SessionId]        = deriveCodec
  implicit lazy val schema: Schema[SessionId] = Schema.derivedSchema
}
