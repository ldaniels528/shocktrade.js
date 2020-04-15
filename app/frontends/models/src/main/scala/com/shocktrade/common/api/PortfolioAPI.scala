package com.shocktrade.common.api

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

  ///////////////////////////////////////////////////////////////
  //          Positions & Orders
  ///////////////////////////////////////////////////////////////

  def cancelOrderURL(portfolioId: String, orderId: String) = s"$baseURL/api/portfolio/$portfolioId/order/$orderId"

  def createOrderURL(contestID: String, userID: String) = s"$baseURL/api/order/$contestID/user/$userID"

  def createOrderByIDURL(portfolioID: String) = s"$baseURL/api/order/$portfolioID"

  def findHeldSecuritiesURL(portfolioID: String): String = s"$baseURL/api/portfolio/$portfolioID/heldSecurities"

  def findOrdersURL(contestID: String, userID: String) = s"$baseURL/api/orders/$contestID/user/$userID"

  def findPortfolioBalanceURL(contestID: String, userID: String) = s"$baseURL/api/portfolio/contest/$contestID/user/$userID/balance"

  def findPositionsURL(contestID: String, userID: String) = s"$baseURL/api/positions/$contestID/user/$userID"

  ///////////////////////////////////////////////////////////////
  //          Perks
  ///////////////////////////////////////////////////////////////

  def findAvailablePerksURL = s"$baseURL/api/contests/perks"

  def findPurchasedPerksURL(portfolioID: String) = s"$baseURL/api/portfolio/$portfolioID/perks"

  def purchasePerksURL(portfolioID: String) = s"$baseURL/api/portfolio/$portfolioID/perks"

}
