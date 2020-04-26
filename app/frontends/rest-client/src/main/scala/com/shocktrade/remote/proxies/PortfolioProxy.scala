package com.shocktrade.remote.proxies

import com.shocktrade.common.Ok
import com.shocktrade.common.api.PortfolioAPI
import com.shocktrade.common.forms.{NewOrderForm, PerksResponse}
import com.shocktrade.common.models.contest._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Portfolio Proxy
 * @param host the given host
 * @param port the given port
 */
class PortfolioProxy(host: String, port: Int)(implicit ec: ExecutionContext) extends Proxy with PortfolioAPI {
  override val baseURL = s"http://$host:$port"

  ///////////////////////////////////////////////////////////////
  //          Portfolio Lifecycle
  ///////////////////////////////////////////////////////////////

  /**
   * Retrieves a portfolio by ID
   * @param portfolioID the given portfolio ID
   * @return the promise of a [[Portfolio portfolio]]
   */
  def findPortfolioByID(portfolioID: String): Future[Portfolio] = get(findPortfolioByIDURL(portfolioID))

  /**
   * Retrieves a portfolio by a contest ID and user ID
   * @param contestID the given contest ID
   * @param userID the given user ID
   * @return the promise of a [[Portfolio portfolio]]
   */
  def findPortfolioByUser(contestID: String, userID: String): Future[Portfolio] = get(findPortfolioByUserURL(contestID, userID))

  /**
   * Retrieves a collection of portfolios by a contest ID
   * @param contestID the given contest ID
   * @return the promise of an array of [[Portfolio portfolios]]
   */
  def findPortfoliosByContest(contestID: String): Future[js.Array[Portfolio]] = get(findPortfoliosByContestURL(contestID))

  /**
   * Retrieves a collection of portfolios by a player ID
   * @param userID the given player ID
   * @return the promise of an array of [[Portfolio portfolios]]
   */
  def findPortfoliosByUser(userID: String): Future[js.Array[Portfolio]] = get(findPortfoliosByUserURL(userID))

  ///////////////////////////////////////////////////////////////
  //          Miscellaneous
  ///////////////////////////////////////////////////////////////

  def findPortfolioBalance(contestID: String, userID: String): Future[PortfolioBalance] = get(findPortfolioBalanceURL(contestID, userID))

  def findChart(contestID: String, userID: String, chart: String): Future[Portfolio] = get(findChartURL(contestID, userID, chart))

  ///////////////////////////////////////////////////////////////
  //          Positions & Orders
  ///////////////////////////////////////////////////////////////

  def cancelOrder(portfolioID: String, orderID: String): Future[Portfolio] = delete(cancelOrderURL(portfolioID, orderID))

  def createOrder(contestID: String, userID: String, order: NewOrderForm): Future[Ok] = post(createOrderURL(contestID, userID), data = order)

  def createOrderByID(portfolioID: String, order: NewOrderForm): Future[Ok] = post(createOrderByIDURL(portfolioID), data = order)

  def findHeldSecurities(portfolioID: String): Future[js.Array[String]] = get(findHeldSecuritiesURL(portfolioID))

  def findOrders(contestID: String, userID: String): Future[js.Array[Order]] = get(findOrdersURL(contestID, userID))

  def findPositions(contestID: String, userID: String): Future[js.Array[Position]] = get(findPositionsURL(contestID, userID))

  ///////////////////////////////////////////////////////////////
  //          Perks
  ///////////////////////////////////////////////////////////////

  /**
   * Retrieves the promise of a sequence of available perks
   * @return the promise of a sequence of available [[Perk perk]]s
   */
  def findAvailablePerks: Future[js.Array[Perk]] = get(findAvailablePerksURL)

  /**
   * Retrieves the promise of an option of a perks response
   * @param portfolioID the given portfolio ID
   * @return the promise of an option of a [[PerksResponse perks response]]
   */
  def findPurchasedPerks(portfolioID: String): Future[PerksResponse] = get(findPurchasedPerksURL(portfolioID))

  /**
   * Attempts to purchase the given perk codes
   * @param portfolioID the given portfolio ID
   * @param perkCodes   the given perk codes to purchase
   * @return the promise of a [[Portfolio]]
   */
  def purchasePerks(portfolioID: String, perkCodes: js.Array[String]): Future[Portfolio] = post(purchasePerksURL(portfolioID), perkCodes)

}
