package com.shocktrade.javascript.models.contest

import java.util.UUID

import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Position Model
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Position(var _id: js.UndefOr[String] = UUID.randomUUID().toString,
               var symbol: js.UndefOr[String],
               var exchange: js.UndefOr[String],
               var pricePaid: js.UndefOr[Double],
               var quantity: js.UndefOr[Double],
               var commission: js.UndefOr[Double],
               var processedTime: js.UndefOr[Double],
               var accountType: js.UndefOr[String],
               var netValue: js.UndefOr[Double]) extends js.Object

/**
  * Position Companion
  * @author lawrence.daniels@gmail.com
  */
object Position {

  /**
    * Position Enrichment
    * @param position the given [[Position position]]
    */
  implicit class PositionEnrichment(val position: Position) extends AnyVal {

    @inline
    def copy(_id: js.UndefOr[String] = js.undefined,
             symbol: js.UndefOr[String] = js.undefined,
             exchange: js.UndefOr[String] = js.undefined,
             pricePaid: js.UndefOr[Double] = js.undefined,
             quantity: js.UndefOr[Double] = js.undefined,
             commission: js.UndefOr[Double] = js.undefined,
             processedTime: js.UndefOr[Double] = js.undefined,
             accountType: js.UndefOr[String] = js.undefined,
             netValue: js.UndefOr[Double] = js.undefined) = {
      new Position(_id = _id ?? position._id,
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
    def isCashAccount = position.accountType.contains("CASH")

    @inline
    def isMarginAccount = position.accountType.contains("MARGIN")

    @inline
    def totalCost = for {
      pricePaid <- position.pricePaid
      quantity <- position.quantity
    } yield pricePaid * quantity

    @inline
    def totalCostWithCommissions = for {
      pricePaid <- position.pricePaid
      quantity <- position.quantity
      commission <- position.commission
    } yield pricePaid * quantity + commission

  }

}