package com.shocktrade.controlpanel.runtime

/**
  * Represents a reference to a variable
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class VariableReference(val name: String) extends ValueReference {

  override def eval(rc: RuntimeContext) = {
    rc.findVariable(name) map { variable =>

    }
  }

}
