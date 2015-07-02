package com.ldaniels528.scalascript.extensions

import scala.scalajs.js

/**
 * Angular.js CookieStore
 * @author lawrence.daniels@gmail.com
 */
trait Sce extends js.Object {

  def trustAsHtml(html: String): String = js.native

}
