package io.ppoker.models

import java.math.BigInteger
import java.security.MessageDigest
import scala.collection.mutable

case class Auth(login: String, passHash: String, salt: String)

object Auth {
  def hash(string: String, salt: String): String = {
    "%032x".format(new BigInteger(1,
      MessageDigest
        .getInstance("SHA-256")
        .digest(string.getBytes("UTF-8") ++ salt.getBytes)
    ))
  }

  def randomAlphaNumericString(length: Int = 8): String = {
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    randomStringFromCharList(length, chars)
  }

  def randomStringFromCharList(length: Int, chars: Seq[Char]): String = {
    val sb = new mutable.StringBuilder
    for (_ <- 1 to length) {
      val randomNum = util.Random.nextInt(chars.length)
      sb.append(chars(randomNum))
    }
    sb.toString
  }
}

