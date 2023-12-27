package io

import io.ppoker.configs.AppConfig
import io.ppoker.controllers.ChatRouter
import io.ppoker.core.{HubManager, MessageProcessor}
import zio.RIO

package object ppoker {
  type AppRIO[A] = RIO[AppEnv, A]

  type AppEnv = Any
}
