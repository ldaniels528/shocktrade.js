package io.scalajs.npm.stylus

import scala.scalajs.js

/**
  * Middleware Options
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class MiddlewareOptions(val src: js.UndefOr[String] = js.undefined,
                        val compile: js.UndefOr[js.Function] = js.undefined) extends js.Object