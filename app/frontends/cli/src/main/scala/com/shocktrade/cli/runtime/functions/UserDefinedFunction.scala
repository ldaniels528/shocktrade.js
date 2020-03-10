package com.shocktrade.cli.runtime.functions

import com.shocktrade.cli.runtime._

import scala.concurrent.ExecutionContext

/**
 * Represents a user-defined function
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserDefinedFunction(val name: String, val params: Seq[String], ops: Evaluatable) extends Function {

  override def eval(rc: RuntimeContext, scope: Scope)(implicit ec: ExecutionContext) = ops.eval(rc, scope)

}