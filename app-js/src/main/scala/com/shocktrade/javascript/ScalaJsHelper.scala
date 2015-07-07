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
  def required(name: String, value: js.Dynamic) = if (!isDefined(value)) die(s"Required property '$name' is missing")

  @inline
  def required[T](name: String, value: js.Array[T], allowEmpty: Boolean = false) = {
    if (value == null || (allowEmpty && value.isEmpty)) die(s"Required property '$name' is missing")
  }

  ////////////////////////////////////////////////////////////////////////
  //    Implicit Definitions and Classes
  ////////////////////////////////////////////////////////////////////////

  /**
   * Convenience method for transforming Scala Sequences into js.Arrays
   * @param items the given sequence of objects
   * @tparam T the parameter type
   */
  implicit class JsArrayExtensionsA[T](val items: Seq[T]) extends AnyVal {

    def toJsArray: js.Array[T] = items.asInstanceOf[js.Array[T]]

  }

  /**
   * Convenience method for transforming Scala Sequences into js.Arrays
   * @param items the given sequence of objects
   * @tparam T the parameter type
   */
  implicit class JsArrayExtensionsB[T](val items: Array[T]) extends AnyVal {

    def toJsArray: js.Array[T] = items.asInstanceOf[js.Array[T]]

  }

  /**
   * js.Dynamic to Value Extensions
   * @param obj the given [[js.Dynamic object]]
   */
  implicit class JsDynamicExtensionsA(val obj: js.Any) extends AnyVal {

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

    @inline
    def as[T] = if (isDefined(obj)) obj.asInstanceOf[T] else null.asInstanceOf[T]

    @inline
    def asOpt[T] = obj.asInstanceOf[js.UndefOr[T]].toOption

    @inline
    def asArray[T] = obj.asInstanceOf[js.Array[T]]

    @inline
    def isTrue = isDefined(obj) && Try(obj.asInstanceOf[Boolean]).toOption.contains(true)

    @inline
    def OID: String = OID_?.orNull

    @inline
    def OID_? : Option[String] = for {
      obj <- obj.asInstanceOf[js.UndefOr[js.Dynamic]].toOption
      _id <- obj._id.asInstanceOf[js.UndefOr[js.Dynamic]].toOption
      $oid <- _id.$oid.asInstanceOf[js.UndefOr[String]].toOption
    } yield $oid

    @inline
    def toUndefOr[T]: js.UndefOr[T] = obj.asInstanceOf[js.UndefOr[T]]

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

  implicit class OptionExtensions[T](val optA: Option[T]) extends AnyVal {

    def ??(optB: => Option[T]): Option[T] = if (optA.isDefined) optA else optB

  }

  /**
   * Convenience methods for strings
   * @param string the given host string
   */
  implicit class StringExtensions(val string: String) extends AnyVal {

    def isBlank: Boolean = string == null && string.trim.isEmpty

    def nonBlank: Boolean = string != null && string.trim.nonEmpty

    def isValidEmail: Boolean = !string.matches( """/^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i""")

  }

}
