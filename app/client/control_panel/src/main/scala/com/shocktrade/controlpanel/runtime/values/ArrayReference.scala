package com.shocktrade.controlpanel.runtime.values

import com.shocktrade.controlpanel.runtime._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Represents an array reference
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ArrayReference(items: Seq[Evaluatable]) extends Evaluatable {

  override def eval(rc: RuntimeContext, scope: Scope)(implicit ec: ExecutionContext) = {
    val promises = Future.sequence(items.map(_.eval(rc, scope)))
    promises.map(ArrayValue.apply)
  }

  override def toString = s"[${items.mkString(", ")}]"

}
