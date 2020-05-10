package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
 * Represents a Position-like model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait PositionLike extends js.Object {

  def portfolioID: js.UndefOr[String]

  def positionID: js.UndefOr[String]

  def symbol: js.UndefOr[String]

  def exchange: js.UndefOr[String]

  def quantity: js.UndefOr[Double]

  def processedTime: js.UndefOr[js.Date]

}

/**
 * Position-Like Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PositionLike {

  /**
   * Position-Like Enriched
   * @param ref the host [[PositionLike]]
   */
  final implicit class PositionLikeEnriched(val ref: PositionLike) extends AnyVal {

    @inline
    def portfolioID_! : String = ref.portfolioID.getOrElse(throw js.JavaScriptException("portfolio ID is required"))

    @inline
    def positionID_! : String = ref.positionID.getOrElse(throw js.JavaScriptException("position ID is required"))

  }

}
