package io.ppoker.models

import io.circe.Decoder

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

object TaskId {
  implicit val decoder: Decoder[TaskId] = _.as[String].map(TaskId.apply)
}
