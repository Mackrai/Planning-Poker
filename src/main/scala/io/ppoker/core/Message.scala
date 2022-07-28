package io.ppoker.core

trait Message {
  val messageType: MessageType
}

trait MessageType
case object Incoming extends MessageType
case object Outgoing extends MessageType
