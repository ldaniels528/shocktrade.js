package com.shocktrade.common.models.contest

import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Represents an order outcome
 * @param orderID         the given order ID
 * @param fulfilled       indicates whether the order was fulfilled
 * @param w               the write count
 * @param negotiatedPrice the negotiated price
 * @param xp              the experience award
 * @param positionID      the position ID
 * @param message         the optional fulfillment exception message
 */
class OrderOutcome(val orderID: String,
                   val fulfilled: Boolean,
                   val w: Int,
                   val negotiatedPrice: js.UndefOr[Double] = js.undefined,
                   val xp: js.UndefOr[Double] = js.undefined,
                   val positionID: js.UndefOr[String] = js.undefined,
                   val message: js.UndefOr[String] = js.undefined) extends js.Object

/**
 * Order Outcome Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OrderOutcome {

  /**
   * Order Outcome Enrichment
   * @param oo the given [[OrderOutcome]]
   */
  final implicit class OrderOutcomeEnrichment(val oo: OrderOutcome) extends AnyVal {

    @inline
    def copy(orderID: js.UndefOr[String] = js.undefined,
             fulfilled: js.UndefOr[Boolean] = js.undefined,
             w: js.UndefOr[Int] = js.undefined,
             negotiatedPrice: js.UndefOr[Double] = js.undefined,
             xp: js.UndefOr[Double] = js.undefined,
             positionID: js.UndefOr[String] = js.undefined,
             message: js.UndefOr[String] = js.undefined): OrderOutcome = {
      new OrderOutcome(
        orderID = orderID getOrElse oo.orderID,
        fulfilled = fulfilled getOrElse oo.fulfilled,
        w = w getOrElse oo.w,
        negotiatedPrice = negotiatedPrice ?? oo.negotiatedPrice,
        xp = xp ?? oo.xp,
        positionID = positionID ?? oo.positionID,
        message = message ?? oo.message)
    }
  }

}