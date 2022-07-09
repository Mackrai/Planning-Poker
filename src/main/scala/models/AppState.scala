package models

case class AppState(sessions: Map[SessionId, Set[UserId]]) {

  def processInputMessage(inputMessage: InputMessage): (AppState, OutputMessage) = {
    // for debug
    val testMessage = OutChatMessage("Test message")

    inputMessage match {
      case ChatMessage(message)    =>
        println(ChatMessage(message))
        this -> testMessage
      case Join(sessionId, userId) =>
        println(Join(sessionId, userId))
        this -> testMessage
      case Leave(userId)           =>
        println(Leave(userId))
        this -> testMessage
      case Help()                  =>
        println(Help())
        this -> testMessage
      case Disconnect()            =>
        println(Disconnect())
        this -> testMessage
    }
  }

  def addUserToSession(sessionId: SessionId, userId: UserId): AppState = {
    val updatedSessions = sessions(sessionId) + userId
    this.copy(sessions = sessions + (sessionId -> updatedSessions))
  }

  def removeUserFromSession(sessionId: SessionId, userId: UserId): AppState = {
    val updatedSessions = sessions(sessionId) - userId
    this.copy(sessions = sessions + (sessionId -> updatedSessions))
  }

}

object AppState {
  lazy val empty: AppState = AppState(sessions = Map.empty)

  lazy val test: AppState  =
    AppState(sessions = Map(SessionId() -> Set(UserId(), UserId()), SessionId() -> Set(UserId(), UserId())))

}
