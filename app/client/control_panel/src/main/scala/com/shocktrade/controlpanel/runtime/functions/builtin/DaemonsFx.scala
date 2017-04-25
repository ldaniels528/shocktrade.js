package com.shocktrade.controlpanel.runtime.functions
package builtin

import com.shocktrade.controlpanel.runtime.{RuntimeContext, Scope, TextValue}
import io.scalajs.JSON
import io.scalajs.npm.request.Request
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.{ExecutionContext, Future}

/**
  * daemons() Function
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class DaemonsFx() extends Function {

  override def name = "daemons"

  override def params = Seq("remote")

  override def eval(rc: RuntimeContext, scope: Scope)(implicit ec: ExecutionContext): Future[TextValue] = {
    val remote = scope.findVariable("remote").map(_.value.toString) getOrElse "localhost:1337"
    val promise = Request.getFuture(s"http://$remote/api/daemons") map { case (response, data) => pretty(data.toString) }
    promise.map(TextValue.apply)
  }

  private def pretty(data: String) = JSON.stringify(JSON.parse(data), null, "\t")

}
