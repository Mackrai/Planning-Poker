package controllers

import fs2.Pipe
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.ws.WebSocketFrame

object Endpoints {

  private val base: PublicEndpoint[Unit, (StatusCode, String), Unit, Any] =
    endpoint.errorOut(statusCode).errorOut(stringBody)

  // SomeRouter
  val helloEndpoint: PublicEndpoint[Unit, (StatusCode, String), String, Any] =
    base.get
      .in("hello")
      .out(jsonBody[String])

  val errorEndpoint: PublicEndpoint[Unit, (StatusCode, String), Int, Any] =
    base.get
      .in("error")
      .out(jsonBody[Int])

  def wsEndpoint[F[_]]: PublicEndpoint[Unit, (StatusCode, String), Pipe[F, WebSocketFrame, WebSocketFrame], Fs2Streams[F] with WebSockets] =
    base.get.in("ws").out(webSocketBodyRaw(Fs2Streams[F]))

}
