package api

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import sttp.tapir.Schema
import sttp.tapir.generic.auto.schemaForCaseClass

case class CreateChatRequest(title: String)

object CreateChatRequest {
  implicit val codec: Codec.AsObject[CreateChatRequest] = deriveCodec[CreateChatRequest]
  implicit lazy val schema: Schema[CreateChatRequest]   = Schema.derivedSchema
}
