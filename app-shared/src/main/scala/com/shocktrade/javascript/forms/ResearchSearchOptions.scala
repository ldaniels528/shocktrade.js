package com.shocktrade.javascript.forms

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Research Search Options
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class ResearchSearchOptions(var sortBy: js.UndefOr[String] = js.undefined,
                            var changeMax: js.UndefOr[Double] = js.undefined,
                            var changeMin: js.UndefOr[Double] = js.undefined,
                            var priceMax: js.UndefOr[Double] = js.undefined,
                            var priceMin: js.UndefOr[Double] = js.undefined,
                            var spreadMax: js.UndefOr[Double] = js.undefined,
                            var spreadMin: js.UndefOr[Double] = js.undefined,
                            var volumeMax: js.UndefOr[Double] = js.undefined,
                            var volumeMin: js.UndefOr[Double] = js.undefined,
                            var reverse: js.UndefOr[Boolean] = false,
                            var maxResults: js.UndefOr[Int] = 25) extends js.Object
