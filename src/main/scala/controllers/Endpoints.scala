package controllers

import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody

object Endpoints {

  val base: Endpoint[Unit, (StatusCode, String), Unit, Any] =
    endpoint.in("api").errorOut(statusCode).errorOut(stringBody)

  val helloEndpoint: Endpoint[Unit, (StatusCode, String), String, Any] =
    base.get
      .in("hello")
      .out(jsonBody[String])

  val errorEndpoint: Endpoint[Unit, (StatusCode, String), Int, Any] =
    base.get
      .in("error")
      .out(jsonBody[Int])

}
