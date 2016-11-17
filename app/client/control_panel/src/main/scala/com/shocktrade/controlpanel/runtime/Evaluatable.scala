package com.shocktrade.controlpanel.runtime

/**
  * Represents an evaluatable value or expression
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait Evaluatable {

  def eval(rc: RuntimeContext): Option[Any]

}