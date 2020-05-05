package com.shocktrade.webapp.vm.dao

import com.shocktrade.common.forms.{ContestCreationRequest, ContestCreationResponse}
import com.shocktrade.common.models.contest.{MessageRef, OrderRef, PortfolioRef}
import com.shocktrade.common.models.user.UserRef
import com.shocktrade.server.dao.DataAccessObjectHelper
import com.shocktrade.webapp.routes.account.dao.{UserAccountData, UserIconData}
import com.shocktrade.webapp.routes.contest.dao.{ContestData, OrderData}
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

  def closeContest(contestID: String): Future[js.Dictionary[Double]]

  def createContest(request: ContestCreationRequest): Future[ContestCreationResponse]

  def joinContest(contestID: String, userID: String): Future[PortfolioRef]

  def quitContest(contestID: String, userID: String): Future[Double]

  def sendChatMessage(contestID: String, userID: String, message: String): Future[MessageRef]

  def startContest(contestID: String, userID: String): Future[Boolean]

  def updateContest(contest: ContestData): Future[Int]

  //////////////////////////////////////////////////////////////////
  //    Player Functions
  //////////////////////////////////////////////////////////////////

  def creditWallet(portfolioID: String, amount: Double): Future[Double]

  def debitWallet(portfolioID: String, amount: Double): Future[Double]

  def grantAwards(portfolioID: String, awardCode: js.Array[String]): Future[Int]

  def grantXP(portfolioID: String, xp: Int): Future[Int]

  def purchasePerks(portfolioID: String, purchasePerkCodes: js.Array[String]): Future[Int]

  //////////////////////////////////////////////////////////////////
  //    Portfolio Functions
  //////////////////////////////////////////////////////////////////

  def cancelOrder(orderID: String): Future[Int]

  def closePortfolio(portfolioID: String): Future[Double]

  def completeOrder(orderID: String, fulfilled: Boolean, message: js.UndefOr[String]): Future[Int]

  def createOrder(portfolioID: String, order: OrderData): Future[OrderRef]

  def creditPortfolio(portfolioID: String, amount: Double): Future[Double]

  def debitPortfolio(portfolioID: String, amount: Double): Future[Double]

  def decreasePosition(orderID: String, position: PositionData, proceeds: Double): Future[Int]

  def increasePosition(orderID: String, position: PositionData, cost: Double): Future[Int]

  def liquidatePortfolio(portfolioID: String): Future[Double]

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    User / Account Management
  /////////////////////////////////////////////////////////////////////////////////////////////////

  def createUserIcon(icon: UserIconData)(implicit ec: ExecutionContext): Future[Int]

  def createUserAccount(account: UserAccountData)(implicit ec: ExecutionContext): Future[UserRef]

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    System Functions
  /////////////////////////////////////////////////////////////////////////////////////////////////

  def trackEvent(event: EventSourceData): Future[Int]

  def updateEventLog(): Future[Int]

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
