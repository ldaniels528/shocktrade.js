package com.shocktrade.webapp.vm.dao

import java.util.UUID

import com.shocktrade.common.forms.{ContestCreationRequest, ContestCreationResponse}
import com.shocktrade.common.models.Award
import com.shocktrade.common.models.contest.{ContestRanking, Perk}
import com.shocktrade.server.dao.MySQLDAO
import com.shocktrade.webapp.routes.account.dao.{UserAccountData, UserIconData, UserProfileData}
import com.shocktrade.webapp.routes.contest.dao.{ContestData, OrderData, PortfolioData, PositionData}
import com.shocktrade.webapp.vm.dao.VirtualMachineDAOMySQL.{AwardsRecommendation, Proceeds, _}
import io.scalajs.npm.mysql.MySQLConnectionOptions
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * Virtual Machine DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class VirtualMachineDAOMySQL(options: MySQLConnectionOptions)(implicit ec: ExecutionContext) extends MySQLDAO(options)
  with VirtualMachineDAO {

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    Account Management
  /////////////////////////////////////////////////////////////////////////////////////////////////

  override def createIcon(icon: UserIconData)(implicit ec: ExecutionContext): Future[Int] = {
    import icon._
    conn.executeFuture(
      """|REPLACE INTO user_icons (userID, name, mime, image)
         |VALUES (?, ?, ?, ?)
         |""".stripMargin,
      js.Array(userID, name, mime, image)).map(_.affectedRows)
  }

  override def createUserAccount(account: UserAccountData)(implicit ec: ExecutionContext): Future[Int] = {
    import account._
    conn.executeFuture(
      """|INSERT INTO users (userID, username, email, password, wallet)
         |VALUES (uuid(), ?, ?, ?, ?)
         |""".stripMargin,
      js.Array(username, email, password, wallet)).map(_.affectedRows) map checkInsertCount
  }

  //////////////////////////////////////////////////////////////////
  //    Contest Functions
  //////////////////////////////////////////////////////////////////

  override def createContest(request: ContestCreationRequest): Future[ContestCreationResponse] = {
    // define the contestID, portfolioID and required parameters
    val params = for {
      name <- request.name.flat.toOption
      userID <- request.userID.flat.toOption
      duration <- request.duration.flat.toOption
      startingBalance <- request.startingBalance.flat.toOption
    } yield (name, userID, startingBalance, duration)

    // create the contest
    params match {
      case Some((name, userID, startingBalance, duration)) =>
        for {
          contestID <- insertContest(userID, name, startingBalance, duration)(request)
          portfolioID <- insertPortfolio(contestID, userID, startingBalance)
          _ <- debitWallet(portfolioID, startingBalance)
          _ <- sendChatMessage(contestID, userID, message = s"Welcome to $name!") map checkInsertCount
        } yield new ContestCreationResponse(contestID, portfolioID)
      case None =>
        Future.failed(js.JavaScriptException("Required parameters are missing"))
    }
  }

  override def joinContest(contestID: String, userID: String): Future[String] = {
    for {
      startingBalance <- findEntryFeeByContestID(contestID)
      portfolioID <- insertPortfolio(contestID = contestID, userID = userID, funds = startingBalance)
      _ <- debitWallet(portfolioID, startingBalance)
    } yield portfolioID
  }

  override def quitContest(contestID: String, userID: String): Future[Double] = {
    for {
      cp <- findContestAndPortfolioID(contestID, userID)
      proceeds <- liquidatePortfolio(cp.portfolioID)
      _ <- creditWallet(cp.portfolioID, proceeds)
    } yield proceeds
  }

  override def sendChatMessage(contestID: String, userID: String, message: String): Future[Int] = {
    conn.executeFuture(
      """|INSERT INTO messages (messageID, contestID, userID, message)
         |VALUES (uuid(), ?, ?, ?)
         |""".stripMargin,
      js.Array(contestID, userID, message)) map (_.affectedRows)
  }

  override def startContest(contestID: String, userID: String): Future[Boolean] = {
    conn.executeFuture(
      s"""|UPDATE portfolios P
          |INNER JOIN contest_statuses CS ON CS.status = 'ACTIVE'
          |SET startTime = now(), statusID = CS.statusID
          |WHERE contestID = ? AND hostUserID = ? AND statusID <> CS.statusID
          |""".stripMargin,
      js.Array(contestID, userID)) map (_.affectedRows > 0)
  }

  override def updateContest(contest: ContestData): Future[Int] = {
    import contest._
    conn.executeFuture("UPDATE contests SET name = ? WHERE contestID = ?", js.Array(name, contestID)).map(_.affectedRows)
  }

  //////////////////////////////////////////////////////////////////
  //    Player Functions
  //////////////////////////////////////////////////////////////////

  override def creditWallet(portfolioID: String, amount: Double): Future[Double] = debitWallet(portfolioID, -amount)

  override def debitWallet(portfolioID: String, amount: Double): Future[Double] = {
    for {
      // lookup the user's wallet by portfolio ID
      user_? <- conn.queryFuture[UserProfileData](
        "SELECT U.userID, wallet FROM users U INNER JOIN portfolios P ON P.userID = U.userID WHERE portfolioID = ?",
        js.Array(portfolioID)).map(_._1).map(_.headOption)

      // get the user's wallet
      result = for {userID <- user_?.flatMap(_.userID.toOption); wallet <- user_?.flatMap(_.wallet.toOption)} yield (userID, wallet)
      (userID, wallet) = result match {
        case Some((_, wallet)) if wallet < amount => throw InsufficientFundsException(wallet, amount)
        case Some((userID, wallet)) => (userID, wallet)
        case None => throw PortfolioNotFoundException(portfolioID)
      }

      // perform the update
      _ <- conn.executeFuture("UPDATE users SET wallet = wallet - ? WHERE userID = ?", js.Array(amount, userID))
        .map(_.affectedRows) map checkCount(_ => f"Portfolio $portfolioID could not be credit wallet with $amount%.2f")
    } yield wallet - amount
  }

  override def grantAwards(portfolioID: String, awardCodes: js.Array[String]): Future[Int] = {
    Future.sequence(awardCodes.toSeq map { awardCode =>
      conn.executeFuture(
        """|REPLACE INTO user_awards (userAwardID, userID, awardCode)
           |SELECT uuid(), userID, ? FROM portfolios WHERE portfolioID = ?
           |""".stripMargin, js.Array(awardCode, portfolioID))
        .map(_.affectedRows) map checkCount(count => s"Portfolio $portfolioID could not grant award [${awardCodes.mkString(",")}]: count = $count")
    }) map (_.sum)
  }

  override def grantXP(portfolioID: String, xp: Int): Future[Int] = {
    conn.executeFuture(
      """|UPDATE users U
         |INNER JOIN portfolios P ON P.userID = U.userID
         |SET U.totalXP = U.totalXP + ?
         |WHERE P.portfolioID = ?
         |""".stripMargin, js.Array(xp, portfolioID))
      .map(_.affectedRows) map checkCount(count => s"Total XP could not be updated: count = $count")
  }

  override def purchasePerks(portfolioID: String, perkCodes: js.Array[String]): Future[Int] = {
    val perks = Perk.availablePerks
    val perkMapping = js.Dictionary(perks.map(p => p.code -> p): _ *)
    val perksCost = (perkCodes flatMap perkMapping.get).map(_.cost).sum

    for {
      _ <- debitPortfolio(portfolioID, perksCost)
      count <- insertPerks(portfolioID, perkCodes)
    } yield count
  }

  //////////////////////////////////////////////////////////////////
  //    Portfolio Functions
  //////////////////////////////////////////////////////////////////

  override def cancelOrder(orderID: String): Future[Int] = {
    completeOrder(orderID, fulfilled = false, message = "Canceled by user")
  }

  override def closeContest(contestID: String): Future[js.Dictionary[Double]] = {
    for {
      _ <- conn.beginTransactionFuture()
      dict <- closePortfolios(contestID)
      _ <- stopContest(contestID)
      _ <- conn.commitFuture()
    } yield dict
  }

  override def closePortfolio(portfolioID: String): Future[Double] = {
    for {
      _ <- completeOrders(portfolioID, message = "Portfolio is closed.")
      funds <- liquidatePortfolio(portfolioID)
      rankings <- findPortfolioRankings(portfolioID).map(ContestRanking.computeRankings(_))
      recommendation = determineAwards(portfolioID, rankings)
      _ <- updateIf(recommendation.awardedXP > 0)(() => grantXP(portfolioID, recommendation.awardedXP))
      _ <- updateIf(recommendation.awardCodes.nonEmpty)(() => grantAwards(portfolioID, recommendation.awardCodes))
    } yield funds
  }

  override def closePortfolios(contestID: String): Future[js.Dictionary[Double]] = {
    for {
      portfolioIDs <- findPortfolioIDsByContest(contestID)
      summaries <- Future.sequence(portfolioIDs.toList.map { pid =>
        closePortfolio(pid).map(wealth => pid -> wealth)
      })
    } yield js.Dictionary(summaries: _*)
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

  override def createOrder(portfolioID: String, order: OrderData): Future[Int] = {
    import order._
    for {
      _ <- ensurePortfolioIsOpen(portfolioID)
      w <- conn.executeFuture(
        """|INSERT INTO orders (orderID, portfolioID, symbol, exchange, orderType, priceType, price, quantity)
           |VALUES (uuid(), ?, ?, ?, ?, ?, ?, ?)
           |""".stripMargin,
        js.Array(portfolioID, symbol, exchange, orderType, priceType, price, quantity)) map (_.affectedRows)
    } yield w
  }

  override def creditPortfolio(portfolioID: String, amount: Double): Future[Double] = debitPortfolio(portfolioID, -amount)

  override def debitPortfolio(portfolioID: String, amount: Double): Future[Double] = {
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
      _ <- conn.executeFuture(
        """|UPDATE portfolios SET funds = funds - ? WHERE portfolioID = ? AND funds >= ?
           |""".stripMargin, js.Array(amount, portfolioID, amount)).map(_.affectedRows)
    } yield funds - amount
  }

  override def decreasePosition(orderID: String, position: PositionData, proceeds: Double): Future[Int] = {
    val portfolioID = position.portfolioID_!
    val outcome = for {
      _ <- ensurePortfolioIsOpen(portfolioID)
      _ <- creditPortfolio(portfolioID, proceeds)
      w <- updatePosition(position, increase = false)
      _ <- completeOrder(orderID, fulfilled = true)
      _ <- grantXP(portfolioID, xp = 10)
    } yield w

    outcome recoverWith {
      case PortfolioClosedException(portfolioID) =>
        completeOrder(orderID, fulfilled = false, message = s"Portfolio $portfolioID is closed")
      case PortfolioNotFoundException(portfolioID) =>
        completeOrder(orderID, fulfilled = false, message = s"Portfolio $portfolioID was not found")
    }
  }

  override def increasePosition(orderID: String, position: PositionData, cost: Double): Future[Int] = {
    val portfolioID = position.portfolioID_!
    val outcome = for {
      _ <- ensurePortfolioIsOpen(portfolioID)
      _ <- debitPortfolio(portfolioID, cost)
      w <- updatePosition(position, increase = true)
      _ <- completeOrder(orderID, fulfilled = true)
      _ <- grantXP(portfolioID, xp = 5)
    } yield w

    outcome recoverWith {
      case InsufficientFundsException(actual, expected) =>
        completeOrder(orderID, fulfilled = false, message = s"Insufficient funds (cash: $actual, cost: $expected)")
      case PortfolioClosedException(portfolioID) =>
        completeOrder(orderID, fulfilled = false, message = s"Portfolio $portfolioID is closed")
      case PortfolioNotFoundException(portfolioID) =>
        completeOrder(orderID, fulfilled = false, message = s"Portfolio $portfolioID was not found")
    }
  }

  override def liquidatePortfolio(portfolioID: String): Future[Double] = {
    for {
      _ <- ensurePortfolioIsOpen(portfolioID)
      proceeds <- computePortfolioEquity(portfolioID)
      _ <- creditPortfolio(portfolioID, proceeds.equity)
      _ <- creditWallet(portfolioID, proceeds.equity + proceeds.cash)
      _ <- closePositions(portfolioID)
    } yield proceeds.equity + proceeds.cash
  }

  //////////////////////////////////////////////////////////////////
  //    Utility Functions
  //////////////////////////////////////////////////////////////////

  private def closePositions(portfolioID: String): Future[Int] = {
    conn.executeFuture("UPDATE positions SET quantity = 0 WHERE portfolioID = ?", js.Array(portfolioID))
      .map(_.affectedRows) map checkCount(count => s"Portfolio $portfolioID could not be closed: count = $count")
  }

  private def completeOrders(portfolioID: String, message: String): Future[Int] = {
    val (closed, fulfilled) = (false, false)
    conn.executeFuture(
      """|UPDATE orders
         |SET closed = ?, fulfilled = ?, processedTime = ?, message = ?
         |WHERE portfolioID = ?
         |""".stripMargin, js.Array(closed, fulfilled, new js.Date(), message, portfolioID))
      .map(_.affectedRows)
  }

  private def computePortfolioEquity(portfolioID: String): Future[Proceeds] = {
    conn.queryFuture[Proceeds](
      """|SELECT P.funds AS cash, SUM(S.lastTrade) AS equity
         |FROM positions PS
         |INNER JOIN portfolios P ON P.portfolioID = PS.portfolioID
         |INNER JOIN stocks S ON S.symbol = PS.symbol AND S.exchange = PS.exchange
         |WHERE PS.portfolioID = ?
         |""".stripMargin, js.Array(portfolioID)).map(_._1.headOption) map {
      case Some(proceeds) => proceeds
      case None => new Proceeds(cash = 0.0, equity = 0.0)
    }
  }

  private def determineAwards(portfolioID: String, rankings: Seq[ContestRanking]): AwardsRecommendation = {
    var items: List[(String, Int)] = Nil
    val myRanking = rankings.find(_.portfolioID.contains(portfolioID))
    items = Award.CHKDFLAG -> 5 :: items
    items = (myRanking collect { case ranking if ranking.rankNum.orZero == 1 => Award.GLDTRPHY -> 25 }).toList ::: items
    items = (myRanking collect { case ranking if ranking.gainLoss.orZero >= 25.0 => Award.PAYDIRT -> 25 }).toList ::: items
    items = (myRanking collect { case ranking if ranking.gainLoss.orZero >= 50.0 => Award.MADMONEY -> 50 }).toList ::: items
    items = (myRanking collect { case ranking if ranking.gainLoss.orZero >= 100.0 => Award.CRYSTBAL -> 100 }).toList ::: items
    new AwardsRecommendation(awardCodes = items.map(_._1).toJSArray, awardedXP = items.map(_._2).sum)
  }

  private def ensurePortfolioIsOpen(portfolioID: String): Future[Boolean] = {
    conn.queryFuture[PortfolioData]("SELECT closedTime FROM portfolios WHERE portfolioID = ?", js.Array(portfolioID))
      .map(_._1).map(_.headOption) flatMap {
      case Some(portfolio) if portfolio.closedTime.flat.nonEmpty => Future.failed(PortfolioClosedException(portfolioID))
      case Some(_) => Future.successful(true)
      case None => Future.failed(PortfolioNotFoundException(portfolioID))
    }
  }

  private def findEntryFeeByContestID(contestID: String): Future[Double] = {
    conn.queryFuture[ContestData]("SELECT startingBalance FROM contests WHERE contestID = ?",
      js.Array(contestID)).map(_._1.headOption) map {
      case Some(contest) => contest.startingBalance.getOrElse(throw js.JavaScriptException("startingBalance is required"))
      case None => throw js.JavaScriptException(s"Contest $contestID not found")
    }
  }

  private def findPortfolioIDsByContest(contestID: String): Future[js.Array[String]] = {
    conn.queryFuture[PortfolioData]("SELECT portfolioID FROM portfolios WHERE contestID = ?",
      js.Array(contestID)).map(_._1.flatMap(_.portfolioID.toOption))
  }

  private def findContestAndPortfolioID(contestID: String, userID: String): Future[ContestPortfolioData] = {
    conn.queryFuture[ContestPortfolioData]("SELECT portfolioID FROM portfolios WHERE contestID = ? AND userID = ?",
      js.Array(contestID, userID)).map(_._1.headOption) map {
      case Some(results) => results
      case None => throw ContestPortfolioNotFoundException(contestID, userID)
    }
  }

  private def findPortfolioRankings(portfolioID: String): Future[js.Array[ContestRanking]] = {
    conn.queryFuture[ContestRanking]("SELECT * FROM contest_rankings WHERE portfolioID = ?", js.Array(portfolioID))
      .map { case (rows, _) => rows }
  }

  private def insertContest(userID: String, name: String, startingBalance: Double, duration: Int)(request: ContestCreationRequest): Future[String] = {
    val contestID = UUID.randomUUID().toString
    conn.executeFuture(
      """|INSERT INTO contests (
         |  contestID, hostUserID, name, statusID, startingBalance, expirationTime,
         |  friendsOnly, invitationOnly, levelCap, perksAllowed, robotsAllowed
         |)
         |SELECT ?, ?, ?, CS.statusID, ?, ?, ?, ?, ?, ?, ?
         |FROM contest_statuses CS
         |WHERE CS.status = 'ACTIVE'
         |""".stripMargin,
      js.Array(contestID, userID, name, startingBalance,
        new js.Date(js.Date.now() + duration * 1.day.toMillis), request.friendsOnly ?? false, request.invitationOnly ?? false,
        request.levelCap ?? 0, request.perksAllowed ?? true, request.robotsAllowed ?? true)).map(_.affectedRows) map {
      case count if count > 0 => contestID
      case count => throw js.JavaScriptException(s"Contest not created (count = $count)")
    }
  }

  private def insertPerks(portfolioID: String, perkCodes: js.Array[String]): Future[Int] = {
    Future.sequence(perkCodes.toSeq map { perkCode =>
      conn.executeFuture("INSERT INTO perks (perkID, portfolioID, perkCode) VALUES (uuid(), ?, ?)",
        js.Array(portfolioID, perkCode)).map(_.affectedRows)
    }) map (_.sum)
  }

  private def insertPortfolio(contestID: String, userID: String, funds: Double): Future[String] = {
    val portfolioID = UUID.randomUUID().toString
    conn.executeFuture("INSERT INTO portfolios (contestID, portfolioID, userID, funds) VALUES (?, ?, ?, ?)",
      js.Array(contestID, portfolioID, userID, funds)).map(_.affectedRows) map {
      case count if count > 0 => portfolioID
      case _ => throw ContestPortfolioNotFoundException(contestID, userID)
    }
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
         |   P.closedTime = now(),
         |   PS.quantity = 0,
         |   U.wallet = U.wallet + P.funds + IFNULL(S.lastTrade * PS.quantity, 0) - OPT.commission
         |WHERE C.contestID = ?
         |""".stripMargin, js.Array(contestID))
      .map(_.affectedRows) map checkCount(count => s"Contest $contestID could not be closed: count = $count")
  }

  private def updateIf(condition: Boolean)(task: () => Future[Int]): Future[Int] = {
    if (condition) task() else Future.successful(1)
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

/**
 * Virtual Machine DAO MySQL Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object VirtualMachineDAOMySQL {

  class AwardsRecommendation(val awardCodes: js.Array[String], val awardedXP: Int) extends js.Object

  class ContestPortfolioData(val contestID: String,
                             val portfolioID: String,
                             val startingBalance: Double) extends js.Object

  class Proceeds(val cash: Double, val equity: Double) extends js.Object

}
