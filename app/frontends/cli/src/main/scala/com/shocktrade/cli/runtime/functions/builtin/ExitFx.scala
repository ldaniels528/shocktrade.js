package com.shocktrade.cli.runtime.functions
package builtin

import com.shocktrade.cli.runtime.{Null, RuntimeContext, Scope}

import scala.concurrent.{ExecutionContext, Future}

/**
  * exit() Function
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ExitFx() extends Function {

  override def name = "exit"

  override def params = Nil

  override def eval(rc: RuntimeContext, scope: Scope)(implicit ec: ExecutionContext) = {
    rc.halt()
    Future.successful(Null)
  }

}