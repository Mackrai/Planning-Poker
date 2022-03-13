package controllers

import sttp.model.StatusCode
import sttp.tapir.server.ServerEndpoint

trait Router[F[_]] {
  val endpoints: List[ServerEndpoint[_, (StatusCode, String), _, Any, F]]
}
