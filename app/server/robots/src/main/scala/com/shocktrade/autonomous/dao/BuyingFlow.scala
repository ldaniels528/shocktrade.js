package com.shocktrade.autonomous.dao

import com.shocktrade.autonomous.dao.BuyingFlow.RuleSet
import com.shocktrade.common.forms.ResearchOptions

import scala.scalajs.js

/**
  * Represents a Trading Strategy Security Buying Flow
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class BuyingFlow(val searchOptions: js.UndefOr[ResearchOptions],
                 val preferredSpendPerSecurity: js.UndefOr[Double] = js.undefined,
                 val rules: js.UndefOr[js.Array[RuleSet]] = js.undefined) extends js.Object

/**
  * Buying Flow Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object BuyingFlow {

  class RuleSet(val name: js.UndefOr[String],
                val exclude: js.UndefOr[js.Array[js.Dictionary[js.Any]]]) extends js.Object

  /**
    * Buying Flow Enrichment
    * @param flow the given [[BuyingFlow buying flow]]
    */
  implicit class BuyingFlowEnrichment(val flow: BuyingFlow) extends AnyVal {

    @inline
    def isValid: Boolean = flow.searchOptions.isDefined

  }

}
