package com.shocktrade.autonomous.dao

import com.shocktrade.common.forms.ResearchOptions

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Trading Strategy's Buying Flow
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class BuyingFlow(val searchOptions: js.UndefOr[ResearchOptions],
                 val preferredSpendPerSecurity: js.UndefOr[Double] = js.undefined) extends js.Object

/**
  * Buying Flow Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object BuyingFlow {

  /**
    * Buying Flow Enrichment
    * @param flow the given [[BuyingFlow flow]]
    */
  implicit class BuyingFlowEnrichment(val flow: BuyingFlow) extends AnyVal {

    @inline
    def isValid = flow.searchOptions.isDefined

  }

}
