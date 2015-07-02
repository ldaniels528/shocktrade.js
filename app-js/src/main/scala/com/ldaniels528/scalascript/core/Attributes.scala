// -   Project: scalajs-scalascript (https://github.com/jokade/scalajs-scalascript)
// Description: API for AngularJS attributes objectes (provided to link/compile functions)
//
// Distributed under the MIT License (see included file LICENSE)
package com.ldaniels528.scalascript.core

import acyclic.file
import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSBracketAccess

trait Attributes extends js.Object {

  @JSBracketAccess
  def apply(name: String) : UndefOr[String] = js.native

  @JSBracketAccess
  def update(name: String, value: String) : Unit = js.native

}
