package com.shocktrade.client.contest

import com.shocktrade.client.models.contest.{Order, Portfolio, Position}
import com.shocktrade.common.Ok
import com.shocktrade.common.forms.NewOrderForm
import com.shocktrade.common.models.contest.{ChartData, _}
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.scalajs.js

/**
 * Portfolio Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PortfolioService($http: Http) extends Service {

  /**
   * Retrieves a portfolio by a contest ID and player ID
   * @param portfolioID the given player ID
   * @return the promise of a [[Portfolio portfolio]]
   */
  def getPortfolioByPlayer(portfolioID: String): js.Promise[HttpResponse[Portfolio]] = {
    $http.get(s"/api/portfolio/$portfolioID")
  }

  /**
   * Retrieves a collection of portfolios by a contest ID
   * @param contestID the given contest ID
   * @return the promise of an array of [[Portfolio portfolios]]
   */
  def getPortfoliosByContest(contestID: String): js.Promise[HttpResponse[js.Array[Portfolio]]] = {
    $http.get(s"/api/portfolios/contest/$contestID")
  }

  /**
   * Retrieves a collection of portfolios by a player ID
   * @param portfolioID the given player ID
   * @return the promise of an array of [[Portfolio portfolios]]
   */
  def getPortfoliosByPlayer(portfolioID: String): js.Promise[HttpResponse[js.Array[Portfolio]]] = {
    $http.get(s"/api/portfolios/$portfolioID")
  }

  def findPortfolio(contestID: String, userID: String): js.Promise[HttpResponse[Portfolio]] = {
    $http.get(s"/api/portfolio/contest/$contestID/user/$userID")
  }

  def findPortfolioBalance(contestID: String, userID: String): js.Promise[HttpResponse[PortfolioBalance]] = {
    $http.get(s"/api/portfolio/contest/$contestID/user/$userID/balance")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Positions & Orders
  /////////////////////////////////////////////////////////////////////////////

  def cancelOrder(portfolioId: String, orderId: String): js.Promise[HttpResponse[Portfolio]] = {
    $http.delete(s"/api/portfolio/$portfolioId/order/$orderId")
  }

  def createOrder(contestID: String, userID: String, order: NewOrderForm): js.Promise[HttpResponse[Ok]] = {
    $http.post(s"/api/order/$contestID/user/$userID", data = order)
  }

  def findOrders(contestID: String, userID: String): js.Promise[HttpResponse[js.Array[Order]]] = {
    $http.get(s"/api/orders/$contestID/user/$userID")
  }

  def findPositions(contestID: String, userID: String): js.Promise[HttpResponse[js.Array[Position]]] = {
    $http.get(s"/api/positions/$contestID/user/$userID")
  }

  def findHeldSecurities(portfolioID: String): js.Promise[HttpResponse[js.Array[String]]] = {
    $http.get(s"/api/portfolio/$portfolioID/heldSecurities")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Charts
  /////////////////////////////////////////////////////////////////////////////

  def findChartData(contestID: String, userID: String, chart: String): js.Promise[HttpResponse[js.Array[ChartData]]] = {
    $http.get(s"/api/contest/$contestID/user/$userID/chart/$chart")
  }

}
