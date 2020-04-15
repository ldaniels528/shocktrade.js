package com.shocktrade.remote

import scala.scalajs.js

package object loader {

  def die[A](message:String): A = throw js.JavaScriptException(message)

}
