package com.ldaniels528.javascript.angularjs.extensions

import scala.scalajs.js

/**
 * Cookie Store Service (ngCookies)
 * @author lawrence.daniels@gmail.com
 */
trait Cookies extends js.Object {

  def get[T](key: String): js.UndefOr[T] = js.native

  def put[T](key: String, value: T): Unit = js.native

  def remove(key: String): String = js.native

}

/**
 * Cookie Store Singleton
 * @author lawrence.daniels@gmail.com
 */
object Cookies {

  implicit class CookieExtensions(val cookies: Cookies) extends AnyVal {

    def getOrElse[T](key: String, defaultValue: T): T = (cookies.get(key) getOrElse defaultValue).asInstanceOf[T]

  }

}
