package com.ldaniels528.javascript.angularjs

import scala.scalajs.js

/**
 * Angular.js Configuration
 * @author lawrence.daniels@gmail.com
 */
trait AngularConfig extends js.Object {
  var strictDi: Boolean = false

}

/**
 * Angular.js Configuration Singleton
 * @author lawrence.daniels@gmail.com
 */
object AngularConfig {

  def apply(strictDi: Boolean = false): AngularConfig = {
    val config = new js.Object().asInstanceOf[AngularConfig]
    config.strictDi = strictDi
    config
  }

}