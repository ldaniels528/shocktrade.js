package com.ldaniels528.javascript.angularjs

import scala.scalajs.js

/**
 * Marker Trait for an Angular.js Scope
 * @author lawrence.daniels@gmail.com
 */
trait Scope extends biz.enef.angulate.Scope {

  def dynamic = this.asInstanceOf[js.Dynamic]

}