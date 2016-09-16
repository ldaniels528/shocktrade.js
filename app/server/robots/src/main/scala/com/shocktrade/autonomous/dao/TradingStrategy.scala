package com.shocktrade.autonomous.dao

import com.shocktrade.common.forms.ResearchOptions

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Trading Strategy
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class TradingStrategy(val name: js.UndefOr[String],
                      val buyingFlow: js.UndefOr[BuyingFlow],
                      val sellingFlow: js.UndefOr[SellingFlow]) extends js.Object

/**
  * Trading Strategy
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object TradingStrategy {

  /**
    * Returns a default trading strategy
    * @return a [[TradingStrategy trading strategy]]
    */
  def default() = {
    new TradingStrategy(
      name = "Day-Trading",
      buyingFlow = new BuyingFlow(
        preferredSpendPerSecurity = 1000.0,
        searchOptions = new ResearchOptions(
          changeMin = -0.10,
          spreadMin = 25.0,
          priceMin =  0.0001,
          priceMax = 2.0,
          avgVolumeMin = 1e+6,
          sortBy = "spread",
          reverse = true,
          maxResults = 250
        )),
      sellingFlow = new SellingFlow()
    )
  }

  /**
    * Trading Strategy Enrichment
    * @param strategy the given [[TradingStrategy strategy]]
    */
  implicit class TradingStrategyEnrichment(val strategy: TradingStrategy) extends AnyVal {

    @inline
    def isValid = strategy.name.isDefined && strategy.buyingFlow.exists(_.isValid) && strategy.sellingFlow.exists(_.isValid)

  }

}