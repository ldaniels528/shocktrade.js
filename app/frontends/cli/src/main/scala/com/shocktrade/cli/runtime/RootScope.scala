package com.shocktrade.cli.runtime

import com.shocktrade.cli.runtime.functions.Function

import scala.collection.mutable

/**
  * Represents a root scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class RootScope() extends Scope {
  private val functions = mutable.Map[String, Function]()
  private val variables = mutable.Map[String, TypedValue]()

  override def +=(function: Function) = functions(function.name) = function

  override def ++=(functions: Seq[Function]) = this.functions ++= functions.map(fx => fx.name -> fx)

  override def findFunction(name: String) = functions.get(name)

  override def findVariable(name: String) = variables.get(name)

  override def setVariable(name: String, value: TypedValue) = variables(name) = value

  override def toString = s"variables => $variables, functions => $functions"

}
