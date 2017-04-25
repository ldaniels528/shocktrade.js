package com.shocktrade.server.dao.contest

import java.util.UUID

import com.shocktrade.common.models.contest.PositionLike
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * Position Data
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class PositionData(var _id: js.UndefOr[String] = UUID.randomUUID().toString,
                   var symbol: js.UndefOr[String],
                   var exchange: js.UndefOr[String],
                   var pricePaid: js.UndefOr[Double],
                   var quantity: js.UndefOr[Double],
                   var commission: js.UndefOr[Double],
                   var processedTime: js.UndefOr[js.Date],
                   var accountType: js.UndefOr[String],
                   var netValue: js.UndefOr[Double]) extends PositionLike


/**
  * Position Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object PositionData {

  /**
    * Position Enrichment
    * @param position the given [[PositionData position]]
    */
  implicit class PositionDataEnrichment(val position: PositionData) extends AnyVal {

    @inline
    def copy(_id: js.UndefOr[String] = js.undefined,
             symbol: js.UndefOr[String] = js.undefined,
             exchange: js.UndefOr[String] = js.undefined,
             pricePaid: js.UndefOr[Double] = js.undefined,
             quantity: js.UndefOr[Double] = js.undefined,
             commission: js.UndefOr[Double] = js.undefined,
             processedTime: js.UndefOr[js.Date] = js.undefined,
             accountType: js.UndefOr[String] = js.undefined,
             netValue: js.UndefOr[Double] = js.undefined) = {
      new PositionData(
        _id = _id ?? position._id,
        symbol = symbol ?? position.symbol,
        exchange = exchange ?? position.exchange,
        pricePaid = pricePaid ?? position.pricePaid,
        quantity = quantity ?? position.quantity,
        commission = commission ?? position.commission,
        processedTime = processedTime ?? position.processedTime,
        accountType = accountType ?? position.accountType,
        netValue = netValue ?? position.netValue)
    }

    @inline
    def fundingAsOfDate = {
      position match {
        case o if o.isCashAccount => "cashAccount.asOfDate"
        case o if o.isMarginAccount => "marginAccount.asOfDate"
        case o => die(s"Invalid account type (${position.accountType.orNull}) for order # ${position._id.orNull}")
      }
    }

    @inline
    def fundingSource = {
      position match {
        case o if o.isCashAccount => "cashAccount.funds"
        case o if o.isMarginAccount => "marginAccount.funds"
        case o => die(s"Invalid account type (${position.accountType.orNull}) for order # ${position._id.orNull}")
      }
    }

    @inline
    def toPerformance(priceSold: Double, commission: Double) = new PerformanceData(
      _id = UUID.randomUUID().toString,
      symbol = position.symbol,
      pricePaid = position.pricePaid,
      priceSold = priceSold,
      quantity = position.quantity,
      commissions = position.commission.map(_ + commission)
    )

  }

}