package io.ppoker.models

import io.circe.{Decoder, HCursor}

import java.util.UUID

case class Session(
    id: SessionId = SessionId(),
    title: String,
    description: Option[String] = None,
    currentTask: Option[Task] = None,
    tasks: Set[Task] = Set.empty
)

case class SessionId(raw: String = UUID.randomUUID().toString) extends AnyVal

object SessionId {
  implicit val decoder: Decoder[SessionId] = (c: HCursor) => c.as[String].map(SessionId.apply)
}
