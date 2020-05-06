package com.shocktrade.webapp.vm.dao

import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.Date

/**
 * Event Source Data
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class EventSourceData(val command: js.UndefOr[String],
                      val `type`: js.UndefOr[String],
                      val contestID: js.UndefOr[String],
                      val portfolioID: js.UndefOr[String],
                      val positionID: js.UndefOr[String],
                      val userID: js.UndefOr[String],
                      val orderID: js.UndefOr[String],
                      val symbol: js.UndefOr[String],
                      val exchange: js.UndefOr[String],
                      val orderType: js.UndefOr[String],
                      val priceType: js.UndefOr[String],
                      val quantity: js.UndefOr[Double],
                      val price: js.UndefOr[Double],
                      val response: js.UndefOr[String],
                      val responseTimeMillis: js.UndefOr[Double],
                      val failed: js.UndefOr[Boolean],
                      val creationTime: js.UndefOr[js.Date]) extends js.Object

/**
 * Event Source Data Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object EventSourceData {

  def apply(command: js.UndefOr[String] = js.undefined,
            `type`: js.UndefOr[String] = js.undefined,
            contestID: js.UndefOr[String] = js.undefined,
            portfolioID: js.UndefOr[String] = js.undefined,
            positionID: js.UndefOr[String] = js.undefined,
            userID: js.UndefOr[String] = js.undefined,
            orderID: js.UndefOr[String] = js.undefined,
            symbol: js.UndefOr[String] = js.undefined,
            exchange: js.UndefOr[String] = js.undefined,
            orderType: js.UndefOr[String] = js.undefined,
            priceType: js.UndefOr[String] = js.undefined,
            quantity: js.UndefOr[Double] = js.undefined,
            price: js.UndefOr[Double] = js.undefined,
            response: js.UndefOr[String] = js.undefined,
            responseTimeMillis: js.UndefOr[Double] = js.undefined,
            failed: js.UndefOr[Boolean] = js.undefined,
            creationTime: js.UndefOr[Date] = js.undefined): EventSourceData = {
    new EventSourceData(
      command = command, `type` = `type`,
      contestID = contestID, portfolioID = portfolioID, positionID = positionID, userID = userID, orderID = orderID,
      symbol = symbol, exchange = exchange, orderType = orderType, priceType = priceType, quantity = quantity, price = price,
      response = response, responseTimeMillis = responseTimeMillis, failed = failed, creationTime = creationTime
    )
  }

  final implicit class EventSourceDataEnriched(val event: EventSourceData) extends AnyVal {

    def copy(command: js.UndefOr[String] = js.undefined,
             `type`: js.UndefOr[String] = js.undefined,
             contestID: js.UndefOr[String] = js.undefined,
             portfolioID: js.UndefOr[String] = js.undefined,
             positionID: js.UndefOr[String] = js.undefined,
             userID: js.UndefOr[String] = js.undefined,
             orderID: js.UndefOr[String] = js.undefined,
             symbol: js.UndefOr[String] = js.undefined,
             exchange: js.UndefOr[String] = js.undefined,
             orderType: js.UndefOr[String] = js.undefined,
             priceType: js.UndefOr[String] = js.undefined,
             quantity: js.UndefOr[Double] = js.undefined,
             price: js.UndefOr[Double] = js.undefined,
             response: js.UndefOr[String] = js.undefined,
             responseTimeMillis: js.UndefOr[Double] = js.undefined,
             failed: js.UndefOr[Boolean] = js.undefined,
             creationTime: js.UndefOr[Date] = js.undefined): EventSourceData = {
      new EventSourceData(
        command = event.command ?? command,
        `type` = event.`type` ?? `type`,
        contestID = event.contestID ?? contestID,
        portfolioID = event.portfolioID ?? portfolioID,
        positionID = event.positionID ?? positionID,
        userID = event.userID ?? userID,
        orderID = event.orderID ?? orderID,
        symbol = event.symbol ?? symbol,
        exchange = event.exchange ?? exchange,
        orderType = event.orderType ?? orderType,
        priceType = event.priceType ?? priceType,
        quantity = event.quantity ?? quantity,
        price = event.price ?? price,
        response = event.response ?? response,
        responseTimeMillis = event.responseTimeMillis ?? responseTimeMillis,
        failed = event.failed ?? failed,
        creationTime = event.creationTime ?? creationTime
      )
    }

  }

}