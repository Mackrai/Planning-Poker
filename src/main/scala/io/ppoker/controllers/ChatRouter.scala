package io.ppoker.controllers

import io.circe.parser._
import io.circe._
import io.circe.syntax._
import io.ppoker.core.{HubManager, InputMessage, MessageProcessor}
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.ztapir._
import sttp.ws.WebSocketFrame
import zio._
import zio.stream.ZStream

final class ChatRouter(hubManager: HubManager, messageProcessor: MessageProcessor) {
  val ws: ZServerEndpoint[Any, ZioStreams with WebSockets] =
    Endpoints.wsEndpoint.zServerLogic { userId =>
      ZIO.succeed { (in: ZStream[Any, Throwable, WebSocketFrame]) =>
        val out = for {
          isClosed <- Promise.make[Throwable, Unit]

          //TODO get from request
          topic = "hub_1"

          dequeue  <- hubManager.subscribe(topic)

          fromUser =
            in.tap(f => ZIO.logInfo(f.toString)).collectZIO {
              case WebSocketFrame.Ping(bytes) =>
                ZIO.succeed(WebSocketFrame.Pong(bytes))
              case close @ WebSocketFrame.Close(_, _) =>
                isClosed.succeed(()).as(close)
              case WebSocketFrame.Text(msg, _, _) =>
                for {
                  inMsg <- ZIO.fromEither(decode[InputMessage](msg))
                  outMsg = messageProcessor.processInputMessage(inMsg)
                  publish <- hubManager.hub(topic).flatMap(_.publishAll(outMsg).commit).unit
                } yield publish
            }.filterNot(_ == ())

          fromTopic = ZStream.fromTQueue(dequeue).filter(_.forUser(userId))
        } yield fromTopic.merge(fromUser).interruptWhen(isClosed)

        ZStream
          .unwrap(ZIO.scoped(out))
          .onError(cause => ZIO.logError(cause.prettyPrint))
          .map(msg => WebSocketFrame.text(msg.toString))
      }
    }
}

object ChatRouter {
  val live: ZLayer[HubManager with MessageProcessor, Nothing, ChatRouter] =
    ZLayer {
      for {
        hubManager <- ZIO.service[HubManager]
        messageProcessor <- ZIO.service[MessageProcessor]
      } yield new ChatRouter(hubManager, messageProcessor)
    }
}
