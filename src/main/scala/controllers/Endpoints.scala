package controllers

import api.ListSessionsResponse
import fs2.Pipe
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.StatusCode
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{PublicEndpoint, endpoint, query, statusCode, stringBody, webSocketBodyRaw}
import sttp.ws.WebSocketFrame

object Endpoints {

  private val base: PublicEndpoint[Unit, (StatusCode, String), Unit, Any] =
    endpoint.errorOut(statusCode).errorOut(stringBody)

  // ChatRouter
  val sessions: PublicEndpoint[Unit, (StatusCode, String), ListSessionsResponse, Any] =
    base.get
      .in("sessions")
      .out(jsonBody[ListSessionsResponse])

  def wsEndpoint[F[_]]: PublicEndpoint[String, (StatusCode, String), Pipe[F, WebSocketFrame, WebSocketFrame], Fs2Streams[F] with WebSockets] =
    base.get.in("ws").in(query[String]("user")).out(webSocketBodyRaw(Fs2Streams[F]))

}
