package io.scalajs.npm.stylus

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Middleware Options
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class MiddlewareOptions(val src: js.UndefOr[String] = js.undefined,
                        val compile: js.UndefOr[js.Function] = js.undefined) extends js.Object