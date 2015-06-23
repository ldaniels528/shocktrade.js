package com.ldaniels528.javascript.angularjs.extensions

import scala.scalajs.js

/**
 * Cookie Store Service
 * @author lawrence.daniels@gmail.com
 */
trait CookieStore extends js.Object {

  def get[T](key: String): js.UndefOr[T] = js.native

  def put[T](key: String, value: T): Unit = js.native

  def remove(key: String): String = js.native

}

/**
 * Cookie Store Singleton
 * @author lawrence.daniels@gmail.com
 */
object CookieStore {

  implicit class CookieStoreExtensions(val cookieStore: CookieStore) extends AnyVal {

    def getOrElse[T](key: String, defaultValue: js.Any): T = (cookieStore.get(key) getOrElse defaultValue).asInstanceOf[T]

  }

}
