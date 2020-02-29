package com.shocktrade.cli.runtime.ops

import com.shocktrade.cli.runtime._

import scala.concurrent.{ExecutionContext, Future}

/**
  * No Operation
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object NoOp extends Evaluatable {

  override def eval(rc: RuntimeContext, scope: Scope)(implicit ec: ExecutionContext) = Future.successful(Null)

}
