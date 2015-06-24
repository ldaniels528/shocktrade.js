package com.ldaniels528.javascript.angularjs.core

import scala.scalajs.js

/**
 * Angular.js $timeout Service
 * @author lawrence.daniels@gmail.com
 */
trait Timeout extends js.Object {

  def apply(f: js.Function, delay: Int = 0, invokeApply: Boolean = true): CancellablePromise = js.native

  //def apply(f: js.Function, delay: FiniteDuration, invokeApply: Boolean = true) : QPromise = apply(f, delay.toMillis.toInt, invokeApply)

}

trait CancellablePromise extends js.Object {

  def cancel() = js.native

}