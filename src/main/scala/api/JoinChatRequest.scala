package api

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import sttp.tapir.Schema
import sttp.tapir.generic.auto.schemaForCaseClass

case class JoinChatRequest(
    chatId: String,
    userId: String
)

object JoinChatRequest {
  implicit val codec: Codec.AsObject[JoinChatRequest] = deriveCodec[JoinChatRequest]
  implicit lazy val schema: Schema[JoinChatRequest]   = Schema.derivedSchema
}
