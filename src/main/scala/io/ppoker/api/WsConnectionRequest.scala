package io.ppoker.api

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import sttp.tapir.Schema
import sttp.tapir.generic.auto.schemaForCaseClass

case class WsConnectionRequest(topicId: String,
                               userId: String)

object WsConnectionRequest {
  implicit val codec: Codec.AsObject[WsConnectionRequest] = deriveCodec[WsConnectionRequest]
  implicit lazy val schema: Schema[WsConnectionRequest]   = Schema.derivedSchema
}
