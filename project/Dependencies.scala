import sbt._

object Dependencies {

  object Version {
    val cats       = "2.7.0"
    val catsEffect = "2.5.1"
    val circe      = "0.14.1"
    val http4s     = "0.22.0"
    val pureConfig = "0.17.1"
    val tapir      = "0.18.3"
  }

  val cats       = "org.typelevel" %% "cats-core"   % Version.cats
  val catsEffect = "org.typelevel" %% "cats-effect" % Version.catsEffect

  val circe = "io.circe" %% "circe-generic" % Version.circe

  val http4s: Seq[ModuleID] = Seq(
    "http4s-blaze-server",
    "http4s-blaze-client",
    "http4s-circe",
    "http4s-dsl"
  ).map("org.http4s" %% _ % Version.http4s)

  val tapir: Seq[ModuleID] = Seq(
    "tapir-core",
    "tapir-json-circe",
    "tapir-http4s-server"
  ).map("com.softwaremill.sttp.tapir" %% _ % Version.tapir)

  val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.17.1"
}
