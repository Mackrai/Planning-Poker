import sbt._

object Dependencies {

    object Version {
        val cats       = "2.7.0"
        val catsEffect = "2.5.1"
        val circe      = "0.14.1"
        val http4s     = "0.21.32"
        val tapir      = "0.20.0"
    }

    val cats       = "org.typelevel" %% "cats-core"   % Version.cats
    val catsEffect = "org.typelevel" %% "cats-effect" % Version.catsEffect

    val tapir = "com.softwaremill.sttp.tapir" %% "tapir-core" % Version.tapir

    val http4s: Seq[ModuleID] = Seq(
        "http4s-ember-server",
        "http4s-ember-client",
        "http4s-circe",
        "http4s-dsl"
    ).map("org.http4s" %% _ % Version.http4s)

    val circe = "io.circe" %% "circe-generic" % Version.circe
}
