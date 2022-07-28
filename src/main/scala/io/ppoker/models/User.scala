package io.ppoker.models

import io.circe.{Decoder, HCursor}

import java.util.UUID

case class User(
    id: UserId = UserId(),
    name: String,
    role: String
)

case class UserId(raw: String = UUID.randomUUID().toString) extends AnyVal

object UserId {
  implicit val decoder: Decoder[UserId] = (c: HCursor) => c.as[String].map(UserId.apply)
}
