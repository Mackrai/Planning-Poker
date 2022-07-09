package api

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import sttp.tapir.Schema
import sttp.tapir.generic.auto.schemaForCaseClass

case class ListSessionsResponse(sessions: Seq[SessionIdsDTO])

object ListSessionsResponse {
  implicit val codec: Codec.AsObject[ListSessionsResponse] = deriveCodec[ListSessionsResponse]
  implicit lazy val schema: Schema[ListSessionsResponse]   = Schema.derivedSchema
}

case class SessionIdsDTO(sessionId: String, userIds: Seq[String])

object SessionIdsDTO {
  implicit val codec: Codec.AsObject[SessionIdsDTO] = deriveCodec[SessionIdsDTO]
  implicit lazy val schema: Schema[SessionIdsDTO]   = Schema.derivedSchema
}
