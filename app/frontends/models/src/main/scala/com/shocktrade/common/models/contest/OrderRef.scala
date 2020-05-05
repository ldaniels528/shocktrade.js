package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
 * Represents a reference to an order
 * @param orderID the given order ID
 */
class OrderRef(val orderID: js.UndefOr[String]) extends js.Object

/**
 * Order Reference Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object OrderRef {

  def apply(orderID: js.UndefOr[String]): OrderRef = new OrderRef(orderID)

  def unapply(ref: OrderRef): Option[String] = ref.orderID.toOption

}