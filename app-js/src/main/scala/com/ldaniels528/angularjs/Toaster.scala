package com.ldaniels528.angularjs

import scala.scalajs.js

/**
 * Angular.js Toaster
 * @author lawrence.daniels@gmail.com
 */
trait Toaster extends js.Object {

  def pop(`type`: String, title: String, message: String = null): Unit = js.native

  def pop(`type`: String, title: String, message: String, delay: Long, format: String): Unit = js.native

}
