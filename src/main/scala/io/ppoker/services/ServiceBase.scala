package io.ppoker.services

import doobie.Update0
import doobie.util.fragment.Fragment

import scala.reflect.runtime.universe._

abstract class ServiceBase[T: TypeTag] {
  val table: String = typeOf[T].getClass.getSimpleName

  def insert(entity: T): Update0 = {
    val (keys, values) = getFormattedKeysAndValues(entity)
    Fragment.const(s"insert into \"$table\" ($keys) values ($values)").update
  }

  private def getFormattedKeysAndValues(obj: T): (String, String) = {
    val map =
      getFieldName.map { fieldName =>
        fieldName -> getFieldValue(obj, fieldName).toString
      }.toMap.map { case (k, v) => s"\"$k\"" -> s"\'$v\'" }

    map.keys.mkString(", ") -> map.values.mkString(", ")
  }

  private def getFieldName: List[String] =
    typeOf[T].decls.collect {
      case m: MethodSymbol if m.isCaseAccessor => m
    }.toList.map(_.name.toString)

  private def getFieldValue(obj: T, fieldName: String): AnyRef = {
    val value = obj.getClass.getDeclaredField(fieldName)
    value.setAccessible(true)
    value.get(obj)
  }
}
