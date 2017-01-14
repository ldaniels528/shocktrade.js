package io.scalajs.npm.stylus

import io.scalajs.RawOptions
import io.scalajs.npm.express

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.|

/**
  * Stylus npm module
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait Stylus extends js.Object {

  def apply(str: String): express.Application = js.native

  def middleware(options: MiddlewareOptions | RawOptions): js.Function3[express.Request, express.Response, js.Function, Any] = js.native

  def render(str: String, options: js.Any = null): js.Function = js.native

}

/**
  * Stylus Singleton
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
@JSImport("stylus", JSImport.Namespace)
object Stylus extends Stylus