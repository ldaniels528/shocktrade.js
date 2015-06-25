package com.ldaniels528.javascript.angularjs.core

import scala.concurrent.duration.FiniteDuration
import scala.scalajs.js

/**
 * Angular.js Timeout / Interval Services ($timeout, $interval)
 * @author lawrence.daniels@gmail.com
 * @see https://docs.angularjs.org/api/ng/service/$timeout
 */
trait Timeout extends js.Object {

  /**
   * $timeout(() => doSomething())
   */
  def apply(f: js.Function): CancellablePromise = js.native

  /**
   * $timeout(() => doSomething(), 100)
   */
  def apply(f: js.Function, delay: Int): CancellablePromise = js.native

  /**
   * $timeout(() => doSomething(), 100, invokeApply = true)
   */
  def apply(f: js.Function, delay: Int, invokeApply: Boolean): CancellablePromise = js.native

  /**
   * $timeout(() => doSomething(), 5.seconds)
   */
  def apply(f: js.Function, delay: FiniteDuration): CancellablePromise = apply(f, delay.toMillis.toInt)

  /**
   * $timeout(() => doSomething(), 5.seconds, invokeApply = true)
   */
  def apply(f: js.Function, delay: FiniteDuration, invokeApply: Boolean): CancellablePromise = apply(f, delay.toMillis.toInt, invokeApply)

  def cancel(promise: js.UndefOr[CancellablePromise]): Unit = js.native

  def flush(delay: Int = 0): Unit = js.native

  def flush(delay: FiniteDuration): Unit = flush(delay.toMillis.toInt)

  def verifyNoPendingTasks(): Boolean = js.native

}

/**
 * Angular.js Cancellable Promise
 * @author lawrence.daniels@gmail.com
 */
trait CancellablePromise extends js.Object