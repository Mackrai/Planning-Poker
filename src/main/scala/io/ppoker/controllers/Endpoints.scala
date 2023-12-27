package io.ppoker.controllers

import io.ppoker.models.UserId
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.capabilities.zio.ZioStreams.Pipe
import sttp.tapir.PublicEndpoint
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir._
import sttp.ws.WebSocketFrame

object Endpoints {

  def sessions =
    endpoint.get
      .in("sessions")
      .out(jsonBody[List[String]])

  def subscribers =
    endpoint.get
      .in("subscribers")
      .in(query[String]("topic"))
      .out(jsonBody[List[String]])

  def wsEndpoint: PublicEndpoint[(String, UserId), Unit, Pipe[WebSocketFrame, WebSocketFrame], ZioStreams with WebSockets] =
    endpoint.get
      .in("ws")
      .in(query[String]("topic"))
      .in(query[UserId]("user"))
      .out(webSocketBodyRaw(ZioStreams))
}
