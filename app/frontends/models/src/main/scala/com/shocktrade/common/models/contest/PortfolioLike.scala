package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
 * Represents a Portfolio-like model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait PortfolioLike extends js.Object {

  def portfolioID: js.UndefOr[String]

  def contestID: js.UndefOr[String]

  def userID: js.UndefOr[String]

  def funds: js.UndefOr[Double]

  def totalXP: js.UndefOr[Double]

  def closedTime: js.UndefOr[js.Date]

}

/**
 * Portfolio-Like Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PortfolioLike {

  /**
   * Portfolio-Like Enriched
   * @param ref the host [[PortfolioLike]]
   */
  final implicit class PortfolioLikeEnriched(val ref: PortfolioLike) extends AnyVal {

    @inline
    def contestID_! : String = ref.contestID.getOrElse(throw js.JavaScriptException("Contest ID is required"))

    @inline
    def portfolioID_! : String = ref.portfolioID.getOrElse(throw js.JavaScriptException("portfolio ID is required"))

    @inline
    def userID_! : String = ref.userID.getOrElse(throw js.JavaScriptException("User ID is required"))

  }

}
