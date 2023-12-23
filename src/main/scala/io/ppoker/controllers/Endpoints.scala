package io.ppoker.controllers

import io.ppoker.models.UserId
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.capabilities.zio.ZioStreams.Pipe
import sttp.tapir.ztapir._
import sttp.tapir.PublicEndpoint
import sttp.ws.WebSocketFrame
import zio.stream.ZStream

object Endpoints {

  // ChatRouter
//  val sessions: PublicEndpoint[Unit, (StatusCode, String), ListSessionsResponse, Any] =
//    base.get
//      .in("sessions")
//      .out(jsonBody[ListSessionsResponse])

  type ZPipe[R, A, B] = ZStream[R, Throwable, A] => ZStream[R, Throwable, B]

  def wsEndpoint: PublicEndpoint[UserId, Unit, Pipe[WebSocketFrame, WebSocketFrame], ZioStreams with WebSockets] =
    endpoint.get
      .in("ws")
      .in(query[UserId]("user"))
      .out(webSocketBodyRaw(ZioStreams))
}
