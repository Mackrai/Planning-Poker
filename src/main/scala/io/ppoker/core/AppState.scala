package io.ppoker.core

import io.ppoker.models.{SessionId, Task, TaskId, UserId}

case class AppState(sessions: Map[SessionId, (Set[UserId], Set[TaskId])]) {

  def processInputMessage(inputMessage: InputMessage): (AppState, Seq[OutputMessage]) =
    inputMessage match {
      case AddTask(sessionId, title, description) =>
        val task = Task(session = sessionId, title = title, description = description)
        //db.insert ?
        val (users, tasks) = sessions(sessionId)
        val nextState = this.copy(sessions + (sessionId -> (users, tasks.incl(task.id))))
        nextState -> Seq()

      case GlobalMessage(text) =>
        this -> allUsers.map(ToUser(_, text))

      case ChatMessage(fromUser, text) =>
        this -> getUserSession(fromUser).map(sendToChat(_, text)).toSeq.flatten

      case Join(userId, sessionId) =>
        getUserSession(userId).collect {
          case userSession if userSession == sessionId =>
            this -> Seq(ToUser(userId, "Already connected to this session"))
        }.getOrElse(addUserToSession(userId, sessionId))

      case Leave(userId) => disconnectUser(userId)

      case Disconnect(userId) => disconnectUser(userId)

      case message => throw new Exception(s"Invalid InputMessage type: [${message.getClass.toString}]")
    }

  def userIsConnected(userId: UserId): Boolean = getUserSession(userId).isEmpty

  private def allUsers: Seq[UserId] = sessions.values.toSeq.flatMap(_._1.toSeq)

  private def getUserSession(userId: UserId): Option[SessionId] = sessions.find(_._2._1.contains(userId)).map(_._1)

  private def sendToChat(sessionId: SessionId, text: String): Seq[OutputMessage] =
    sessions(sessionId)._1.map(ToUser(_, text)).toSeq

  private def addUserToSession(userId: UserId, sessionId: SessionId): (AppState, Seq[OutputMessage]) = {
    val (users, tasks) = sessions(sessionId)
    val nextState = this.copy(sessions + (sessionId -> (users.incl(userId), tasks)))
    nextState -> Seq(ToUsers(nextState.allUsers, s"User $userId has joined"))
  }

  private def removeUserFromSession(userId: UserId, sessionId: SessionId): (AppState, Seq[OutputMessage]) = {
    val (users, tasks) = sessions(sessionId)
    val nextState = this.copy(sessions + (sessionId -> (users.excl(userId), tasks)))
    nextState -> Seq(ToUsers(nextState.allUsers, s"User $userId has left"))
  }

  private def disconnectUser(userId: UserId): (AppState, Seq[OutputMessage]) =
    getUserSession(userId)
      .map(removeUserFromSession(userId, _))
      .getOrElse(this -> Seq(ToUser(userId, "Currently not connected to any session")))

}

object AppState {
  lazy val empty: AppState = AppState(sessions = Map.empty)

  lazy val singleSession: AppState =
    AppState(sessions = Map(SessionId("sId_1") -> (Set.empty, Set.empty)))
}
