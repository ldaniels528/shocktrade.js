package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
 * Represents a Position-like model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait PositionLike extends js.Object {

  def positionID: js.UndefOr[String]

  def symbol: js.UndefOr[String]

  def exchange: js.UndefOr[String]

  def quantity: js.UndefOr[Double]

  def processedTime: js.UndefOr[js.Date]

}
