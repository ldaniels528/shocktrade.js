package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
 * Represents a reference to a portfolio
 * @param portfolioID the given portfolio ID
 */
class PortfolioRef(val portfolioID: js.UndefOr[String]) extends js.Object

/**
 * Portfolio Reference Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PortfolioRef {

  def apply(portfolioID: js.UndefOr[String]): PortfolioRef = new PortfolioRef(portfolioID)

  def unapply(ref: PortfolioRef): Option[String] = ref.portfolioID.toOption

}