package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
 * Represents a Performance-like model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait PerformanceLike extends js.Object {

  def performanceID: js.UndefOr[String]

  def symbol: js.UndefOr[String]

  def pricePaid: js.UndefOr[Double]

  def priceSold: js.UndefOr[Double]

  def quantity: js.UndefOr[Double]

  def commissions: js.UndefOr[Double]

}

/**
 * Performance-Like Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PerformanceLike {

  /**
   * Performance-Like Enrichment
   * @param performance the given [[Performance performance]]
   */
  implicit class PerformanceLikeEnrichment(val performance: PerformanceLike) extends AnyVal {

    @inline
    def gainLoss: js.UndefOr[Double] = for {
      proceeds <- proceeds
      cost <- totalCost
    } yield (proceeds / cost) * 100d

    @inline
    def proceeds: js.UndefOr[Double] = for {
      soldValue <- totalSold
      totalCost <- totalCost
    } yield soldValue - totalCost

    @inline
    def totalSold: js.UndefOr[Double] = for {
      priceSold <- performance.priceSold
      quantity <- performance.quantity
      commission <- performance.commissions
    } yield priceSold * quantity - commission

    @inline
    def totalCost: js.UndefOr[Double] = for {
      pricePaid <- performance.pricePaid
      quantity <- performance.quantity
      commission <- performance.commissions
    } yield pricePaid * quantity + commission

  }

}