package com.shocktrade.cli.runtime

import scala.concurrent.{ExecutionContext, Future}

/**
 * Represents an executable code block with it's own scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class CodeBlock(ops: Seq[Evaluatable]) extends Evaluatable {

  override def eval(rc: RuntimeContext, scope: Scope)(implicit ec: ExecutionContext) = {
    ops.foldLeft[Future[TypedValue]](Future.successful(Null)) { (returnValue, op) =>
      op.eval(rc, scope)
    }
  }

}
