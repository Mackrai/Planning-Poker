package io.ppoker.controllers

import org.http4s.HttpRoutes

trait Router[F[_]] {
  val routes: HttpRoutes[F]
}
