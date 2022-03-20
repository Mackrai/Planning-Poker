package models

case class AppState(userSessions: Map[UserId, SessionId], sessionUsers: Map[SessionId, Set[UserId]]) {

  def process(inputMessage: InputMessage) = inputMessage match {
    case ChatMessage(message) => ???
    case JoinChat(chatId, userId) => ???
    case LeaveChat(userId) => ???
    case Help() => ???
    case Disconnect() => ???
  }

  private def addToRoom(newUserId: UserId, sessionId: SessionId) =
    sessionUsers.get(sessionId).map(users => users + newUserId)

}

object AppState {

  val empty: AppState = AppState(userSessions = Map.empty, sessionUsers = Map.empty)
}