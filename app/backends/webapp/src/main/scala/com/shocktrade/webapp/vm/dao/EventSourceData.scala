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
                      val negotiatedPrice: js.UndefOr[Double],
                      val quantity: js.UndefOr[Double],
                      val xp: js.UndefOr[Double],
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
            negotiatedPrice: js.UndefOr[Double] = js.undefined,
            quantity: js.UndefOr[Double] = js.undefined,
            xp: js.UndefOr[Double] = js.undefined,
            response: js.UndefOr[String] = js.undefined,
            responseTimeMillis: js.UndefOr[Double] = js.undefined,
            failed: js.UndefOr[Boolean] = js.undefined,
            creationTime: js.UndefOr[Date] = js.undefined): EventSourceData = {
    new EventSourceData(
      command = command, `type` = `type`,
      contestID = contestID, portfolioID = portfolioID, positionID = positionID, userID = userID, orderID = orderID,
      symbol = symbol, exchange = exchange, orderType = orderType, priceType = priceType, quantity = quantity,
      negotiatedPrice = negotiatedPrice, xp = xp,
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
             negotiatedPrice: js.UndefOr[Double] = js.undefined,
             quantity: js.UndefOr[Double] = js.undefined,
             xp: js.UndefOr[Double] = js.undefined,
             response: js.UndefOr[String] = js.undefined,
             responseTimeMillis: js.UndefOr[Double] = js.undefined,
             failed: js.UndefOr[Boolean] = js.undefined,
             creationTime: js.UndefOr[Date] = js.undefined): EventSourceData = {
      new EventSourceData(
        command = command ?? event.command,
        `type` = `type` ?? event.`type`,
        contestID = contestID ?? event.contestID,
        portfolioID = portfolioID ?? event.portfolioID,
        positionID = positionID ?? event.positionID,
        userID = userID ?? event.userID,
        orderID = orderID ?? event.orderID,
        symbol = symbol ?? event.symbol,
        exchange = exchange ?? event.exchange,
        orderType = orderType ?? event.orderType,
        priceType = priceType ?? event.priceType,
        quantity = quantity ?? event.quantity,
        negotiatedPrice = negotiatedPrice ?? event.negotiatedPrice,
        xp = xp ?? event.xp,
        response = response ?? event.response,
        responseTimeMillis = responseTimeMillis ?? event.responseTimeMillis,
        failed = failed ?? event.failed,
        creationTime = creationTime ?? event.creationTime
      )
    }

  }

}