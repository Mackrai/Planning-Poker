package io

import io.ppoker.core.HubManager
import zio.RIO

package object ppoker {
  type AppRIO[A] = RIO[AppEnv, A]

  type AppEnv = HubManager
}
