package com.shocktrade.cli.runtime.values

import com.shocktrade.cli.runtime._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Represents a reference to a function
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class FunctionReference(val name: String, args: Seq[Evaluatable]) extends Evaluatable {

  override def eval(rc: RuntimeContext, scope: Scope)(implicit ec: ExecutionContext) = {
    scope.findFunction(name) match {
      case Some(fx) =>
        // populate the function's scope with the arguments
        val fxScope = new LocalScope(scope)
        fx.params zip args foreach { case (param, arg) =>
          arg.eval(rc, fxScope) foreach (value => scope.setVariable(param, value))
        }
        fx.eval(rc, fxScope)
      case None =>
        Future.successful(Null)
    }
  }

}
