package com.shocktrade.javascript.directives

import com.ldaniels528.scalascript.core.{Attributes, JQLite}
import com.ldaniels528.scalascript.{Scope, Directive}
import org.scalajs.dom._

import scala.scalajs.js

/**
 * Escape Directive
 * @author lawrence.daniels@gmail.com
 */
class EscapeDirective() extends Directive {
  override type ScopeType = Scope
  override type ControllerType = js.Dynamic

  override def postLink(scope: ScopeType, elem: JQLite, attrs: Attributes, controller: js.Dynamic): Unit = {
    elem.on("keydown", (evt: KeyboardEvent) => if (evt.keyCode == 27) scope.$apply(attrs("doEscape")))
  }
}