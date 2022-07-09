package api

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import models.SessionId
import sttp.tapir.Schema
import sttp.tapir.generic.auto.schemaForCaseClass

case class ListSessionsResponse(sessions: Seq[SessionResponse])

object ListSessionsResponse {
  implicit val codec: Codec.AsObject[ListSessionsResponse] = deriveCodec[ListSessionsResponse]
  implicit lazy val schema: Schema[ListSessionsResponse]   = Schema.derivedSchema
}

case class SessionResponse(id: SessionId)

object SessionResponse {
  implicit val codec: Codec.AsObject[SessionResponse] = deriveCodec[SessionResponse]
  implicit lazy val schema: Schema[SessionResponse]   = Schema.derivedSchema
}
