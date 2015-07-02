package com.ldaniels528.scalascript.core

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.UndefOr

/**
 * Represents an Angular.js Http Promise
 */
trait HttpPromise[T] extends js.Object {

  def success(callback: js.Function): HttpPromise[T] = js.native

  def success(callback: js.Function1[js.Any, Unit]): HttpPromise[T] = js.native

  def success(callback: js.Function2[js.Any, Int, Unit]): HttpPromise[T] = js.native

  def success(callback: js.Function3[js.Any, js.Any, Int, Unit]): HttpPromise[T] = js.native

  def success(callback: js.Function4[js.Any, Int, js.Any, js.Any, Unit]): HttpPromise[T] = js.native

  def success(callback: js.Function5[js.Any, Int, js.Any, js.Any, js.Any, Unit]): HttpPromise[T] = js.native

  def error(callback: js.Function): HttpPromise[T] = js.native

  def error(callback: js.Function1[js.Any, Unit]): HttpPromise[T] = js.native

  def error(callback: js.Function2[js.Any, Int, Unit]): HttpPromise[T] = js.native

  def error(callback: js.Function3[js.Any, js.Any, Int, Unit]): HttpPromise[T] = js.native

  def error(callback: js.Function4[js.Any, Int, js.Any, js.Any, Unit]): HttpPromise[T] = js.native

  def error(callback: js.Function5[js.Any, Int, js.Any, js.Any, UndefOr[String], Unit]): HttpPromise[T] = js.native

  var `then`: js.Function3[js.Function, js.Function, js.Function, HttpPromise[T]] = js.native
  //var `then`: js.Function3[js.Function1[T,Unit],js.Function,js.Function,Unit] = js.native
}

object HttpPromise {

  case class HttpException(status: HttpStatus, message: String) extends Exception

  implicit def promise2future[T](promise: HttpPromise[T]): Future[T] = {
    val p = concurrent.Promise[T]()

    def onSuccess(data: js.Any): Unit = p.success(data.asInstanceOf[T])

    def onError(data: js.Any, status: Int, config: js.Any, headers: js.Any, statusText: UndefOr[String]) {
      p failure HttpException(status, statusText getOrElse s"Failed to process HTTP request: '$data'")
    }

    promise.success(onSuccess _).error(onError _)
    p.future
  }

}
