package com.shocktrade.controlpanel.runtime.values

import com.shocktrade.controlpanel.runtime.{Evaluatable, Null, RuntimeContext, Scope}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Represents a reference to a variable
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class VariableReference(name: String) extends Evaluatable {

  override def eval(rc: RuntimeContext, scope: Scope)(implicit ec: ExecutionContext) = {
    Future.successful(scope.findVariable(name) getOrElse Null)
  }

}
