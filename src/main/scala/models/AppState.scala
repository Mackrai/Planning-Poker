package models

case class AppState(sessions: Map[SessionId, Set[UserId]]) {

  def processInputMessage(inputMessage: InputMessage): (AppState, Seq[OutputMessage]) = {
    // for debug
    val testMessage = Seq(ToUsers(allUsers, "Test message"))

    inputMessage match {
      case GlobalMessage(text) =>
        this -> allUsers.map(ToUser(_, text))

      case ChatMessage(fromUser, text) =>
        this -> getUserSession(fromUser).map(sendToChat(_, text)).toSeq.flatten

      case Join(sessionId, userId) =>
        val nextState = addUserToSession(sessionId, userId)
        nextState -> Seq(ToUsers(allUsers, s"User $userId has joined"))

      case Leave(userId) =>
        val nextState = getUserSession(userId).map(removeUserFromSession(_, userId)).getOrElse(this)
        nextState -> Seq(ToUsers(allUsers, s"User $userId has left"))

      case Help() =>
        this -> testMessage

      case Disconnect() =>
        this -> testMessage
    }
  }

  def sendToChat(sessionId: SessionId, text: String): Seq[OutputMessage] =
    sessions(sessionId).toSeq.map(ToUser(_, text))

  def addUserToSession(sessionId: SessionId, userId: UserId): AppState =
    modifySessionUsers(sessionId, userId, sessions(sessionId).incl)

  def removeUserFromSession(sessionId: SessionId, userId: UserId): AppState =
    modifySessionUsers(sessionId, userId, sessions(sessionId).excl)

  private def allUsers: Seq[UserId] = sessions.values.toSeq.flatMap(_.toSeq)

  private def modifySessionUsers(sessionId: SessionId, userId: UserId, addOrRemove: UserId => Set[UserId]): AppState =
    this.copy(sessions = sessions + (sessionId -> addOrRemove(userId)))

  private def getUserSession(userId: UserId): Option[SessionId] = sessions.find(_._2.contains(userId)).map(_._1)

}

object AppState {
  lazy val empty: AppState = AppState(sessions = Map.empty)

  lazy val test: AppState =
    AppState(sessions = Map(SessionId() -> Set(UserId(), UserId()), SessionId() -> Set(UserId(), UserId())))

}
