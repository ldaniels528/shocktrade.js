package com.shocktrade.controlpanel.runtime.ops

import com.shocktrade.controlpanel.runtime.{Evaluatable, RuntimeContext, Scope}

import scala.concurrent.ExecutionContext

/**
  * Define Variable Operation
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class DefineVariableOp(name: String, expression: Evaluatable) extends Evaluatable {

  override def eval(rc: RuntimeContext, scope: Scope)(implicit ec: ExecutionContext) = {
    expression.eval(rc, scope) map { value =>
      scope.setVariable(name, value)
      value
    }
  }

}
