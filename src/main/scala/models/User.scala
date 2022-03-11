package models

import java.util.UUID

case class User(
    uuid: UUID = UUID.randomUUID(),
    name: String,
    role: String
)
