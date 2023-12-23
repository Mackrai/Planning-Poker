import Dependencies._

lazy val root =
  (project in file("."))
    .settings(
      name         := "Planning-Poker",
      version      := "0.1.0-SNAPSHOT",
      scalaVersion := "2.13.8",
      libraryDependencies ++= Seq(
        zio,
        zioLogging,
        zioConfig,
        circe,
        doobie,
        http4s,
        tapir
      ).flatten,
      libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.7",
      addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
      scalacOptions ++= Seq(
        "-feature",
        "-deprecation",
        "-unchecked",
        "-language:postfixOps",
        "-Ymacro-annotations"
      )
    )
