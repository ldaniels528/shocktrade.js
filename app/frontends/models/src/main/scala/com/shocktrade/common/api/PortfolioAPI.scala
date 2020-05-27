package com.shocktrade.common.api

import com.shocktrade.common.forms.OrderSearchOptions

import scala.scalajs.js

/**
 * Portfolio API
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait PortfolioAPI extends BaseAPI {

  ///////////////////////////////////////////////////////////////
  //          Portfolio Lifecycle
  ///////////////////////////////////////////////////////////////

  def findPortfolioByIDURL(portfolioID: String) = s"$baseURL/api/portfolio/$portfolioID"

  def findPortfolioByUserURL(contestID: String, userID: String) = s"$baseURL/api/portfolio/contest/$contestID/user/$userID"

  def findPortfoliosByUserURL(userID: String) = s"$baseURL/api/portfolios/user/$userID"

  def findPortfoliosByContestURL(contestID: String) = s"$baseURL/api/portfolios/contest/$contestID"

  ///////////////////////////////////////////////////////////////
  //          Miscellaneous
  ///////////////////////////////////////////////////////////////

  def findChartURL(contestID: String, userID: String, chart: String) = s"$baseURL/api/contest/$contestID/user/$userID/chart/$chart"

  def findPortfolioBalanceURL(contestID: String, userID: String) = s"$baseURL/api/portfolio/contest/$contestID/user/$userID/balance"

  ///////////////////////////////////////////////////////////////
  //          Orders
  ///////////////////////////////////////////////////////////////

  def cancelOrderURL(orderID: String) = s"$baseURL/api/order/$orderID"

  def createOrderURL(contestID: String, userID: String) = s"$baseURL/api/order/$contestID/user/$userID"

  def createOrdersURL(portfolioID: String) = s"$baseURL/api/orders/$portfolioID"

  def findOrderByIDURL(orderID: String) = s"$baseURL/api/order/$orderID"

  def orderSearchURL(options: js.UndefOr[OrderSearchOptions] = js.undefined): String = {
    val uri = s"$baseURL/api/orders"
    options.map(opts => s"$uri?${opts.toQueryString}") getOrElse uri
  }

  ///////////////////////////////////////////////////////////////
  //          Positions
  ///////////////////////////////////////////////////////////////

  def findHeldSecuritiesURL(portfolioID: String): String = s"$baseURL/api/positions/$portfolioID/heldSecurities"

  def findPositionByIDURL(positionID: String) = s"$baseURL/api/position/$positionID"

  def findPositionsURL(contestID: String, userID: String) = s"$baseURL/api/positions/$contestID/user/$userID"

  ///////////////////////////////////////////////////////////////
  //          Perks
  ///////////////////////////////////////////////////////////////

  def findPurchasedPerksURL(portfolioID: String) = s"$baseURL/api/portfolio/$portfolioID/perks"

  def purchasePerksURL(portfolioID: String) = s"$baseURL/api/portfolio/$portfolioID/perks"

}
