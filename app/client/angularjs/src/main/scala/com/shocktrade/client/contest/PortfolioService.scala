package com.shocktrade.client.contest

import com.shocktrade.client.models.contest.{Order, Portfolio}
import com.shocktrade.common.forms.{FundsTransferRequest, NewOrderForm}
import com.shocktrade.common.models.contest._
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http
import org.scalajs.dom._

import scala.scalajs.js

/**
  * Portfolio Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class PortfolioService($http: Http) extends Service {

  /**
    * Retrieves a portfolio by a contest ID and player ID
    * @param contestID the given contest ID
    * @param playerID  the given player ID
    * @return the promise of a [[Portfolio portfolio]]
    */
  def getPortfolioByPlayer(contestID: String, playerID: String) = {
    $http.get[Portfolio](s"/api/portfolio/contest/$contestID/player/$playerID")
  }

  /**
    * Retrieves a collection of portfolios by a contest ID
    * @param contestID the given contest ID
    * @return the promise of an array of [[Portfolio portfolios]]
    */
  def getPortfoliosByContest(contestID: String) = {
    $http.get[js.Array[Portfolio]](s"/api/portfolios/contest/$contestID")
  }

  /**
    * Retrieves a collection of portfolios by a player ID
    * @param playerID the given player ID
    * @return the promise of an array of [[Portfolio portfolios]]
    */
  def getPortfoliosByPlayer(playerID: String) = {
    $http.get[js.Array[Portfolio]](s"/api/portfolios/player/$playerID")
  }

  /**
    * Retrieves the rankings for the given contest by ID
    * @param contestId the given contest ID
    * @return the array of [[Participant rankings]]
    */
  def getRankings(contestId: String) = {
    $http.get[js.Array[Participant]](s"/api/portfolios/contest/$contestId/rankings")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Positions & Orders
  /////////////////////////////////////////////////////////////////////////////

  def cancelOrder(portfolioId: String, orderId: String) = {
    $http.delete[Portfolio](s"/api/portfolio/$portfolioId/order/$orderId")
  }

  def createOrder(portfolioId: String, order: NewOrderForm) = {
    $http.post[Portfolio](s"/api/portfolio/$portfolioId/order", data = order)
  }

  def getOrders(portfolioId: String) = {
    $http.get[js.Array[Order]](s"/api/portfolio/$portfolioId/orders")
  }

  def getPositions(portfolioId: String) = {
    $http.get[js.Array[Position]](s"/api/portfolio/$portfolioId/positions")
  }

  def getHeldSecurities(playerId: String) = {
    $http.get[js.Array[String]](s"/api/portfolio/$playerId/heldSecurities")
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Cash & Margin Accounts
  /////////////////////////////////////////////////////////////////////////////

  def getCashAccountMarketValue(portfolioId: String) = {
    $http.get[MarketValueResponse](s"/api/portfolio/$portfolioId/marketValue?accountType=cash")
  }

  def getMarginAccountMarketValue(portfolioId: String) = {
    $http.get[MarketValueResponse](s"/api/portfolio/$portfolioId/marketValue?accountType=margin")
  }

  def getTotalInvestment(playerID: String) = {
    $http.get[TotalInvestment](s"/api/portfolios/player/$playerID/totalInvestment")
  }

  def transferFunds(portfolioId: String, form: FundsTransferRequest) = {
    $http.post[Portfolio](s"/api/portfolio/$portfolioId/transferFunds", form)
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Charts
  /////////////////////////////////////////////////////////////////////////////

  def getExposureChartData(contestID: String, playerID: String, exposure: String) = {
    $http.get[js.Array[js.Object]](s"/api/charts/exposure/$exposure/$contestID/$playerID")
  }

  def getChart(contestID: String, playerName: String, chartName: String) = {
    // determine the chart type
    val chartType = if (chartName == "gains" || chartName == "losses") "performance" else "exposure"

    // load the chart representing the securities
    $http.get[js.Dynamic](s"/api/charts/$chartType/$chartName/$contestID/$playerName")
  }

}
