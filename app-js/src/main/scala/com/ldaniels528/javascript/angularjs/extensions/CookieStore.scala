package com.ldaniels528.javascript.angularjs.extensions

import scala.scalajs.js

/**
 * CookieStore Service (requires: ngCookies) - Provides read/write access to browser's cookies.
 * @author lawrence.daniels@gmail.com
 * @see [[https://docs.angularjs.org/api/ngCookies/service/$cookies]]
 */
class CookieStore extends js.Object {

  /**
   * Returns the value of given cookie key
   * @param key the given cookie key
   * @tparam T the cookie value's type
   * @return the value of given cookie key
   */
  def get[T](key: String): js.UndefOr[T] = js.native

  /**
   * Sets a value for given cookie key
   * @param key the given cookie key
   * @param value the value of given cookie key
   * @tparam T the cookie value's type
   */
  def put[T](key: String, value: T): Unit = js.native

  /**
   * Removes given cookie
   * @param key the given cookie key
   * @return
   */
  def remove(key: String): js.Any = js.native

}

/**
 * CookieStore Service Singleton
 * @author lawrence.daniels@gmail.com
 */
object CookieStore {

  /**
   * Cookie Extensions
   * @param cookies the given cookies instance
   */
  implicit class CookieStroreExtensions(val cookies: Cookies) extends AnyVal {

    @inline
    def getOrElse[T](key: String, defaultValue: T): T = (cookies.get(key) getOrElse defaultValue).asInstanceOf[T]

  }

}
