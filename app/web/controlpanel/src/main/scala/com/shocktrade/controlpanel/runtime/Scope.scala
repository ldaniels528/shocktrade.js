package com.shocktrade.controlpanel.runtime

import com.shocktrade.controlpanel.runtime.functions.Function

/**
  * Represents a Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait Scope {

  def +=(function: Function): Unit

  def ++=(functions: Seq[Function]): Unit

  def findFunction(name: String): Option[Function]

  def findVariable(name: String): Option[TypedValue]

  def setVariable(name: String, value: TypedValue): Unit

}
