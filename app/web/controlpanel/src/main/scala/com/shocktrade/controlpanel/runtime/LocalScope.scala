package com.shocktrade.controlpanel.runtime

import io.scalajs.util.OptionHelper._

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
