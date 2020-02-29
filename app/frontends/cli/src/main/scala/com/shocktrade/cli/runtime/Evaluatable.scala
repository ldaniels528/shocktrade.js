package com.shocktrade.cli.runtime

import scala.concurrent.{ExecutionContext, Future}

/**
  * Represents an evaluatable value or expression
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait Evaluatable {

  def eval(rc: RuntimeContext, scope: Scope)(implicit ec: ExecutionContext): Future[TypedValue]

}