package com.ldaniels528.scalascript.core

import scala.language.implicitConversions
import scala.scalajs.js

/**
 * Angular.js Q Service ($q)
 * @author lawrence.daniels@gmail.com
 */
trait Q extends js.Object {

  def defer[T](): QDefer[T] = js.native

}
