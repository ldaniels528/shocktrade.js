package com.shocktrade.client.contest

import com.shocktrade.client.models.contest.{Order, Portfolio, Position}
import com.shocktrade.common.forms.{FundsTransferRequest, NewOrderForm}
import com.shocktrade.common.models.contest._
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
    * @param contestID the given contest ID
    * @param portfolioID  the given player ID
    * @return the promise of a [[Portfolio portfolio]]
    */
  def getPortfolioByPlayer(contestID: String, portfolioID: String): js.Promise[HttpResponse[Portfolio]] = {
    $http.get[Portfolio](s"/api/portfolio/contest/$contestID/player/$portfolioID")
  }

  /**
    * Retrieves a collection of portfolios by a contest ID
    * @param contestID the given contest ID
    * @return the promise of an array of [[Portfolio portfolios]]
    */
  def getPortfoliosByContest(contestID: String): js.Promise[HttpResponse[js.Array[Portfolio]]] = {
    $http.get[js.Array[Portfolio]](s"/api/portfolios/contest/$contestID")
  }

  /**
    * Retrieves a collection of portfolios by a player ID
    * @param portfolioID the given player ID
    * @return the promise of an array of [[Portfolio portfolios]]
    */
  def getPortfoliosByPlayer(portfolioID: String): js.Promise[HttpResponse[js.Array[Portfolio]]] = {
    $http.get[js.Array[Portfolio]](s"/api/portfolios/$portfolioID")
  }

  /**
    * Retrieves the rankings for the given contest by ID
    * @param contestId the given contest ID
    * @return the array of [[Participant rankings]]
    */
  def getRankings(contestId: String): js.Promise[HttpResponse[js.Array[Participant]]] = {
    $http.get[js.Array[Participant]](s"/api/portfolios/contest/$contestId/rankings")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Positions & Orders
  /////////////////////////////////////////////////////////////////////////////

  def cancelOrder(portfolioId: String, orderId: String): js.Promise[HttpResponse[Portfolio]] = {
    $http.delete[Portfolio](s"/api/portfolio/$portfolioId/order/$orderId")
  }

  def createOrder(portfolioId: String, order: NewOrderForm): js.Promise[HttpResponse[Portfolio]] = {
    $http.post[Portfolio](s"/api/portfolio/$portfolioId/order", data = order)
  }

  def getOrders(portfolioId: String): js.Promise[HttpResponse[js.Array[Order]]] = {
    $http.get[js.Array[Order]](s"/api/portfolio/$portfolioId/orders")
  }

  def getPositions(portfolioId: String): js.Promise[HttpResponse[js.Array[Position]]] = {
    $http.get[js.Array[Position]](s"/api/portfolio/$portfolioId/positions")
  }

  def getHeldSecurities(portfolioID: String): js.Promise[HttpResponse[js.Array[String]]] = {
    $http.get[js.Array[String]](s"/api/portfolio/$portfolioID/heldSecurities")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Cash & Margin Accounts
  /////////////////////////////////////////////////////////////////////////////

  def getCashAccountMarketValue(portfolioId: String): js.Promise[HttpResponse[MarketValueResponse]] = {
    $http.get[MarketValueResponse](s"/api/portfolio/$portfolioId/marketValue?accountType=cash")
  }

  def getMarginAccountMarketValue(portfolioId: String): js.Promise[HttpResponse[MarketValueResponse]] = {
    $http.get[MarketValueResponse](s"/api/portfolio/$portfolioId/marketValue?accountType=margin")
  }

  def getTotalInvestment(portfolioID: String): js.Promise[HttpResponse[TotalInvestment]] = {
    $http.get[TotalInvestment](s"/api/portfolios/$portfolioID/totalInvestment")
  }

  def transferFunds(portfolioId: String, form: FundsTransferRequest): js.Promise[HttpResponse[Portfolio]] = {
    $http.post[Portfolio](s"/api/portfolio/$portfolioId/transferFunds", form)
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Charts
  /////////////////////////////////////////////////////////////////////////////

  def getExposureChartData(contestID: String, portfolioID: String, exposure: String): js.Promise[HttpResponse[js.Array[js.Object]]] = {
    $http.get[js.Array[js.Object]](s"/api/charts/exposure/$exposure/$contestID/$portfolioID")
  }

  def getChart(contestID: String, playerName: String, chartName: String): js.Promise[HttpResponse[js.Dynamic]] = {
    // determine the chart type
    val chartType = if (chartName == "gains" || chartName == "losses") "performance" else "exposure"

    // load the chart representing the securities
    $http.get[js.Dynamic](s"/api/charts/$chartType/$chartName/$contestID/$playerName")
  }

}
