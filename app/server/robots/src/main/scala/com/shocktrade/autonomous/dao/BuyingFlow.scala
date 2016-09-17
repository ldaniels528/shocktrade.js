package com.shocktrade.autonomous.dao

import com.shocktrade.autonomous.dao.BuyingFlow.Rule
import com.shocktrade.common.forms.ResearchOptions

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Trading Strategy's Buying Flow
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class BuyingFlow(val searchOptions: js.UndefOr[ResearchOptions],
                 val preferredSpendPerSecurity: js.UndefOr[Double] = js.undefined,
                 val rules: js.UndefOr[Rule]) extends js.Object

/**
  * Buying Flow Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object BuyingFlow {

  @ScalaJSDefined
  class Rule(val name: js.UndefOr[String],
             val exclude: js.UndefOr[js.Dictionary[js.Any]],
             val sortBy: js.UndefOr[js.Dictionary[String]]) extends js.Object

  /**
    * Buying Flow Enrichment
    * @param flow the given [[BuyingFlow buying flow]]
    */
  implicit class BuyingFlowEnrichment(val flow: BuyingFlow) extends AnyVal {

    @inline
    def isValid = flow.searchOptions.isDefined

  }

}
