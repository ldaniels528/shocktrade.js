package com.shocktrade.controlpanel.runtime.ops

import com.shocktrade.controlpanel.runtime._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Show Scope Operation
  */
class ScopeOp() extends Evaluatable {

  override def eval(rc: RuntimeContext, scope: Scope)(implicit ec: ExecutionContext) = {
    Future.successful(TextValue(scope.toString))
  }

}
