package io.ppoker.services

import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor.Aux
import io.ppoker.models.Auth

case class AuthService[F[_]: MonadCancelThrow](xa: Aux[F, Unit]) extends ServiceBase[Auth] {

  override val table: String = "Auth"

  def register(login: String, password: String): F[Int] = {
    val salt = Auth.randomAlphaNumericString()
    val hash = Auth.hash(password, salt)
    insert(Auth(login, hash, salt)).run.transact(xa)
  }

  def verify(login: String, password: String): F[Boolean] =
    sql"""select * from "Auth" where "login"=$login"""
      .query[Auth]
      .unique
      .transact(xa)
      .map(loginData => loginData.passHash == Auth.hash(password, loginData.salt))

}
