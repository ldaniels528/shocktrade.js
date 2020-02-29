package com.shocktrade.common.forms

import scala.scalajs.js

/**
  * Funds Transfer Request form
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class FundsTransferRequest(val accountType: js.UndefOr[String],
                           val amount: js.UndefOr[Double]) extends js.Object

/**
  * Funds Transfer Request Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object FundsTransferRequest {

  /**
    * Funds Transfer Request Enrichment
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  implicit class FundsTransferRequestEnrichment(val form: FundsTransferRequest) extends AnyVal {

    @inline
    def extract: Option[(String, Double)] = for {
      accountType <- form.accountType.toOption
      amount <- form.amount.toOption
    } yield (accountType, amount)

  }

}