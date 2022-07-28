package io.ppoker.models

import java.util.UUID

case class Task(
    id: TaskId = TaskId(),
    session: SessionId,
    title: String,
    description: Option[String] = None,
    userVotes: Map[User, Int] = Map.empty,
    finalScore: Option[Int] = None
) {

  def isPassed: Boolean = finalScore.nonEmpty
}

case class TaskId(raw: String = UUID.randomUUID().toString) extends AnyVal