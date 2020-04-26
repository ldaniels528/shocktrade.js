package com.shocktrade.webapp.vm.dao

import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.dao.MySQLDAO
import com.shocktrade.webapp.routes.account.dao.UserProfileData
import com.shocktrade.webapp.routes.contest.dao.{PortfolioData, PositionData}
import io.scalajs.npm.mysql.MySQLConnectionOptions
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Virtual Machine DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class VirtualMachineDAOMySQL(options: MySQLConnectionOptions)(implicit ec: ExecutionContext)
  extends MySQLDAO(options) with VirtualMachineDAO {
  private val logger = LoggerFactory.getLogger(getClass)

  override def closeContest(contestID: String): Future[js.Dictionary[Double]] = {
    for {
      w <- stopContest(contestID)
      portfolioIDs <- findPortfolioIDsByContest(contestID)
      dict <- Future.sequence(portfolioIDs.toSeq.map(p => liquidatePortfolio(p).map(t => p -> t))).map(a => js.Dictionary(a: _*))
    } yield dict
  }

  private def findPortfolioIDsByContest(contestID: String): Future[js.Array[String]] = {
    conn.queryFuture[PortfolioData]("SELECT portfolioID FROM portfolios WHERE contestID = ?",
      js.Array(contestID)).map(_._1.flatMap(_.portfolioID.toOption))
  }

  override def completeOrder(orderID: String, fulfilled: Boolean, message: js.UndefOr[String] = js.undefined): Future[Int] = {
    val closed = true
    conn.executeFuture(
      """|UPDATE orders
         |SET closed = ?, fulfilled = ?, processedTime = ?, message = ?
         |WHERE orderID = ?
         |""".stripMargin, js.Array(closed, fulfilled, new js.Date(), message.orNull, orderID))
      .map(_.affectedRows) map checkCount(count => s"Order $orderID could not be closed: count = $count")
  }

  override def decreasePosition(orderID: String, position: PositionData, proceeds: Double): Future[Int] = {
    val portfolioID = position.portfolioID_!
    for {
      _ <- creditFunds(portfolioID, proceeds)
      w <- updatePosition(position, increase = false)
      _ <- completeOrder(orderID, fulfilled = true)
      _ <- increaseXP(portfolioID, xp = 1)
    } yield w
  }

  override def increasePosition(orderID: String, position: PositionData, cost: Double): Future[Int] = {
    val portfolioID = position.portfolioID_!
    val outcome = for {
      _ <- debitFunds(portfolioID, cost)
      w <- updatePosition(position, increase = true)
      _ <- completeOrder(orderID, fulfilled = true)
      _ <- increaseXP(portfolioID, xp = 1)
    } yield w

    outcome recoverWith {
      case InsufficientFundsException(actual, expected) =>
        completeOrder(orderID, fulfilled = false, message = s"Insufficient funds (cash: $actual, cost: $expected)")
    }
  }

  override def liquidatePortfolio(portfolioID: String): Future[Double] = {
    for {
      equity <- computePortfolioEquity(portfolioID)
      _ <- creditWallet(portfolioID, equity)
      _ <- closePositions(portfolioID)
    } yield equity
  }

  private def closePositions(portfolioID: String): Future[Int] = {
    conn.executeFuture("UPDATE positions SET quantity = 0 WHERE portfolioID = ?", js.Array(portfolioID))
      .map(_.affectedRows) map checkCount(count => s"Portfolio $portfolioID could not be closed: count = $count")
  }

  private def computePortfolioEquity(portfolioID: String): Future[Double] = {
    conn.queryFuture[PortfolioData](
      """|SELECT SUM(S.lastTrade) AS funds
         |FROM positions P
         |INNER JOIN stocks S ON S.symbol = P.symbol AND S.exchange = P.exchange
         |WHERE P.portfolioID = ?
         |""".stripMargin, js.Array(portfolioID)).map(_._1.flatMap(_.funds.toOption).headOption) map {
      case Some(funds) => funds
      case None => 0.0
    }
  }

  override def creditFunds(portfolioID: String, amount: Double): Future[Int] = debitFunds(portfolioID, -amount)

  override def debitFunds(portfolioID: String, amount: Double): Future[Int] = {
    for {
      // lookup the portfolio
      portfolio_? <- conn.queryFuture[PortfolioData](
        "SELECT funds FROM portfolios WHERE portfolioID = ?", js.Array(portfolioID))
        .map(_._1).map(_.headOption)

      // get the portfolio's funds
      funds = portfolio_?.flatMap(_.funds.toOption) match {
        case Some(funds) if funds < amount => throw InsufficientFundsException(funds, amount)
        case Some(funds) => funds
        case None => throw PortfolioNotFoundException(portfolioID)
      }

      // perform the update
      count <- conn.executeFuture(
        """|UPDATE portfolios SET funds = funds - ? WHERE portfolioID = ? AND funds >= ?
           |""".stripMargin, js.Array(amount, portfolioID, amount)).map(_.affectedRows)
    } yield count
  }

  override def creditWallet(portfolioID: String, amount: Double): Future[Int] = debitWallet(portfolioID, -amount)

  override def debitWallet(portfolioID: String, amount: Double): Future[Int] = {
    for {
      // lookup the user's wallet by portfolio ID
      user_? <- conn.queryFuture[UserProfileData](
        """|SELECT U.userID, wallet FROM users U INNER JOIN portfolios P ON P.userID = U.userID WHERE portfolioID = ?
           |""".stripMargin, js.Array(portfolioID))
        .map(_._1).map(_.headOption)

      // get the user's wallet
      result = for {userID <- user_?.flatMap(_.userID.toOption); wallet <- user_?.flatMap(_.wallet.toOption)} yield (userID, wallet)
      (userID, wallet) = result match {
        case Some((_, wallet)) if wallet < amount => throw InsufficientFundsException(wallet, amount)
        case Some((userID, wallet)) => (userID, wallet)
        case None => throw PortfolioNotFoundException(portfolioID)
      }

      // perform the update
      count <- conn.executeFuture("UPDATE users SET wallet = wallet - ? WHERE userID = ?", js.Array(amount, userID))
        .map(_.affectedRows) map checkCount(_ => f"Portfolio $portfolioID could not be credit wallet with $amount%.2f")
    } yield count
  }

  override def increaseXP(portfolioID: String, xp: Int): Future[Int] = {
    conn.executeFuture(
      """|UPDATE users U
         |INNER JOIN portfolios P ON P.userID = U.userID
         |SET U.totalXP = U.totalXP + ?
         |WHERE P.portfolioID = ?
         |""".stripMargin, js.Array(xp, portfolioID))
      .map(_.affectedRows) map checkCount(count => s"Total XP could not be updated: count = $count")
  }

  private def stopContest(contestID: String): Future[Int] = {
    conn.executeFuture(
      """|UPDATE contests C
         |INNER JOIN contest_statuses CS ON CS.status = 'CLOSED'
         |INNER JOIN order_price_types OPT ON OPT.name = 'MARKET_AT_CLOSE'
         |INNER JOIN portfolios P ON P.contestID = C.contestID
         |INNER JOIN users U ON U.userID = P.userID
         |LEFT  JOIN positions PS ON PS.portfolioID = P.portfolioID
         |LEFT  JOIN stocks S ON S.symbol = PS.symbol AND S.exchange = PS.exchange
         |SET
         |   C.closedTime = now(),
         |   C.statusID = CS.statusID,
         |   PS.quantity = 0,
         |   U.wallet = U.wallet + P.funds + IFNULL(S.lastTrade * PS.quantity, 0) - OPT.commission
         |WHERE C.contestID = ?
         |""".stripMargin, js.Array(contestID))
      .map(_.affectedRows) map checkCount(count => s"Contest $contestID could not be closed: count = $count")
  }

  private def updatePosition(position: PositionData, increase: Boolean): Future[Int] = {
    import position._
    val _processedTime = processedTime ?? new js.Date()
    if (increase) {
      conn.executeFuture(
        """|INSERT INTO positions (positionID, portfolioID, symbol, exchange, quantity, processedTime)
           |VALUES (?, ?, ?, ?, ?, ?)
           |ON DUPLICATE KEY UPDATE quantity = quantity + ?, processedTime = ?
           |""".stripMargin, js.Array(
          /* insert */ newID, portfolioID, symbol, exchange, quantity, _processedTime,
          /* update */ quantity, _processedTime
        )).map(_.affectedRows) map checkCount(count => s"Failed to increase position: count = $count")
    } else {
      conn.executeFuture(
        """|UPDATE positions
           |SET quantity = quantity - ?, processedTime = ?
           |WHERE portfolioID = ?
           |AND symbol = ?
           |AND quantity >= ?
           |""".stripMargin, js.Array(quantity, _processedTime, portfolioID, symbol, quantity))
        .map(_.affectedRows) map checkCount(count => s"Failed to decrease position: count = $count")
    }
  }

}
