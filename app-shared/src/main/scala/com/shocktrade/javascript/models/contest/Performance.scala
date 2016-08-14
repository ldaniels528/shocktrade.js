package com.shocktrade.javascript.models.contest

import scala.scalajs.js

/**
  * Trading Performance Model
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait Performance extends js.Object {
  var _id: js.UndefOr[String] = js.native
  var symbol: js.UndefOr[String] = js.native
  var pricePaid: js.UndefOr[Double] = js.native
  var priceSold: js.UndefOr[Double] = js.native
  var quantity: js.UndefOr[Double] = js.native
  var commissions: js.UndefOr[Double] = js.native

}

/**
  * Performance Companion
  * @author lawrence.daniels@gmail.com
  */
object Performance {

  /**
    * Performance Enrichment
    * @param performance the given [[Performance performance]]
    */
  implicit class PerformanceEnrichment(val performance: Performance) extends AnyVal {

    @inline
    def gainLoss = for {
      proceeds <- proceeds
      cost <- totalCost
    } yield (proceeds / cost) * 100d

    @inline
    def proceeds = for {
      soldValue <- totalSold
      totalCost <- totalCost
    } yield soldValue - totalCost

    @inline
    def totalCost = for {
      pricePaid <- performance.pricePaid
      quantity <- performance.quantity
      commission <- performance.commissions
    } yield pricePaid * quantity + commission

    @inline
    def totalSold = for {
      priceSold <- performance.priceSold
      quantity <- performance.quantity
      commission <- performance.commissions
    } yield priceSold * quantity - commission

  }

}