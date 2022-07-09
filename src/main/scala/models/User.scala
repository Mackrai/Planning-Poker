package models

import java.util.UUID

case class User(
    id: UserId = UserId(),
    name: String,
    role: String
)

case class UserId(raw: String = UUID.randomUUID().toString) extends AnyVal
