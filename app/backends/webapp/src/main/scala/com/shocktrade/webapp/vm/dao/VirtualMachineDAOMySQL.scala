package com.shocktrade.webapp.vm.dao

import java.util.UUID

import com.shocktrade.common.forms.{ContestCreationRequest, ContestCreationResponse}
import com.shocktrade.common.models.Award
import com.shocktrade.common.models.contest._
import com.shocktrade.common.models.user.{PlayerStatistics, UserRef}
import com.shocktrade.common.{AppConstants, Ok}
import com.shocktrade.server.dao.MySQLDAO
import com.shocktrade.webapp.routes.account.dao.{UserAccountData, UserIconData, UserProfileData}
import com.shocktrade.webapp.routes.contest.dao.{ContestData, OrderData, PortfolioData}
import com.shocktrade.webapp.vm.dao.VirtualMachineDAOMySQL.{PortfolioEquity, _}
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

  override def createUserIcon(icon: UserIconData)(implicit ec: ExecutionContext): Future[Ok] = {
    import icon._
    conn.executeFuture(
      """|REPLACE INTO user_icons (userID, name, mime, image) VALUES (?, ?, ?, ?)
         |""".stripMargin, js.Array(userID, name, mime, image)).map(r => Ok(r.affectedRows))
  }

  override def createUserAccount(account: UserAccountData)(implicit ec: ExecutionContext): Future[UserRef] = {
    import account._
    val newUserID = newID
    conn.executeFuture(
      """|INSERT INTO users (userID, username, email, password, wallet) VALUES (?, ?, ?, ?, ?)
         |""".stripMargin, js.Array(newUserID, username, email, password, wallet)).map(_.affectedRows) map {
      case count if count > 0 => UserRef(newUserID, username)
      case count => throw UpdateException(s"User account not created", count)
    }
  }

  //////////////////////////////////////////////////////////////////
  //    Contest Functions
  //////////////////////////////////////////////////////////////////

  override def closeContest(contestID: String): Future[Ok] = {
    conn.executeFuture(
      """|UPDATE contests C
         |INNER JOIN contest_statuses CS ON CS.status = 'CLOSED'
         |SET
         |   C.closedTime = now(),
         |   C.statusID = CS.statusID
         |WHERE C.contestID = ?
         |""".stripMargin, js.Array(contestID))
      .map(_.affectedRows) map checkCount(_ => "Contest could not be closed") map (Ok(_))
  }

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
          contestID <- insertContest(request.contestID getOrElse newID, userID, name, startingBalance, duration)(request)
          portfolioID <- insertPortfolio(contestID, userID, startingBalance)
          proceeds <- debitWallet(portfolioID, startingBalance)
          messageRef <- sendChatMessage(contestID, userID, message = s"Welcome to $name!")
          w0 <- updatePlayerStatistics(portfolioID)(PlayerStatistics(gamesCreated = +1))
        } yield new ContestCreationResponse(contestID, portfolioID, messageRef.messageID)
      case None =>
        Future.failed(js.JavaScriptException("Required parameters are missing"))
    }
  }

  override def joinContest(contestID: String, userID: String): Future[PortfolioRef] = {
    for {
      // verify whether the user can actually join this contest
      ps <- findPortfolioStatus(contestID, userID) map {
        case ps if ps.playerCount >= AppConstants.MaxPlayers => throw MaxPlayersReachedException(ps)
        case ps if ps.isParticipant != 0 => throw PlayerAlreadyJoinedException(ps)
        case ps if ps.startingBalance > ps.wallet => throw InsufficientFundsException(actual = ps.wallet, expected = ps.startingBalance)
        case ps => ps
      }
      portfolioID <- insertPortfolio(contestID = contestID, userID = userID, funds = ps.startingBalance)
      proceeds <- debitWallet(portfolioID, ps.startingBalance)
      w0 <- updatePlayerStatistics(portfolioID)(PlayerStatistics(gamesJoined = +1))
    } yield new PortfolioRef(portfolioID)
  }

  override def quitContest(contestID: String, userID: String): Future[PortfolioEquity] = {
    for {
      cp <- findContestAndPortfolioID(contestID, userID)
      proceeds <- closeAndTransferFunds(cp.portfolioID)
      w1 <- updatePlayerStatistics(cp.portfolioID)(PlayerStatistics(gamesWithdrawn = +1))
    } yield proceeds
  }

  override def sendChatMessage(contestID: String, userID: String, message: String): Future[MessageRef] = {
    val messageID = newID
    conn.executeFuture(
      """|INSERT INTO messages (messageID, contestID, userID, message) VALUES (?, ?, ?, ?)
         |""".stripMargin, js.Array(messageID, contestID, userID, message)).map(_.affectedRows) map {
      case count if count > 0 => MessageRef(messageID)
      case count => throw UpdateException("Chat message not created", count)
    }
  }

  override def startContest(contestID: String, userID: String): Future[Boolean] = {
    conn.executeFuture(
      s"""|UPDATE portfolios P
          |INNER JOIN contest_statuses CS ON CS.status = 'ACTIVE'
          |SET startTime = now(), statusID = CS.statusID
          |WHERE contestID = ? AND hostUserID = ? AND statusID <> CS.statusID
          |""".stripMargin, js.Array(contestID, userID)) map (_.affectedRows > 0)
  }

  override def updateContest(contest: ContestData): Future[Ok] = {
    import contest._
    conn.executeFuture("UPDATE contests SET name = ? WHERE contestID = ?",
      js.Array(name, contestID)).map(r => Ok(r.affectedRows))
  }

  //////////////////////////////////////////////////////////////////
  //    Player Functions
  //////////////////////////////////////////////////////////////////

  override def creditWallet(portfolioID: String, amount: Double): Future[Ok] = {
    conn.executeFuture(
      """|UPDATE users U
         |INNER JOIN portfolios P ON P.userID = U.userID
         |SET U.wallet = U.wallet + ?
         |WHERE P.portfolioID = ?
         |""".stripMargin, js.Array(amount, portfolioID))
      .map(_.affectedRows) map checkCount(_ => f"Could not be credit wallet with $amount%.2f") map (Ok(_))
  }

  override def debitWallet(portfolioID: String, amount: Double): Future[PortfolioEquity] = {
    for {
      // lookup the user's wallet by portfolio ID
      (userID, wallet) <- conn.queryFuture[UserProfileData](
        """|SELECT U.userID, U.wallet
           |FROM users U
           |INNER JOIN portfolios P ON P.userID = U.userID
           |WHERE portfolioID = ?
           |""".stripMargin,
        js.Array(portfolioID)).map(_._1.headOption) map { user_? =>
        (for {user <- user_?; userID <- user.userID.toOption; wallet <- user.wallet.toOption} yield (userID, wallet)) match {
          case Some((_, wallet)) if wallet < amount => throw InsufficientFundsException(wallet, amount)
          case Some((userID, wallet)) => (userID, wallet)
          case None => throw PortfolioNotFoundException(portfolioID)
        }
      }

      // perform the update
      w0 <- conn.executeFuture("UPDATE users SET wallet = wallet - ? WHERE userID = ?", js.Array(amount, userID))
        .map(_.affectedRows) map checkCount(_ => f"Could not be debit wallet with $amount%.2f")
    } yield new PortfolioEquity(cash = wallet, equity = amount)
  }

  override def purchasePerks(portfolioID: String, perkCodes: js.Array[String]): Future[PurchasePerksResponse] = {
    val perks = Perk.availablePerks
    val perkMapping = js.Dictionary(perks.map(p => p.code -> p): _ *)
    val perksCost = (perkCodes flatMap perkMapping.get).map(_.cost).sum
    for {
      portfolioEquity <- debitPortfolio(portfolioID, perksCost)
      w0 <- insertPerks(portfolioID, perkCodes)
    } yield new PurchasePerksResponse(portfolioEquity, wPerks = w0)
  }

  //////////////////////////////////////////////////////////////////
  //    Portfolio Functions
  //////////////////////////////////////////////////////////////////

  override def cancelOrder(orderID: String): Future[OrderOutcome] = {
    completeOrder(orderID, fulfilled = false, message = "Canceled by user")
  }

  override def completeOrder(orderID: String,
                             fulfilled: Boolean,
                             negotiatedPrice: js.UndefOr[Double] = js.undefined,
                             message: js.UndefOr[String] = js.undefined): Future[OrderOutcome] = {
    val closed = true
    conn.executeFuture(
      """|UPDATE orders
         |SET closed = ?, fulfilled = ?, processedTime = now(), negotiatedPrice = ?, message = ?
         |WHERE orderID = ?
         |""".stripMargin, js.Array(closed, fulfilled, negotiatedPrice, message.orNull, orderID))
      .map(_.affectedRows).map(checkCount(_ => s"Order $orderID could not be closed"))
      .map(w => new OrderOutcome(fulfilled = fulfilled, w = w))
  }

  override def createOrder(portfolioID: String, order: OrderData): Future[OrderRef] = {
    import order._
    val newOrderID = newID
    val outcome = for {

      - <- ensurePortfolioIsOpen(portfolioID)
      w <- conn.executeFuture(
        """|INSERT INTO orders (orderID, portfolioID, symbol, exchange, orderType, priceType, price, quantity)
           |VALUES (?, ?, ?, ?, ?, ?, ?, ?)
           |""".stripMargin,
        js.Array(newOrderID, portfolioID, symbol, exchange, orderType, priceType, price, quantity)).map(_.affectedRows)
    } yield w

    outcome map {
      case count if count > 0 => OrderRef(newOrderID)
      case count => throw UpdateException(s"Order $newOrderID could not be inserted", count)
    }
  }

  override def decreasePosition(portfolioID: String, orderID: String, priceType: String, symbol: String, exchange: String, quantity: Double): Future[OrderOutcome] = {
    val outcome = for {
      _ <- ensurePortfolioIsOpen(portfolioID)
      metrics <- findPositionMetrics(portfolioID, symbol, exchange, quantity, priceType)
      xp = 1e+1 * metrics.gainLossPct.orZero
      _ <- creditPortfolio(portfolioID, amount = metrics.marketValue.orZero - metrics.commission.orZero, xp = xp)
      pu <- updatePosition(portfolioID, symbol, exchange, quantity, isBuying = false)
      ou <- completeOrder(orderID, negotiatedPrice = metrics.lastTrade, fulfilled = true)
    } yield new OrderOutcome(positionID = pu.positionID, negotiatedPrice = metrics.lastTrade, fulfilled = true, xp = xp, w = pu.w)

    outcome recoverWith {
      case e: InsufficientQuantityException =>
        completeOrder(orderID, fulfilled = false, message = e.getMessage)
      case PortfolioClosedException(_, closedTime) =>
        completeOrder(orderID, fulfilled = false, message = s"Portfolio was closed at ${closedTime.orNull}")
      case _: PortfolioNotFoundException =>
        completeOrder(orderID, fulfilled = false, message = "Portfolio was not found")
      case e: Exception =>
        completeOrder(orderID, fulfilled = false, message = e.getMessage)
    }
  }

  override def increasePosition(portfolioID: String, orderID: String, priceType: String, symbol: String, exchange: String, quantity: Double): Future[OrderOutcome] = {
    val outcome = for {
      _ <- ensurePortfolioIsOpen(portfolioID)
      mv <- computeMarketValue(symbol, exchange, quantity, priceType)
      proceeds <- debitPortfolio(portfolioID, amount = mv.marketValue + mv.commission)
      pu <- updatePosition(portfolioID, symbol, exchange, quantity, isBuying = true)
      ou <- completeOrder(orderID, negotiatedPrice = mv.lastTrade, fulfilled = true)
    } yield new OrderOutcome(positionID = pu.positionID, negotiatedPrice = mv.lastTrade, fulfilled = true, w = pu.w)

    outcome recoverWith {
      case e: InsufficientFundsException =>
        completeOrder(orderID, fulfilled = false, message = e.getMessage)
      case PortfolioClosedException(_, closedTime) =>
        completeOrder(orderID, fulfilled = false, message = s"Portfolio is closed at ${closedTime.orNull}")
      case _: PortfolioNotFoundException =>
        completeOrder(orderID, fulfilled = false, message = "Portfolio was not found")
      case e: Exception =>
        completeOrder(orderID, fulfilled = false, message = e.getMessage)
    }
  }

  override def liquidatePortfolio(portfolioID: String): Future[ClosePortfolioResponse] = {
    import Award._
    for {
      rankings <- findPortfolioRankings(portfolioID)
      _ <- foreCloseOrders(portfolioID)
      proceeds <- closeAndTransferFunds(portfolioID)
      recommendation = determineAwards(portfolioID, rankings)
      _ <- updateIf(recommendation.awardedXP > 0) { () => grantXP(portfolioID, recommendation.awardedXP) }
      _ <- updateIf(recommendation.awardCodes.nonEmpty) { () => grantAwards(portfolioID, recommendation.awardCodes) }
      _ <- updatePlayerStatistics(portfolioID)(PlayerStatistics(gamesCompleted = +1,
        trophiesGold = recommendation(GLDTRPHY),
        trophiesSilver = recommendation(SLVRTRPHY),
        trophiesBronze = recommendation(BRNZTRPHY)
      ))
    } yield new ClosePortfolioResponse(proceeds, recommendation, xp = recommendation.awardedXP)
  }

  private def closeAndTransferFunds(portfolioID: String): Future[PortfolioEquity] = {
    for {
      // retrieve the portfolio's funds and equity
      proceeds <- conn.queryFuture[PortfolioEquity](
        """|SELECT P.funds AS cash, SUM(S.lastTrade * PS.quantity) AS equity
           |FROM portfolios P
           |LEFT JOIN positions PS ON P.portfolioID = PS.portfolioID
           |LEFT JOIN stocks S ON S.symbol = PS.symbol AND S.exchange = PS.exchange
           |WHERE PS.portfolioID = ?
           |""".stripMargin, js.Array(portfolioID)).map(_._1.headOption) map {
        case Some(proceeds) => proceeds
        case None => throw PortfolioNotFoundException(portfolioID)
      }

      // compute the new wallet and portfolio funds
      (wallet, funds) = (proceeds.equity + proceeds.cash, proceeds.equity)

      // transfer funds; then close-out the portfolio and the positions
      _ <- conn.executeFuture(
        """|UPDATE users U
           |INNER JOIN portfolios P ON P.userID = U.userID
           |LEFT JOIN positions PS ON PS.portfolioID = P.portfolioID
           |SET U.wallet = U.wallet + ?,
           |    P.funds = P.funds + ?,
           |    P.closedTime = now(),
           |    PS.quantity = 0.0
           |WHERE P.portfolioID = ?
           |""".stripMargin, js.Array(wallet, funds, portfolioID))
        .map(_.affectedRows) map checkCount(_ => f"Could not be credit wallet with $wallet%.2f")
    } yield proceeds
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    System Functions
  /////////////////////////////////////////////////////////////////////////////////////////////////

  override def trackEvent(event: EventSourceData): Future[Ok] = {
    import event._
    conn.executeFuture(
      """|INSERT INTO eventsource (
         |  command, type, contestID, portfolioID, userID, orderID, positionID, orderType, priceType,
         |  negotiatedPrice, quantity, symbol, exchange, xp, failed, response, responseTimeMillis
         |)
         |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
         |""".stripMargin, js.Array(
        command, `type`, contestID, portfolioID, userID, orderID, positionID, orderType, priceType,
        negotiatedPrice, quantity, symbol, exchange, xp, failed, response, responseTimeMillis)
    ).map(_.affectedRows) map (Ok(_))
  }

  override def updateEventLog(): Future[Ok] = {
    for {
      // is contestID or userID null?
      w0 <- conn.executeFuture(
        """|UPDATE eventsource E
           |INNER JOIN portfolios P ON P.portfolioID = E.portfolioID
           |SET E.contestID = P.contestID, E.userID = P.userID
           |WHERE E.portfolioID IS NOT NULL
           |AND (E.contestID IS NULL OR E.userID IS NULL)
           |""".stripMargin).map(_.affectedRows)

      // is portfolioID null?
      w1 <- conn.executeFuture(
        """|UPDATE eventsource E
           |INNER JOIN portfolios P on P.contestID = E.contestID AND P.userID = E.userID
           |SET E.portfolioID = P.contestID
           |WHERE E.portfolioID IS NULL
           |AND (E.contestID IS NOT NULL AND E.userID IS NOT NULL)
           |""".stripMargin).map(_.affectedRows)
    } yield Ok(w0 + w1)
  }

  //////////////////////////////////////////////////////////////////
  //    Utility Functions
  //////////////////////////////////////////////////////////////////

  private def checkCount(message: Int => String): Int => Int = {
    case count if count < 1 => throw UpdateException(message(count), count)
    case count => count
  }

  private def checkUpdate(f: Int => Throwable): Int => Int = {
    case count if count < 1 => throw f(count)
    case count => count
  }

  private def computeMarketValue(symbol: String, exchange: String, quantity: Double, priceType: String): Future[MarketValue] = {
    conn.queryFuture[MarketValue](
      """|SELECT S.lastTrade, S.lastTrade * ? AS marketValue, OPT.commission
         |FROM stocks S
         |INNER JOIN order_price_types OPT ON OPT.name = ?
         |WHERE S.symbol = ? AND S.exchange = ?
         |""".stripMargin, js.Array(quantity, priceType, symbol, exchange)).map(_._1.headOption) map {
      case Some(proceeds) => proceeds
      case None => throw TickerNotFoundException(symbol, exchange)
    }
  }

  private def creditPortfolio(portfolioID: String, amount: Double, xp: Double): Future[Ok] = {
    conn.executeFuture(
      """|UPDATE users U
         |INNER JOIN portfolios P ON P.userID = U.userID
         |SET U.totalXP = U.totalXP + ?,
         |    P.totalXP = P.totalXP + ?,
         |    P.funds = P.funds + ?
         |WHERE P.portfolioID = ?
         |""".stripMargin, js.Array(xp.toInt, xp.toInt, amount, portfolioID))
      .map(_.affectedRows) map checkUpdate(_ => PortfolioNotFoundException(portfolioID)) map (Ok(_))
  }

  private def debitPortfolio(portfolioID: String, amount: Double): Future[PortfolioEquity] = {
    for {
      // get the portfolio's funds
      funds <- conn.queryFuture[PortfolioData]("SELECT funds FROM portfolios WHERE portfolioID = ?",
        js.Array(portfolioID)).map(_._1.headOption).map(_.flatMap(_.funds.toOption) match {
        case Some(funds) if funds < amount => throw InsufficientFundsException(funds, amount)
        case Some(funds) => funds
        case None => throw PortfolioNotFoundException(portfolioID)
      })

      // perform the update
      _ <- conn.executeFuture("UPDATE portfolios SET funds = funds - ? WHERE portfolioID = ?",
        js.Array(amount, portfolioID)).map(_.affectedRows) map checkUpdate(_ => PortfolioNotFoundException(portfolioID))
    } yield new PortfolioEquity(cash = funds, equity = amount)
  }

  private def determineAwards(portfolioID: String, rankings: Seq[ContestRanking]): AwardsRecommendation = {
    var items: List[(String, Int)] = Nil
    val myRanking = rankings.find(_.portfolioID.contains(portfolioID))
    val userID = myRanking.flatMap(_.userID.toOption).getOrElse(throw js.JavaScriptException("User ID is required"))
    items = Award.CHKDFLAG -> 5 :: items
    items = (myRanking collect { case ranking if ranking.rankNum.contains(1) => Award.GLDTRPHY -> 100 }).toList ::: items
    items = (myRanking collect { case ranking if ranking.rankNum.contains(2) => Award.SLVRTRPHY -> 50 }).toList ::: items
    items = (myRanking collect { case ranking if ranking.rankNum.contains(3) => Award.BRNZTRPHY -> 25 }).toList ::: items
    items = (myRanking collect { case ranking if ranking.gainLoss.exists(_ >= 25.0) => Award.PAYDIRT -> 25 }).toList ::: items
    items = (myRanking collect { case ranking if ranking.gainLoss.exists(_ >= 50.0) => Award.MADMONEY -> 50 }).toList ::: items
    items = (myRanking collect { case ranking if ranking.gainLoss.exists(_ >= 100.0) => Award.CRYSTBAL -> 100 }).toList ::: items
    new AwardsRecommendation(portfolioID, userID, awardCodes = items.map(_._1).toJSArray, awardedXP = items.map(_._2).sum)
  }

  private def ensurePortfolioIsOpen(portfolioID: String): Future[Boolean] = {
    conn.queryFuture[PortfolioData]("SELECT closedTime FROM portfolios WHERE portfolioID = ?", js.Array(portfolioID))
      .map(_._1).map(_.headOption) map {
      case Some(portfolio) if portfolio.closedTime.flat.nonEmpty => throw PortfolioClosedException(portfolioID, portfolio.closedTime)
      case Some(_) => true
      case None => throw PortfolioNotFoundException(portfolioID)
    }
  }

  private def findContestAndPortfolioID(contestID: String, userID: String): Future[ContestPortfolioData] = {
    conn.queryFuture[ContestPortfolioData]("SELECT portfolioID FROM portfolios WHERE contestID = ? AND userID = ?",
      js.Array(contestID, userID)).map(_._1.headOption) map {
      case Some(results) => results
      case None => throw ContestPortfolioNotFoundException(contestID, userID)
    }
  }

  private def findOwnedQuantity(portfolioID: String, symbol: String, exchange: String): Future[OwnedQuantity] = {
    conn.queryFuture[OwnedQuantity](
      """|SELECT quantity
         |FROM positions
         |WHERE portfolioID = ? AND symbol = ? AND exchange = ?
         |""".stripMargin, js.Array(portfolioID, symbol, exchange)).map(_._1.headOption) map {
      case Some(result) => result
      case None => throw PositionNotFoundException(portfolioID, symbol, exchange)
    }
  }

  private def findPortfolioRankings(portfolioID: String): Future[js.Array[ContestRanking]] = {
    conn.queryFuture[ContestRanking](
      """|SELECT CR.*
         |FROM portfolios P
         |INNER JOIN contest_rankings CR ON CR.contestID = P.contestID
         |WHERE P.portfolioID = ?
         |""".stripMargin, js.Array(portfolioID)).map(_._1).map(ContestRanking.computeRankings(_))
  }

  private def findPortfolioStatus(contestID: String, userID: String): Future[PortfolioStatus] = {
    conn.queryFuture[PortfolioStatus](
      """|SELECT
         |  C.contestID, U.userID, U.wallet, C.startingBalance,
         |  IFNULL(GS.playerCount, 0) AS playerCount,
         |  IFNULL(GS.isParticipant, 0) AS isParticipant
         |FROM contests C
         |INNER JOIN users U ON U.userID = ?
         |LEFT JOIN (
         |  SELECT contestID, COUNT(*) AS playerCount, SUM(CASE WHEN userID = ? THEN 1 ELSE 0 END) AS isParticipant
         |  FROM portfolios
         |  WHERE contestID = ?
         |) AS GS ON GS.contestID = C.contestID
         |WHERE C.contestID = ?
         |""".stripMargin, js.Array(userID, userID, contestID, contestID)).map(_._1.headOption) map {
      case Some(gameStatus) => gameStatus
      case None => throw ContestPortfolioNotFoundException(contestID, userID)
    }
  }

  private def findPositionMetrics(portfolioID: String, symbol: String, exchange: String, quantity: Double, priceType: String): Future[PositionMetrics] = {
    conn.queryFuture[PositionMetrics](
      """|SELECT
         |  S.lastTrade, OPT.commission,
         |  S.lastTrade * ? AS marketValue,
         |  AVG(O.negotiatedPrice) AS pricePaid,
         |  S.lastTrade - AVG(O.negotiatedPrice) * SUM(O.quantity) AS gainLoss,
         |  (S.lastTrade - AVG(O.negotiatedPrice) * SUM(O.quantity)) / (AVG(O.negotiatedPrice) * SUM(O.quantity)) AS gainLossPct
         |FROM orders O
         |INNER JOIN order_price_types OPT ON OPT.name = ?
         |LEFT JOIN stocks S ON S.symbol = O.symbol AND S.exchange = O.exchange
         |WHERE O.portfolioID = ? AND O.symbol = ? AND O.exchange = ?
         |AND O.orderType = 'BUY' AND O.fulfilled = 1
         |GROUP BY O.symbol, O.exchange
         |""".stripMargin, js.Array(quantity, priceType, portfolioID, symbol, exchange)).map(_._1.headOption) map {
      case Some(metrics) => metrics
      case None => throw PositionNotFoundException(portfolioID, symbol, exchange)
    }
  }

  private def foreCloseOrders(portfolioID: String): Future[Int] = {
    val (message, closed, fulfilled) = ("Portfolio is closed.", true, false)
    conn.executeFuture(
      """|UPDATE orders
         |SET closed = ?, fulfilled = ?, processedTime = now(), message = ?
         |WHERE portfolioID = ?
         |""".stripMargin, js.Array(closed, fulfilled, message, portfolioID))
      .map(_.affectedRows)
  }

  private def grantAwards(portfolioID: String, awardCodes: js.Array[String]): Future[Int] = {
    Future.sequence(awardCodes.toSeq map { awardCode =>
    conn.executeFuture(
        """|REPLACE INTO user_awards (userAwardID, userID, awardCode)
           |SELECT uuid(), userID, ? FROM portfolios WHERE portfolioID = ?
           |""".stripMargin, js.Array(awardCode, portfolioID))
        .map(_.affectedRows) map checkCount(_ => s"Portfolio $portfolioID could not grant award [${awardCodes.mkString(",")}]")
    }) map (_.sum)
  }

  private def grantXP(portfolioID: String, xp: Int): Future[Int] = {
    conn.executeFuture(
      """|UPDATE users U
         |INNER JOIN portfolios P ON P.userID = U.userID
         |SET U.totalXP = U.totalXP + ?, P.totalXP = P.totalXP + ?
         |WHERE P.portfolioID = ?
         |""".stripMargin, js.Array(xp, xp, portfolioID))
      .map(_.affectedRows) map checkCount(_ => "Total XP could not be updated")
  }

  private def insertContest(contestID: String, userID: String, name: String, startingBalance: Double, duration: Int)(request: ContestCreationRequest): Future[String] = {
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
      case count => throw UpdateException("Contest not created", count)
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

  private def newID: String = UUID.randomUUID().toString

  private def updateIf(condition: Boolean)(task: () => Future[Int]): Future[Int] = {
    if (condition) task() else Future.successful(1)
  }

  private def updatePosition(portfolioID: String, symbol: String, exchange: String, quantity: Double, isBuying: Boolean): Future[PositionUpdate] = {
    if (isBuying) {
      val positionID = newID
      conn.executeFuture(
        """|INSERT INTO positions (positionID, portfolioID, symbol, exchange, quantity) VALUES (?, ?, ?, ?, ?)
           |ON DUPLICATE KEY UPDATE quantity = quantity + ?, processedTime = now()
           |""".stripMargin, js.Array(
          /* insert */ positionID, portfolioID, symbol, exchange, quantity,
          /* update */ quantity
        )).map(_.affectedRows) map checkCount(_ => "Failed to increase position") map (w => new PositionUpdate(positionID, w))
    } else {
      val outcome = for {
        w <- conn.executeFuture(
          """|UPDATE positions
             |SET quantity = quantity - ?, processedTime = now()
             |WHERE symbol = ? AND exchange = ? AND quantity >= ?
             |""".stripMargin, js.Array(quantity, symbol, exchange, quantity)).map(_.affectedRows)
        ownedQty <- if (w == 0) findOwnedQuantity(portfolioID, symbol, exchange) else Future.successful(new OwnedQuantity(quantity))
      } yield (w, ownedQty)

      outcome map {
        case (count, _) if count > 0 => new PositionUpdate(w = count, positionID = js.undefined)
        case (_, ownedQty) => throw InsufficientQuantityException(expected = quantity, actual = ownedQty.quantity)
      }
    }
  }

  private def updatePlayerStatistics(portfolioID: String)(stats: PlayerStatistics): Future[Int] = {
    val tuples: List[(Symbol, Any)] = {
      stats.gamesCompleted.map(v => 'gamesCompleted -> v).toList :::
        stats.gamesCreated.map(v => 'gamesCreated -> v).toList :::
        stats.gamesDeleted.map(v => 'gamesDeleted -> v).toList :::
        stats.gamesJoined.map(v => 'gamesJoined -> v).toList :::
        stats.gamesWithdrawn.map(v => 'gamesWithdrawn -> v).toList :::
        stats.trophiesGold.map(v => 'trophiesGold -> v).toList :::
        stats.trophiesSilver.map(v => 'trophiesSilver -> v).toList :::
        stats.trophiesBronze.map(v => 'trophiesBronze -> v).toList
    }

    // build the SQL statement
    val sql =
      s"""|UPDATE users U
          |INNER JOIN portfolios P ON P.userID = U.userID
          |SET ${tuples map { case (column, _) => s"U.${column.name} = U.${column.name} + ?" } mkString ", "}
          |WHERE P.portfolioID = ?
          |""".stripMargin

    // execute the update
    conn.executeFuture(sql, params = (tuples.map(_._2) ::: portfolioID :: Nil).toJSArray).map(_.affectedRows)
  }

}

/**
 * Virtual Machine DAO MySQL Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object VirtualMachineDAOMySQL {

  class ContestPortfolioData(val contestID: String,
                             val portfolioID: String,
                             val startingBalance: Double) extends js.Object

  class MarketValue(val lastTrade: Double, val marketValue: Double, val commission: Double) extends js.Object

  class OwnedQuantity(val quantity: Double) extends js.Object

  class PortfolioEquity(val cash: Double, val equity: Double) extends js.Object

  class PositionMetrics(val lastTrade: js.UndefOr[Double],
                        val marketValue: js.UndefOr[Double],
                        val pricePaid: js.UndefOr[Double],
                        val gainLoss: js.UndefOr[Double],
                        val gainLossPct: js.UndefOr[Double],
                        val commission: js.UndefOr[Double]) extends js.Object

  class PositionUpdate(val positionID: js.UndefOr[String], val w: Int) extends js.Object

}
