package com.shocktrade.common.forms

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Research Securities Options
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class ResearchOptions(var sortBy: js.UndefOr[String] = js.undefined,
                      var betaMax: js.UndefOr[Double] = js.undefined,
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
                      var reverse: js.UndefOr[Boolean] = js.undefined,
                      var maxResults: js.UndefOr[Int] = 25) extends js.Object
