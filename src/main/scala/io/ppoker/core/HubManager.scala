package io.ppoker.core

import io.ppoker.models.UserId
import zio._
import zio.stm._

trait HubManager {
  def subscribe(topic: String, userId: UserId): Task[TDequeue[OutputMessage]]

  def unsubscribe(topic: String, userId: UserId): UIO[Unit]

  def hub(topic: String): Task[THub[OutputMessage]]

  def getHubs: Task[List[String]]

  def getSubscribers(topic: String): Task[List[UserId]]
}

case class HubManagerImpl(hubs: TMap[String, THub[OutputMessage]], subscribers: TMap[String, List[UserId]]) extends HubManager {
  override def subscribe(topic: String, userId: UserId): Task[TDequeue[OutputMessage]] =
    getOrCreateHub(topic)
      .flatMap(_.subscribe)
      .tap(_ => subscribers.updateWith(topic)(_.map(_ :+ userId).orElse(Some(List(userId)))))
      .commit

  override def unsubscribe(topic: String, userId: UserId): UIO[Unit] =
    getOrCreateHub(topic)
      .tap(_ => subscribers.updateWith(topic)(_.map(_.filter(_ != userId)))) // remove hub from hubs TMap if last subscriber unsubscribed?
      .unit
      .commit

  override def hub(topic: String): Task[THub[OutputMessage]] =
    getOrCreateHub(topic).commit

  override def getHubs: Task[List[String]] =
    hubs.keys.commit

  override def getSubscribers(topic: String): Task[List[UserId]] =
    subscribers.get(topic).map(_.toList.flatten).commit

  private def getOrCreateHub(topic: String): USTM[THub[OutputMessage]] =
    hubs.get(topic).flatMap {
      case Some(hub) => STM.succeed(hub)
      case None      => THub.unbounded[OutputMessage].tap(hubs.put(topic, _))
    }
}

object HubManager {
  val live: ZLayer[Any, Nothing, HubManagerImpl] =
    ZLayer {
      (for {
        hubs        <- TMap.empty[String, THub[OutputMessage]]
        subscribers <- TMap.empty[String, List[UserId]]
      } yield HubManagerImpl(hubs, subscribers)).commit
    }
}
