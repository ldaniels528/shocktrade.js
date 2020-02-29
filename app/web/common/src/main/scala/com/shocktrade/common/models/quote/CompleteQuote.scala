package com.shocktrade.common.models.quote

import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * Represents a Complete Quote
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait CompleteQuote extends KeyStatistics {
  var symbol: js.UndefOr[String]
  var name: js.UndefOr[String]
  var exchange: js.UndefOr[String]
  var market: js.UndefOr[String]
  var lastTrade: js.UndefOr[Double]
  var open: js.UndefOr[Double]
  var close: js.UndefOr[Double]
  var prevClose: js.UndefOr[Double]
  var high: js.UndefOr[Double]
  var low: js.UndefOr[Double]
  var change: js.UndefOr[Double]
  var changePct: js.UndefOr[Double]
  var spread: js.UndefOr[Double]
  //var volume: js.UndefOr[Double]
  var avgVolume10Day: js.UndefOr[Double]
  //var beta: js.UndefOr[Double]
  var active: js.UndefOr[Boolean]

  var naicsNumber: js.UndefOr[Int]
  var sicNumber: js.UndefOr[Int]
}

/**
  * Complete Quote Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object CompleteQuote {

  def apply(): CompleteQuote = New[CompleteQuote]

}