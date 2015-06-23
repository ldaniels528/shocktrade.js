package com.ldaniels528.javascript.angularjs.extensions

import scala.scalajs.js

/**
 * Angular.js Toaster
 * @author lawrence.daniels@gmail.com
 */
trait Toaster extends js.Object {

  def pop(`type`: String, title: String, message: String): Unit = js.native

  def pop(`type`: String, title: String, message: String, delay: Long, format: String): Unit = js.native

}

/**
 * Angular.js Toaster Singleton
 * @author lawrence.daniels@gmail.com
 */
object Toaster {

  /**
   * Toaster Enhancements
   * @param toaster the given Toaster service instance
   */
  final implicit class ToasterEnhancements(val toaster: Toaster) extends AnyVal {

    def info(title: String, message: String = null) = toaster.pop(`type` = "info", title, message)

    def danger(title: String, message: String = null) = toaster.pop(`type` = "danger", title, message)

    def error(title: String, message: String = null) = toaster.pop(`type` = "error", title, message)

    def success(title: String, message: String = null) = toaster.pop(`type` = "success", title, message)

    def success(title: String, message: String, delay: Long, format: String) = toaster.pop(`type` = "success", title, message, delay, format)

  }

}