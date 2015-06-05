package com.ldaniels528.angularjs

import com.greencatsoft.angularjs.injectable

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js

/**
 * Angular.js Toaster
 * @author lawrence.daniels@gmail.com
 */
@injectable("toaster")
trait Toaster extends js.Object {

  def pop(`type`: String, title: String, message: String = null): Unit = js.native

  def pop(`type`: String, title: String, message: String, delay: FiniteDuration, format: String): Unit = js.native

}
