package util

import api.{CreateChatRequest, JoinChatRequest, ListSessionsResponse}
import sttp.model.StatusCode
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{Endpoint, endpoint, statusCode, stringBody}

object Endpoints {

  private val base: Endpoint[Unit, (StatusCode, String), Unit, Any] =
    endpoint.errorOut(statusCode).errorOut(stringBody)

  // ChatRouter
  val sessions: Endpoint[Unit, (StatusCode, String), ListSessionsResponse, Any] =
    base.get
      .in("sessions")
      .out(jsonBody[ListSessionsResponse])

  val createChat: Endpoint[CreateChatRequest, (StatusCode, String), Unit, Any] =
    base.post
      .in("createChat")
      .in(jsonBody[CreateChatRequest])

  val joinChat: Endpoint[JoinChatRequest, (StatusCode, String), Unit, Any] =
    base.post
      .in("joinChat")
      .in(jsonBody[JoinChatRequest])

}
