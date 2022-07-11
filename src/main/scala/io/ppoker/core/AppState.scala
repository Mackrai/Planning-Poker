package io.ppoker.core

import io.ppoker.models.{SessionId, UserId}

case class AppState(sessions: Map[SessionId, Set[UserId]]) {

  def processInputMessage(inputMessage: InputMessage): (AppState, Seq[OutputMessage]) =
    inputMessage match {
      case GlobalMessage(text) =>
        this -> allUsers.map(ToUser(_, text))

      case ChatMessage(fromUser, text) =>
        this -> getUserSession(fromUser).map(sendToChat(_, text)).toSeq.flatten

      case Join(sessionId, userId) =>
        val nextState = addUserToSession(sessionId, userId)
        nextState -> Seq(ToUsers(nextState.allUsers, s"User $userId has joined"))

      case Leave(userId) => disconnectUser(userId)

      case Help() =>
        this -> Seq(ToUsers(allUsers, "Test message"))

      case Disconnect(userId) => disconnectUser(userId)
    }

  private def allUsers: Seq[UserId] = sessions.values.toSeq.flatMap(_.toSeq)

  private def getUserSession(userId: UserId): Option[SessionId] = sessions.find(_._2.contains(userId)).map(_._1)

  private def sendToChat(sessionId: SessionId, text: String): Seq[OutputMessage] =
    sessions(sessionId).toSeq.map(ToUser(_, text))

  private def modifySessionUsers(sessionId: SessionId, userId: UserId, addOrRemove: UserId => Set[UserId]): AppState =
    this.copy(sessions = sessions + (sessionId -> addOrRemove(userId)))

  private def addUserToSession(sessionId: SessionId, userId: UserId): AppState =
    modifySessionUsers(sessionId, userId, sessions(sessionId).incl)

  private def removeUserFromSession(sessionId: SessionId, userId: UserId): AppState =
    modifySessionUsers(sessionId, userId, sessions(sessionId).excl)

  private def disconnectUser(userId: UserId): (AppState, Seq[OutputMessage]) = {
    val nextState = getUserSession(userId).map(removeUserFromSession(_, userId)).getOrElse(this)
    nextState -> Seq(ToUsers(nextState.allUsers, s"User $userId has left"))
  }

}

object AppState {
  lazy val empty: AppState = AppState(sessions = Map.empty)

  lazy val singleSession: AppState =
    AppState(sessions = Map(SessionId("f3447dcb-6536-4aab-b2cf-068602b39d64") -> Set.empty))

  lazy val test: AppState =
    AppState(sessions = Map(SessionId() -> Set(UserId(), UserId()), SessionId() -> Set(UserId(), UserId())))

}
