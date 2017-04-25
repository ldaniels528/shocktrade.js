package com.shocktrade.autonomous.dao

import scala.scalajs.js

/**
  * Represents a Trading Strategy's Selling Flow
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class SellingFlow(val profitTarget: js.UndefOr[Double]) extends js.Object

/**
  * Selling Flow Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object SellingFlow {

  /**
    * Selling Flow Enrichment
    * @param flow the given [[SellingFlow selling flow]]
    */
  implicit class SellingFlowEnrichment(val flow: SellingFlow) extends AnyVal {

    @inline
    def isValid: Boolean = flow.profitTarget.nonEmpty

  }

}