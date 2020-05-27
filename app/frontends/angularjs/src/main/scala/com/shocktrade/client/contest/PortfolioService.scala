package com.shocktrade.client.contest

import com.shocktrade.client.contest.models.{Order, Portfolio}
import com.shocktrade.common.Ok
import com.shocktrade.common.api.PortfolioAPI
import com.shocktrade.common.forms.{NewOrderForm, OrderSearchOptions, PerksResponse}
import com.shocktrade.common.models.contest.{ChartData, _}
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.scalajs.js

/**
 * Portfolio Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PortfolioService($http: Http) extends Service with PortfolioAPI {

  def findPortfolioBalance(contestID: String, userID: String): js.Promise[HttpResponse[PortfolioBalance]] = {
    $http.get(findPortfolioBalanceURL(contestID,userID))
  }

  /**
   * Retrieves a portfolio by a contest ID and player ID
   * @param portfolioID the given player ID
   * @return the promise of a [[Portfolio portfolio]]
   */
  def findPortfolioByID(portfolioID: String): js.Promise[HttpResponse[Portfolio]] = {
    $http.get(findPortfolioByIDURL(portfolioID))
  }

  def findPortfolioByUser(contestID: String, userID: String): js.Promise[HttpResponse[Portfolio]] = {
    $http.get(findPortfolioByUserURL(contestID, userID))
  }

  /**
   * Retrieves a collection of portfolios by a contest ID
   * @param contestID the given contest ID
   * @return the promise of an array of [[Portfolio portfolios]]
   */
  def findPortfoliosByContest(contestID: String): js.Promise[HttpResponse[js.Array[Portfolio]]] = {
    $http.get(findPortfoliosByContestURL(contestID))
  }

  /**
   * Retrieves a collection of portfolios by a player ID
   * @param userID the given player ID
   * @return the promise of an array of [[Portfolio portfolios]]
   */
  def findPortfoliosByUser(userID: String): js.Promise[HttpResponse[js.Array[Portfolio]]] = {
    $http.get(findPortfoliosByUserURL(userID))
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Perks
  /////////////////////////////////////////////////////////////////////////////

  /**
   * Retrieves the promise of an option of a perks response
   * @param portfolioID the given portfolio ID
   * @return the promise of an option of a [[PerksResponse perks response]]
   */
  def findPurchasedPerks(portfolioID: String): js.Promise[HttpResponse[PerksResponse]] = {
    $http.get(findPurchasedPerksURL(portfolioID))
  }

  /**
   * Attempts to purchase the given perk codes
   * @param portfolioID the given portfolio ID
   * @param perkCodes   the given perk codes to purchase
   * @return the promise of a [[Portfolio]]
   */
  def purchasePerks(portfolioID: String, perkCodes: js.Array[String]): js.Promise[HttpResponse[Portfolio]] = {
    $http.post(purchasePerksURL(portfolioID), perkCodes)
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Orders
  /////////////////////////////////////////////////////////////////////////////

  def cancelOrder(orderID: String): js.Promise[HttpResponse[Ok]] = $http.delete(cancelOrderURL(orderID))

  def createOrder(contestID: String, userID: String, order: NewOrderForm): js.Promise[HttpResponse[Ok]] = {
    $http.post(createOrderURL(contestID, userID), data = order)
  }

  def findOrderByID(orderID: String): js.Promise[HttpResponse[Order]] = $http.get(findOrderByIDURL(orderID))

  def orderSearch(options: OrderSearchOptions): js.Promise[HttpResponse[js.Array[Order]]] = $http.get(orderSearchURL(options))

  /////////////////////////////////////////////////////////////////////////////
  //			Positions
  /////////////////////////////////////////////////////////////////////////////

  def findPositionByID(positionID: String): js.Promise[HttpResponse[Position]] = {
    $http.get(findPositionByIDURL(positionID))
  }

  def findPositions(contestID: String, userID: String): js.Promise[HttpResponse[js.Array[Position]]] = {
    $http.get(findPositionsURL(contestID, userID))
  }

  def findHeldSecurities(portfolioID: String): js.Promise[HttpResponse[js.Array[String]]] = {
    $http.get(findHeldSecuritiesURL(portfolioID))
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Charts
  /////////////////////////////////////////////////////////////////////////////

  def findChart(contestID: String, userID: String, chart: String): js.Promise[HttpResponse[js.Array[ChartData]]] = {
    $http.get(findChartURL(contestID, userID, chart))
  }

}
