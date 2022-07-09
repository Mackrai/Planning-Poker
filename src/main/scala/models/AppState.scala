package models

case class AppState(sessions: Map[SessionId, Set[UserId]]) {

  def process(inputMessage: InputMessage) = inputMessage match {
    case ChatMessage(message) => ???
    case JoinChat(chatId, userId) => ???
    case LeaveChat(userId) => ???
    case Help() => ???
    case Disconnect() => ???
  }

  private def addToRoom(newUserId: UserId, sessionId: SessionId) =
    sessions.get(sessionId).map(users => users + newUserId)

}

object AppState {

  lazy val empty: AppState = AppState(sessions = Map.empty)
}