package com.shocktrade.server.dao

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * News Source Data Object
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class NewsSourceData(val name: js.UndefOr[String] = js.undefined,
                     val url: js.UndefOr[String] = js.undefined,
                     val priority: js.UndefOr[Int] = js.undefined) extends js.Object
