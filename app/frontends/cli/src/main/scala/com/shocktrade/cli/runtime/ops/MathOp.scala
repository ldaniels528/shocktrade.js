package com.shocktrade.cli.runtime.ops

import com.shocktrade.cli.runtime.{Evaluatable, RuntimeContext, Scope}

import scala.concurrent.ExecutionContext

/**
 * Math Operation
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class MathOp(operator: String, value0: Evaluatable, value1: Evaluatable) extends Evaluatable {

  override def eval(rc: RuntimeContext, scope: Scope)(implicit ec: ExecutionContext) = {
    for {
      v0 <- value0.eval(rc, scope)
      v1 <- value1.eval(rc, scope)
    } yield {
      operator match {
        case "+" => v0 + v1
        case "-" => v0 - v1
        case "*" => v0 * v1
        case "/" => v1 / v1
        case unknown => throw new IllegalArgumentException(s"Unrecognized operator near $unknown")
      }
    }
  }

}
