package com.shocktrade.webapp.vm.dao

import com.shocktrade.server.dao.DataAccessObjectHelper
import com.shocktrade.webapp.routes.contest.dao.PositionData
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Virtual Machine DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait VirtualMachineDAO {

  //////////////////////////////////////////////////////////////////
  //    Player Functions
  //////////////////////////////////////////////////////////////////

  def creditFunds(portfolioID: String, amount: Double): Future[Int]

  def debitFunds(portfolioID: String, amount: Double): Future[Int]

  def creditWallet(portfolioID: String, amount: Double): Future[Int]

  def debitWallet(portfolioID: String, amount: Double): Future[Int]

  def grantAward(portfolioID: String, awardCode: String): Future[Int]

  def grantXP(portfolioID: String, xp: Int): Future[Int]

  //////////////////////////////////////////////////////////////////
  //    Portfolio Functions
  //////////////////////////////////////////////////////////////////

  def closeContest(contestID: String): Future[js.Dictionary[Double]]

  def closePortfolio(portfolioID: String): Future[Double]

  def closePortfolios(contestID: String): Future[js.Dictionary[Double]]

  def completeOrder(orderID: String, fulfilled: Boolean, message: js.UndefOr[String]): Future[Int]

  def decreasePosition(orderID: String, position: PositionData, proceeds: Double): Future[Int]

  def increasePosition(orderID: String, position: PositionData, cost: Double): Future[Int]

  def liquidatePortfolio(portfolioID: String): Future[Double]

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
