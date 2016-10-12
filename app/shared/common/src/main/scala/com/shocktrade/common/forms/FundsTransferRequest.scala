package com.shocktrade.common.forms

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Funds Transfer Request form
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
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
    def extract = for {
      accountType <- form.accountType.toOption
      amount <- form.amount.toOption
    } yield (accountType, amount)

  }

}