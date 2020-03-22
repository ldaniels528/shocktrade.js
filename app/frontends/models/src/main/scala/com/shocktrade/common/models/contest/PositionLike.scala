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

  def price: js.UndefOr[Double]

  def quantity: js.UndefOr[Double]

  def commission: js.UndefOr[Double]

  def processedTime: js.UndefOr[js.Date]

  def netValue: js.UndefOr[Double]

}

/**
 * Position-Like Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PositionLike {

  /**
   * Position Enrichment
   * @param position the given [[PositionLike position]]
   */
  implicit class PositionLikeEnrichment(val position: PositionLike) extends AnyVal {

    @inline
    def totalCost: js.UndefOr[Double] = for {
      pricePaid <- position.price
      quantity <- position.quantity
    } yield pricePaid * quantity

    @inline
    def totalCostWithCommissions: js.UndefOr[Double] = for {
      pricePaid <- position.price
      quantity <- position.quantity
      commission <- position.commission
    } yield pricePaid * quantity + commission

  }

}