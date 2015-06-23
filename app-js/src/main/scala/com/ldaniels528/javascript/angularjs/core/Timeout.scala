package com.ldaniels528.javascript.angularjs.core

import biz.enef.angulate.core.QPromise

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js

/**
 * Angular.js $timeout Service
 * @author lawrence.daniels@gmail.com
 */
trait Timeout extends js.Object {
  
  def apply(f: js.Function, delay: Int = 0, invokeApply: Boolean = true) : QPromise = js.native

  //def apply(f: js.Function, delay: FiniteDuration, invokeApply: Boolean = true) : QPromise = apply(f, delay.toMillis.toInt, invokeApply)
  
}
