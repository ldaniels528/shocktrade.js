package com.ldaniels528.javascript.angularjs

import scala.scalajs.js

/**
 * Angular.js Injector
 * @author lawrence.daniels@gmail.com
 */
trait Injector extends js.Object {

  def get[T](name: String): js.UndefOr[T] = js.native

  def invoke(f: js.Function, self: js.Object = null, locals: js.Object = null): Unit = js.native

}
