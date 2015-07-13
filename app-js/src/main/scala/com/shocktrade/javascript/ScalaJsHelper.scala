package com.shocktrade.javascript

import org.scalajs.dom.console

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.util.{Failure, Success, Try}

/**
 * Scala.js Convenience Helper Functions
 * @author lawrence.daniels@gmail.com
 */
object ScalaJsHelper {

  ////////////////////////////////////////////////////////////////////////
  //    Convenience Functions
  ////////////////////////////////////////////////////////////////////////

  @inline
  def emptyArray[T] = js.Array[T]()

  @inline
  def makeNew[T] = new js.Object().asInstanceOf[T]

  def params(values: (String, Any)*): String = {
    val queryString = values map { case (k, v) => s"$k=${g.encodeURI(String.valueOf(v))}" } mkString "&"
    val fullQuery = if (queryString.nonEmpty) "?" + queryString else queryString
    console.log(s"query is $fullQuery")
    fullQuery
  }

  ////////////////////////////////////////////////////////////////////////
  //    Validation Functions
  ////////////////////////////////////////////////////////////////////////

  @inline
  def die[T](message: String): T = throw new IllegalStateException(message)

  @inline
  def isDefined(obj: js.Any) = obj != null && !js.isUndefined(obj)

  @inline
  def isDefined(fx: js.Function) = fx != null && !js.isUndefined(fx)

  @inline
  def isFalse(obj: js.Dynamic) = !isTrue(obj)

  @inline
  def isTrue(obj: js.Dynamic) = isDefined(obj) && obj.as[Boolean]

  @inline
  def required(name: String, value: String) = if (value == null || value.trim.isEmpty) die(s"Required property '$name' is missing")

  @inline
  def required(name: String, value: js.Any) = if (!isDefined(value)) die(s"Required property '$name' is missing")

  @inline
  def required[T](name: String, value: js.Array[T], allowEmpty: Boolean = false) = {
    if (value == null || (allowEmpty && value.isEmpty)) die(s"Required property '$name' is missing")
  }

  ////////////////////////////////////////////////////////////////////////
  //    Implicit Definitions and Classes
  ////////////////////////////////////////////////////////////////////////

  /**
   * js.Dynamic to Value Extensions
   * @param obj the given [[js.Dynamic object]]
   */
  implicit class JsAnyExtensions(val obj: js.Any) extends AnyVal {

    def ===[T](value: T): Boolean = {
      if (value == null) !isDefined(obj)
      else {
        Try(obj.asInstanceOf[T]) match {
          case Success(converted) => converted == value
          case Failure(e) =>
            console.log(s"JsDynamicExtensionsA: value '$value': ${e.getMessage}")
            false
        }
      }
    }

    @inline def as[T] = if (isDefined(obj)) obj.asInstanceOf[T] else null.asInstanceOf[T]

    @inline def asOpt[T] = obj.asInstanceOf[js.UndefOr[T]].toOption

    @inline def asArray[T] = obj.asInstanceOf[js.Array[T]]

    @inline def isTrue = isDefined(obj) && Try(obj.asInstanceOf[Boolean]).toOption.contains(true)

    @inline def toUndefOr[T]: js.UndefOr[T] = obj.asInstanceOf[js.UndefOr[T]]

  }

  implicit class JsArrayExtensions[A](val array: js.Array[A]) extends AnyVal {

    @inline def removeAll(): Unit = array.remove(0, array.length)

  }

  /**
   * js.Object Extensions
   * @param obj the given [[js.Dynamic object]]
   */
  implicit class JsDynamicExtensionsA(val obj: js.Dynamic) extends AnyVal {

    @inline def OID_? : Option[String] = if (isDefined(obj._id)) Option(obj._id.$oid.asInstanceOf[String]) else None

  }

  /**
   * Value to js.Dynamic Extensions
   * @param value the given [[String value]]
   */
  implicit class JsDynamicExtensionsB(val value: String) extends AnyVal {

    def ===(obj: js.Dynamic): Boolean = if (!isDefined(obj)) value == null
    else {
      Try(obj.asInstanceOf[String]) match {
        case Success(converted) => converted == value
        case Failure(e) =>
          console.log(s"JsDynamicExtensionsB: value '$value': ${e.getMessage}")
          false
      }
    }
  }

  /**
   * js.Object Extensions
   * @param obj the given [[js.Dynamic object]]
   */
  implicit class JsObjectExtensions(val obj: js.Object) extends AnyVal {

    @inline def dynamic = obj.asInstanceOf[js.Dynamic]

    @inline
    def OID_? : Option[String] = {
      val dyn = obj.asInstanceOf[js.Dynamic]
      if (isDefined(dyn._id)) Option(dyn._id.$oid.asInstanceOf[String]) else None
    }

  }

  implicit class OptionExtensions[T](val optA: Option[T]) extends AnyVal {

    @inline def ??(optB: => Option[T]): Option[T] = if (optA.isDefined) optA else optB

  }

  /**
   * Convenience methods for strings
   * @param string the given host string
   */
  implicit class StringExtensions(val string: String) extends AnyVal {

    @inline
    def indexOfOpt(substring: String): Option[Int] = Option(string).map(_.indexOf(substring)) flatMap {
      case -1 => None
      case index => Some(index)
    }

    @inline def isBlank: Boolean = string == null && string.trim.isEmpty

    @inline def nonBlank: Boolean = string != null && string.trim.nonEmpty

    @inline def isValidEmail: Boolean = !string.matches( """/^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i""")

  }

}
