package com.ldaniels528.javascript.angularjs.core

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.UndefOr

/**
 * Angular.js $http Service
 * @author lawrence.daniels@gmail.com
 */
trait Http extends js.Object {

  def apply[T](config: HttpConfig): HttpPromise[T] = js.native

  def get[T](url: String): HttpPromise[T] = js.native

  def get[T](url: String, config: HttpConfig): HttpPromise[T] = js.native

  def post[T](url: String): HttpPromise[T] = js.native

  def post[T](url: String, data: js.Any): HttpPromise[T] = js.native

  def post[T](url: String, data: js.Any, config: HttpConfig): HttpPromise[T] = js.native

  def jsonp[T](url: String): HttpPromise[T] = js.native

  def jsonp[T](url: String, config: HttpConfig): HttpPromise[T] = js.native

  def put[T](url: String): HttpPromise[T] = js.native

  def put[T](url: String, data: js.Any): HttpPromise[T] = js.native

  def put[T](url: String, data: js.Any, config: HttpConfig): HttpPromise[T] = js.native

  def delete[T](url: String): HttpPromise[T] = js.native

  def delete[T](url: String, data: js.Any): HttpPromise[T] = js.native

  def delete[T](url: String, data: js.Any, config: HttpConfig): HttpPromise[T] = js.native

  def head[T](url: String): HttpPromise[T] = js.native

  def head[T](url: String, config: HttpConfig): HttpPromise[T] = js.native

}

class HttpError(msg: String, val status: Int) extends RuntimeException(msg)

trait HttpProvider extends js.Object {

  def useApplyAsync(): HttpProvider = js.native

  def useApplyAsync(async: Boolean): HttpProvider = js.native

  def defaults: HttpDefaults = js.native

  def interceptors: js.Array[js.Any] = js.native

}

trait HttpDefaults extends js.Object {
  var cache: js.Dynamic = js.native
  var xsrfCookieName: String = js.native
  var xsrfHeaderName: String = js.native
  var headers: js.Dynamic = js.native
  var withCredentials: Boolean = js.native
}

trait HttpResponse extends js.Object {
  def status: Int = js.native

  def statusText: String = js.native

  def data: js.Any = js.native

  def config: HttpConfig = js.native

  def headers(name: String): String = js.native
}
