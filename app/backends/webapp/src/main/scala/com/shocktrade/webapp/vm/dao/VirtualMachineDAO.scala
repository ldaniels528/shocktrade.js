package com.shocktrade.webapp.vm.dao

import com.shocktrade.common.Ok
import com.shocktrade.common.forms.{ContestCreationRequest, ContestCreationResponse}
import com.shocktrade.common.models.contest._
import com.shocktrade.common.models.user.UserRef
import com.shocktrade.server.dao.DataAccessObjectHelper
import com.shocktrade.webapp.routes.account.dao.{UserAccountData, UserIconData}
import com.shocktrade.webapp.routes.contest.dao.{ContestData, OrderData}
import com.shocktrade.webapp.vm.dao.VirtualMachineDAOMySQL.PortfolioEquity
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Virtual Machine DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait VirtualMachineDAO {

  //////////////////////////////////////////////////////////////////
  //    Contest Functions
  //////////////////////////////////////////////////////////////////

  def closeContest(contestID: String): Future[Ok]

  def createContest(request: ContestCreationRequest): Future[ContestCreationResponse]

  def joinContest(contestID: String, userID: String): Future[PortfolioRef]

  def quitContest(contestID: String, userID: String): Future[PortfolioEquity]

  def sendChatMessage(contestID: String, userID: String, message: String): Future[MessageRef]

  def startContest(contestID: String, userID: String): Future[Boolean]

  def updateContest(contest: ContestData): Future[Ok]

  //////////////////////////////////////////////////////////////////
  //    Player Functions
  //////////////////////////////////////////////////////////////////

  def creditWallet(portfolioID: String, amount: Double): Future[Ok]

  def debitWallet(portfolioID: String, amount: Double): Future[PortfolioEquity]

  def purchasePerks(portfolioID: String, purchasePerkCodes: js.Array[String]): Future[PurchasePerksResponse]

  //////////////////////////////////////////////////////////////////
  //    Portfolio Functions
  //////////////////////////////////////////////////////////////////

  def cancelOrder(orderID: String): Future[OrderOutcome]

  def completeOrder(orderID: String, fulfilled: Boolean, negotiatedPrice: js.UndefOr[Double], message: js.UndefOr[String]): Future[OrderOutcome]

  def createOrder(portfolioID: String, order: OrderData): Future[OrderRef]

  def decreasePosition(portfolioID: String, orderID: String, priceType: String, symbol: String, exchange: String, quantity: Double): Future[OrderOutcome]

  def increasePosition(portfolioID: String, orderID: String, priceType: String, symbol: String, exchange: String, quantity: Double): Future[OrderOutcome]

  def liquidatePortfolio(portfolioID: String): Future[ClosePortfolioResponse]

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    User / Account Management
  /////////////////////////////////////////////////////////////////////////////////////////////////

  def createUserIcon(icon: UserIconData)(implicit ec: ExecutionContext): Future[Ok]

  def createUserAccount(account: UserAccountData)(implicit ec: ExecutionContext): Future[UserRef]

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    System Functions
  /////////////////////////////////////////////////////////////////////////////////////////////////

  def trackEvent(event: EventSourceData): Future[Ok]

  def updateEventLog(): Future[Ok]

}

/**
 * Virtual Machine DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object VirtualMachineDAO {

  /**
   * Creates a new Virtual Machine DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[VirtualMachineDAO Virtual Machine DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions)(implicit ec: ExecutionContext): VirtualMachineDAO = new VirtualMachineDAOMySQL(options)

}
