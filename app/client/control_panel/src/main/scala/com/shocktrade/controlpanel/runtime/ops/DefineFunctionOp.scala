package com.shocktrade.controlpanel.runtime.ops

import com.shocktrade.controlpanel.runtime.functions.UserDefinedFunction
import com.shocktrade.controlpanel.runtime.{Evaluatable, RuntimeContext, Scope}

import scala.concurrent.ExecutionContext

/**
  * Define Function Operation
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class DefineFunctionOp(name: String, params: Seq[String], expression: Evaluatable) extends Evaluatable {

  override def eval(rc: RuntimeContext, scope: Scope)(implicit ec: ExecutionContext) = {
    scope += new UserDefinedFunction(name, params, expression)

    expression.eval(rc, scope) map { realValue =>
      scope.setVariable(name, realValue)
      realValue
    }
  }

}
