import sbt._

object Dependencies {

  object Version {
    val cats                = "2.7.0"
    val catsEffect          = "3.3.8"
    val catsEffectScalaTest = "1.4.0"
    val circe               = "0.14.1"
    val http4s              = "0.23.12"
    val logback             = "1.2.11"
    val log4cats            = "2.2.0"
    val pureConfig          = "0.17.1"
    val scalatest           = "3.2.12"
    val tapir               = "1.0.1"
    val fs2                 = "3.2.10"
  }

  val cats                = "org.typelevel" %% "cats-core"                     % Version.cats
  val catsEffect          = "org.typelevel" %% "cats-effect"                   % Version.catsEffect
  val catsEffectScalaTest = "org.typelevel" %% "cats-effect-testing-scalatest" % Version.catsEffectScalaTest % Test

  val scalactic = "org.scalactic" %% "scalactic" % Version.scalatest
  val scalatest = "org.scalatest" %% "scalatest" % Version.scalatest % "test"

  val circe = "io.circe" %% "circe-generic" % Version.circe

  val fs2 = "co.fs2" %% "fs2-core" % Version.fs2

  val http4s: Seq[ModuleID] = Seq(
    "http4s-blaze-server",
    "http4s-blaze-client",
    "http4s-circe",
    "http4s-dsl"
  ).map("org.http4s" %% _ % Version.http4s)

  val tapir: Seq[ModuleID] = Seq(
    "tapir-core",
    "tapir-json-circe",
    "tapir-http4s-server",
    "tapir-http4s-client"
  ).map("com.softwaremill.sttp.tapir" %% _ % Version.tapir)

  val logback = "ch.qos.logback" % "logback-classic" % Version.logback

  val log4cats = "org.typelevel" %% "log4cats-slf4j" % Version.log4cats

  val pureConfig = "com.github.pureconfig" %% "pureconfig" % Version.pureConfig
}
