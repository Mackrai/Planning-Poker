package models

import java.util.UUID

case class Task(
    uuid: UUID = UUID.randomUUID(),
    title: String,
    description: Option[String] = None,
    userVotes: Map[User, Int] = Map.empty,
    finalScore: Option[Int] = None
) {

  def isPassed: Boolean = finalScore.nonEmpty
}
