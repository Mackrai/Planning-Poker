package models

case class AppState[F[_]](sessions: Map[String, Session[F]], users: Map[String, User])