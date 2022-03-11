package models

// Возможно с Enum-ом нафиг не надо заморачиваться и просто сделать какой-нибудь object Constants
object Role extends Enumeration {
  type Role = Value

  val Host     = "Host"
  val Player   = "Player"
  val Observer = "Observer"
}
