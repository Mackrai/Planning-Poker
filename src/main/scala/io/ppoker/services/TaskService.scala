package io.ppoker.services

import cats.effect.kernel.Async

case class TaskService[F[_]: Async]() {

}
