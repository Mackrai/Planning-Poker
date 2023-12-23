package io.ppoker.core

import zio._
import zio.stm._

trait HubManager {
  def subscribe(topic: String): Task[TDequeue[OutputMessage]]

  def hub(topic: String): Task[THub[OutputMessage]]
}

case class HubManagerImpl(hubs: TMap[String, THub[OutputMessage]]) extends HubManager {
  override def subscribe(topic: String): Task[TDequeue[OutputMessage]] =
    getHub(topic).flatMap(_.subscribe).commit

  private def getHub(topic: String): USTM[THub[OutputMessage]] =
    hubs.get(topic).flatMap {
      case Some(hub) => STM.succeed(hub)
      case None => THub.unbounded[OutputMessage].tap(hubs.put(topic, _))
    }

  override def hub(topic: String): Task[THub[OutputMessage]] =
    getHub(topic).commit
}

object HubManager {
  val live: ZLayer[Any, Nothing, HubManagerImpl] =
    ZLayer {
      TMap.empty[String, THub[OutputMessage]].commit.map { hubs =>
        HubManagerImpl(hubs)
      }
    }
}
