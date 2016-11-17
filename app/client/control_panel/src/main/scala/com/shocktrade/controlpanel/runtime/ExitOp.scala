package com.shocktrade.controlpanel.runtime

/**
  * Exit Function
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ExitOp() extends Evaluatable {

  override def eval(rc: RuntimeContext) = rc.halt()

}