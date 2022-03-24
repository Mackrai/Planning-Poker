package controllers

import sttp.capabilities
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint

trait Router[F[_]] {
  val endpoints: List[ServerEndpoint[Fs2Streams[F], F]]
}

trait WsRouter[F[_]] {
  val endpoints: List[ServerEndpoint[Fs2Streams[F] with capabilities.WebSockets, F]]
}
