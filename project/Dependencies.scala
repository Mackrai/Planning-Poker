import sbt._

object Dependencies {

  object V {
    val zio        = "2.0.16"
    val zioLogging = "2.0.0"
    val zioConfig  = "4.0.0-RC14"
    val circe      = "0.14.1"
    val doobie     = "1.0.0-RC1"
    val http4s     = "0.23.23"
    val tapir      = "1.9.6"
  }

  val zio: Seq[ModuleID] =
    Seq(
      "dev.zio" %% "zio"
    ).map(_ % V.zio)

  val zioLogging: Seq[ModuleID] =
    Seq(
      "dev.zio" %% "zio-logging",
      "dev.zio" %% "zio-logging-slf4j"
    ).map(_ % V.zioLogging)

  val zioConfig: Seq[ModuleID] =
    Seq(
      "dev.zio" %% "zio-config",
      "dev.zio" %% "zio-config-typesafe",
      "dev.zio" %% "zio-config-magnolia"
    ).map(_ % V.zioConfig)

  val circe: Seq[ModuleID] = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-generic-extras"
  ).map(_ % V.circe)

  val doobie: Seq[ModuleID] = Seq(
    "org.tpolecat" %% "doobie-core",
    "org.tpolecat" %% "doobie-postgres"
  ).map(_ % V.doobie)

  val http4s: Seq[ModuleID] = Seq(
    "http4s-circe",
    "http4s-ember-client",
    "http4s-ember-server",
    "http4s-dsl"
  ).map("org.http4s" %% _ % V.http4s)

  val tapir: Seq[ModuleID] = Seq(
    "tapir-core",
    "tapir-zio",
    "tapir-zio-http-server",
    "tapir-json-circe",
    "tapir-http4s-server-zio",
    "tapir-http4s-client"
  ).map("com.softwaremill.sttp.tapir" %% _ % V.tapir)
}
