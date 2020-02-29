package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
  * Represents a Portfolio-like model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait PortfolioLike extends js.Object {

  def portfolioID: js.UndefOr[String]

  def active: js.UndefOr[Boolean]

  def perks: js.UndefOr[js.Array[String]]

  def cashAccount: js.UndefOr[CashAccount]

  def marginAccount: js.UndefOr[MarginAccount]

  def orders: js.UndefOr[js.Array[_ <: OrderLike]]

  def closedOrders: js.UndefOr[js.Array[_ <: OrderLike]]

  def performance: js.UndefOr[js.Array[_ <: PerformanceLike]]

  def positions: js.UndefOr[js.Array[_ <: PositionLike]]

}
