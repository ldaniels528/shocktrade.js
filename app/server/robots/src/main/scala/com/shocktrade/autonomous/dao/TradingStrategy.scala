package com.shocktrade.autonomous.dao

import scala.scalajs.js

/**
  * Represents a Trading Strategy
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class TradingStrategy(val name: js.UndefOr[String],
                      val buyingFlow: js.UndefOr[BuyingFlow],
                      val sellingFlow: js.UndefOr[SellingFlow]) extends js.Object

/**
  * Trading Strategy
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object TradingStrategy {

  /**
    * Trading Strategy Enrichment
    * @param strategy the given [[TradingStrategy strategy]]
    */
  implicit class TradingStrategyEnrichment(val strategy: TradingStrategy) extends AnyVal {

    @inline
    def isValid: Boolean = {
      strategy.name.isDefined && strategy.buyingFlow.exists(_.isValid) && strategy.sellingFlow.exists(_.isValid)
    }

  }

}