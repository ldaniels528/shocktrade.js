package com.ldaniels528.scalascript.extensions

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js

/**
 * Angular.js Toaster
 * @author lawrence.daniels@gmail.com
 * @see https://github.com/jirikavi/AngularJS-Toaster
 */
trait Toaster extends js.Object {

  def pop(`type`: js.Any, title: js.Any, message: js.Any): Unit = js.native

  def pop(`type`: js.Any, title: js.Any, message: js.Any, delay: Long, format: js.Any): Unit = js.native

  def pop(`type`: js.Any, title: js.Any, message: js.Any, delay: FiniteDuration, format: js.Any): Unit = {
    pop(`type`, title, message, delay.toMillis.toInt, format)
  }

}

/**
 * Angular.js Toaster Singleton
 * @author lawrence.daniels@gmail.com
 */
object Toaster {
  val INFO = "info"
  val DANGER = "danger"
  val ERROR = "error"
  val SUCCESS = "success"
  val WARNING = "warning"

  /**
   * Toaster Enhancements
   * @param toaster the given Toaster service instance
   */
  final implicit class ToasterEnhancements(val toaster: Toaster) extends AnyVal {

    def info(title: js.Any, message: js.Any = null) = toaster.pop(`type` = INFO, title, message)

    def info(title: js.Any, message: js.Any, delay: Long, format: js.Any) = toaster.pop(`type` = INFO, title, message, delay, format)

    def info(title: js.Any, message: js.Any, delay: FiniteDuration, format: js.Any) = toaster.pop(`type` = INFO, title, message, delay, format)

    def danger(title: js.Any, message: js.Any = null) = toaster.pop(`type` = DANGER, title, message)

    def error(title: js.Any, message: js.Any = null) = toaster.pop(`type` = ERROR, title, message)

    def success(title: js.Any, message: js.Any = null) = toaster.pop(`type` = SUCCESS, title, message)

    def success(title: js.Any, message: js.Any, delay: Long, format: js.Any) = toaster.pop(`type` = SUCCESS, title, message, delay, format)

    def success(title: js.Any, message: js.Any, delay: FiniteDuration, format: js.Any) = toaster.pop(`type` = SUCCESS, title, message, delay, format)

    def warning(title: js.Any, message: js.Any = null) = toaster.pop(`type` = WARNING, title, message)

    def warning(title: js.Any, message: js.Any, delay: Long, format: js.Any) = toaster.pop(`type` = WARNING, title, message, delay, format)

    def warning(title: js.Any, message: js.Any, delay: FiniteDuration, format: js.Any) = toaster.pop(`type` = WARNING, title, message, delay, format)

  }

}