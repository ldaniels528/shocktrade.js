package com.shocktrade.common.models.quote

import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Complete Quote
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
trait CompleteQuote extends ResearchQuote with KeyStatistics {
  var naicsNumber: js.UndefOr[Int]
  var sicNumber: js.UndefOr[Int]
}

/**
  * Complete Quote Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object CompleteQuote {

  def apply() = New[CompleteQuote]

}