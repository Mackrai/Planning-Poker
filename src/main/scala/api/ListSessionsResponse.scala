package api

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import models.SessionId
import sttp.tapir.Schema
import sttp.tapir.generic.auto.schemaForCaseClass

case class ListSessionsResponse(sessions: Seq[SessionId])

object ListSessionsResponse {
  implicit val codec: Codec.AsObject[ListSessionsResponse] = deriveCodec[ListSessionsResponse]
  implicit lazy val schema: Schema[ListSessionsResponse]   = Schema.derivedSchema
}

