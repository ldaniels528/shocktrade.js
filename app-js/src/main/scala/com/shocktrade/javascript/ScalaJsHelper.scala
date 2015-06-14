package com.shocktrade.javascript

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g}
import scala.util.{Failure, Success, Try}

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
    val fullQuery = if (queryString.nonEmpty) "?" + queryString else queryString
    g.console.log(s"query is $fullQuery")
    fullQuery
  }

  private def encode(text: String) = {
    var s = text
    " !@#$^*()`~\"".toCharArray foreach (c => s = s.replaceAllLiterally(c.toString, f"%%$c%02x"))
    s
  }

  def flatten[T](task: Future[Try[T]]): Future[T] = task flatMap {
    case Success(s) => Future.successful(s)
    case Failure(f) => Future.failed(f)
  }

  ////////////////////////////////////////////////////////////////////////
  //    Validation Functions
  ////////////////////////////////////////////////////////////////////////

  def die[T](message: String): T = throw new IllegalStateException(message)

  def isDefined(obj: js.Dynamic) = obj != null && !js.isUndefined(obj)

  def isDefined(fx: js.Function) = fx != null && !js.isUndefined(fx)

  def isFalse(obj: js.Dynamic) = !isTrue(obj)

  def isTrue(obj: js.Dynamic) = isDefined(obj) && obj.as[Boolean]

  def required(name: String, value: String) = if (value == null || value.trim.isEmpty) die(s"Required property '$name' is missing")

  def required(name: String, value: js.Dynamic) = if (!isDefined(value)) die(s"Required property '$name' is missing")

  ////////////////////////////////////////////////////////////////////////
  //    Implicit Defintions and Classes
  ////////////////////////////////////////////////////////////////////////

  implicit def duration2Double(duration: FiniteDuration): Double = duration.toMillis

  implicit def duration2Long(duration: FiniteDuration): Long = duration.toMillis

  implicit def duration2Int(duration: FiniteDuration): Int = duration.toMillis.toInt

  object Implicits {

    object Risky {

      implicit def valueToOption[T](value: T): Option[T] = Option(value)

    }

  }

  //implicit def jsDynamic2Value[T](obj: js.Dynamic): T = obj.as[T]

  implicit class StringExtensions(val string: String) extends AnyVal {

    def nonBlank: Boolean = string != null && string.trim.nonEmpty

  }

  /**
   * js.Dynamic to Value Extensions
   * @param obj the given [[js.Dynamic object]]
   */
  implicit class JsDynamicExtensionsA(val obj: js.Dynamic) extends AnyVal {

    def ?(label: String) = if (isDefined(obj(label))) obj(label) else null

    def ===[T](value: T): Boolean = {
      if(value == null) false
      else {
        Try(obj.asInstanceOf[T]) match {
          case Success(converted) => converted == value
          case Failure(e) =>
            g.console.log(s"value '$value': ${e.getMessage}")
            false
        }
      }
    }

    def as[T] = if (isDefined(obj)) obj.asInstanceOf[T] else null.asInstanceOf[T]

    def asArray[T] = obj.asInstanceOf[js.Array[T]]

    def OID: String = OID_?.orNull

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
