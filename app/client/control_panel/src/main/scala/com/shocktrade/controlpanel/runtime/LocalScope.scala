package com.shocktrade.controlpanel.runtime

import org.scalajs.sjs.OptionHelper._

/**
  * Represents a local scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class LocalScope(parentScope: Scope) extends RootScope {

  override def findFunction(name: String) = {
    super.findFunction(name) ?? parentScope.findFunction(name)
  }

  override def findVariable(name: String) = {
    super.findVariable(name) ?? parentScope.findVariable(name)
  }

}
