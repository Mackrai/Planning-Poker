package models

import java.util.UUID

case class Session(
    uuid: UUID = UUID.randomUUID(),
    title: String,
    description: Option[String] = None,
    currentTask: Option[Task] = None,
    tasks: Set[Task] = Set.empty,
    users: Set[User]
)
