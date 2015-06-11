package com.shocktrade.javascript

import scala.language.implicitConversions
import scala.scalajs.js

/**
 * Service Support
 * @author lawrence.daniels@gmail.com
 */
object ScalaJsHelper {

  ////////////////////////////////////////////////////////////////////////
  //    Convenience Functions
  ////////////////////////////////////////////////////////////////////////

  def emptyArray[T] = js.Array[T]()

  def params(values: (String, Any)*): String = {
    val queryString = values map { case (k, v) => s"$k=${encode(String.valueOf(v))}" } mkString "&"
    if (queryString.nonEmpty) "?" + queryString else queryString
  }

  private def encode(text: String) = {
    var s = text
    " !@#$^*()`~\"".toCharArray foreach (c => s = s.replaceAllLiterally(c.toString, f"%%$c%02x"))
    s
  }

  ////////////////////////////////////////////////////////////////////////
  //    Validation Functions
  ////////////////////////////////////////////////////////////////////////

  def die[T](message: String): T = throw new IllegalStateException(message)

  def isDefined(obj: js.Dynamic) = obj != null && !js.isUndefined(obj)

  def isDefined(fx: js.Function) = fx != null && !js.isUndefined(fx)

  def required(name: String, value: String) = {
    if (value == null || value.trim.isEmpty) die(s"Required property '$name' is missing")
  }

  def required(name: String, value: js.Dynamic) = {
    if (!isDefined(value)) die(s"Required property '$name' is missing")
  }

  ////////////////////////////////////////////////////////////////////////
  //    Implicit Defintions and Classes
  ////////////////////////////////////////////////////////////////////////

  //implicit def jsDynamic2Value[T](obj: js.Dynamic): T = obj.as[T]

  implicit class StringExtensions(val string: String) extends AnyVal {

    def nonBlank: Boolean = string != null && string.trim.nonEmpty

  }

  /**
   * js.Dynamic to Value Extensions
   * @param obj the given [[js.Dynamic object]]
   */
  implicit class JsDynamicExtensionsA(val obj: js.Dynamic) extends AnyVal {

    def ===[T](value: T): Boolean = obj.asInstanceOf[T] == value

    def as[T] = if(isDefined(obj)) obj.asInstanceOf[T] else null.asInstanceOf[T]

    def asArray[T] = obj.asInstanceOf[js.Array[T]]

    def OID: String = if (isDefined(obj._id)) obj._id.$oid.asInstanceOf[String] else null

    def OID_? : Option[String] = if (isDefined(obj._id)) Option(obj._id.$oid.asInstanceOf[String]) else None

  }

  /**
   * Value to js.Dynamic Extensions
   * @param value the given [[String value]]
   */
  implicit class JsDynamicExtensionsB(val value: String) extends AnyVal {

    def ===(obj: js.Dynamic): Boolean = value == obj.asInstanceOf[String]

  }

}
