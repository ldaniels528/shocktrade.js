package com.ldaniels528.javascript.angularjs.core

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.runtime._

/**
 * Angular.js Q Promise
 * @author lawrence.daniels@gmail.com
 */
trait QPromise[T] extends js.Object {

  def `then`(successCallback: js.Function1[T, T]): this.type = js.native

  def `then`(successCallback: js.Function1[T, T], errorCallback: js.Function1[T, Unit]): this.type = js.native

  def `then`(successCallback: js.Function1[T, T], errorCallback: js.Function1[T, Unit], notifyCallback: js.Function1[T, Unit]): this.type = js.native

  def `catch`(errorCallback: js.Function1[T, Unit]): this.type = js.native

  def `finally`(callback: js.Function1[T, Unit]): Unit = js.native

}

/**
 * Angular Q Promise Singleton
 * @author lawrence.daniels@gmail.com
 */
object QPromise {

  implicit def qPromise2Future[T](promise: QPromise[T]): Future[T] = {
    val p = concurrent.Promise[T]()

    def onSuccess(data: T): T = {
      p.success(data)
      data
    }

    def onError(error: T): Unit = p.failure(wrapJavaScriptException(error))

    promise.`then`(onSuccess _).`catch`(onError _)
    p.future
  }

}

