package io.ppoker.controllers

import io.circe.parser._
import io.ppoker.core.{HubManager, InputMessage, MessageProcessor}
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.ztapir._
import sttp.ws.WebSocketFrame
import zio._
import zio.stream.ZStream

final class ChatRouter(hubManager: HubManager, messageProcessor: MessageProcessor) {
  private val ws: ZServerEndpoint[Any, ZioStreams with WebSockets] =
    Endpoints.wsEndpoint.zServerLogic { case (topic, userId) =>
      ZIO.succeed { in =>
        val out = for {
          isClosed <- Promise.make[Throwable, Unit]
          dequeue  <- hubManager.subscribe(topic, userId)

          fromUser =
            in.tap(f => ZIO.logInfo(f.toString)).collectZIO {
              case WebSocketFrame.Ping(bytes) =>
                ZIO.succeed(WebSocketFrame.Pong(bytes))
              case close @ WebSocketFrame.Close(_, _) =>
                isClosed.succeed(()).as(close)
              case WebSocketFrame.Text(msg, _, _) =>
                for {
                  inMsg   <- ZIO.fromEither(decode[InputMessage](msg))
                  outMsg  <- messageProcessor.processInputMessage(inMsg)
                  publish <- hubManager.hub(topic).flatMap(_.publish(outMsg).commit).unit
                } yield publish
            }.filterNot(_ == ())

          fromTopic = ZStream.fromTQueue(dequeue).filter(_.forUser(userId))
        } yield fromTopic.merge(fromUser).interruptWhen(isClosed)

        ZStream
          .unwrap(ZIO.scoped(out))
          .onError(cause => ZIO.logError(cause.prettyPrint))
          .map(msg => WebSocketFrame.text(msg.toString))
          .ensuring {
            hubManager.unsubscribe(topic, userId)
          }
      }
    }

  private val getSessions: ZServerEndpoint[Any, Any] =
    Endpoints.sessions.zServerLogic { _ =>
      hubManager.getHubs.mapError(_ => new Throwable("err"))
    }

  private val subscribers: ZServerEndpoint[Any, Any] =
    Endpoints.subscribers.zServerLogic { topic =>
      hubManager.getSubscribers(topic).map(_.map(_.raw)).mapError(_ => new Throwable("err"))
    }

  val endpoints = List(ws, getSessions, subscribers)
}

object ChatRouter {
  val live: ZLayer[HubManager with MessageProcessor, Nothing, ChatRouter] =
    ZLayer {
      for {
        hubManager       <- ZIO.service[HubManager]
        messageProcessor <- ZIO.service[MessageProcessor]
      } yield new ChatRouter(hubManager, messageProcessor)
    }
}
