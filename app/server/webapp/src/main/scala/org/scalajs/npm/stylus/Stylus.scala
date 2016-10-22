package org.scalajs.npm.stylus

import org.scalajs.nodejs.{NodeModule, NodeRequire, express}

import scala.scalajs.js
import scala.scalajs.js.|

/**
  * Stylus npm module
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait Stylus extends NodeModule {

  def apply(str: String): express.Application = js.native

  def middleware(options: MiddlewareOptions | js.Any): js.Function3[express.Request, express.Response, js.Function, Any] = js.native

  def render(str: String, options: js.Any = null): js.Function = js.native

}

/**
  * Stylus Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object Stylus {

  def apply()(implicit require: NodeRequire) = require[Stylus]("stylus")

}
