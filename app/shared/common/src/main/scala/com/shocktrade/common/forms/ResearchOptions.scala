package com.shocktrade.common.forms

import com.shocktrade.common.forms.ResearchOptions.SortField

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Securities Research Options
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class ResearchOptions(var betaMax: js.UndefOr[Double] = js.undefined,
                      var betaMin: js.UndefOr[Double] = js.undefined,
                      var changeMax: js.UndefOr[Double] = js.undefined,
                      var changeMin: js.UndefOr[Double] = js.undefined,
                      var priceMax: js.UndefOr[Double] = js.undefined,
                      var priceMin: js.UndefOr[Double] = js.undefined,
                      var spreadMax: js.UndefOr[Double] = js.undefined,
                      var spreadMin: js.UndefOr[Double] = js.undefined,
                      var volumeMax: js.UndefOr[Double] = js.undefined,
                      var volumeMin: js.UndefOr[Double] = js.undefined,
                      var avgVolumeMax: js.UndefOr[Double] = js.undefined,
                      var avgVolumeMin: js.UndefOr[Double] = js.undefined,
                      var sortFields: js.UndefOr[js.Array[SortField]] = js.undefined,
                      var sortBy: js.UndefOr[String] = js.undefined,
                      var reverse: js.UndefOr[Boolean] = js.undefined,
                      var maxResults: js.UndefOr[Int] = 25) extends js.Object

/**
  * Research Options Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ResearchOptions {

  @ScalaJSDefined
  class SortField(val field: String, val direction: Int) extends js.Object

}