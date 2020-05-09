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

  override def closeContest(contestID: String): Future[js.Array[ClosePortfolioResponse]] = {
    for {
      w <- stopContest(contestID)
      rankings <- findContestRankings(contestID)
      portfolioIDs = rankings.flatMap(_.portfolioID.toOption)
      summaries <- Future.sequence(portfolioIDs.toList.map(closePortfolio(_, rankings)))
    } yield summaries.toJSArray
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
          wallet <- debitWallet(portfolioID, startingBalance)
          messageRef <- sendChatMessage(contestID, userID, message = s"Welcome to $name!")
          w <- updatePlayerStatistics(portfolioID)(PlayerStatistics(gamesCreated = +1))
        } yield new ContestCreationResponse(contestID, portfolioID, messageRef.messageID)
      case None =>
        Future.failed(js.JavaScriptException("Required parameters are missing"))
    }
  }

  override def joinContest(contestID: String, userID: String): Future[PortfolioRef] = {
    for {
      // verify whether the user can actually join this contest
      gs <- findPortfolioStatus(contestID, userID) map {
        case gs if gs.playerCount >= AppConstants.MaxPlayers => throw MaxPlayersReachedException(gs)
        case gs if gs.isParticipant != 0 => throw PlayerAlreadyJoinedException(gs)
        case gs if gs.startingBalance > gs.wallet => throw InsufficientFundsException(actual = gs.wallet, expected = gs.startingBalance)
        case gs => gs
      }
      portfolioID <- insertPortfolio(contestID = contestID, userID = userID, funds = gs.startingBalance)
      wallet <- debitWallet(portfolioID, gs.startingBalance)
      w <- updatePlayerStatistics(portfolioID)(PlayerStatistics(gamesJoined = +1))
    } yield new PortfolioRef(portfolioID)
  }

  override def quitContest(contestID: String, userID: String): Future[PortfolioEquity] = {
    for {
      cp <- findContestAndPortfolioID(contestID, userID)
      proceeds <- liquidatePortfolio(cp.portfolioID)
      wallet <- creditWallet(cp.portfolioID, proceeds.equity + proceeds.cash)
      w <- updatePlayerStatistics(cp.portfolioID)(PlayerStatistics(gamesWithdrawn = +1))
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

  override def creditWallet(portfolioID: String, amount: Double): Future[Double] = debitWallet(portfolioID, -amount)

  override def debitWallet(portfolioID: String, amount: Double): Future[Double] = {
    for {
      // lookup the user's wallet by portfolio ID
      (userID, wallet) <- conn.queryFuture[UserProfileData](
        "SELECT U.userID, wallet FROM users U INNER JOIN portfolios P ON P.userID = U.userID WHERE portfolioID = ?",
        js.Array(portfolioID)).map(_._1.headOption) map { user_? =>
        (for {user <- user_?; userID <- user.userID.toOption; wallet <- user.wallet.toOption} yield (userID, wallet)) match {
          case Some((_, wallet)) if wallet < amount => throw InsufficientFundsException(wallet, amount)
          case Some((userID, wallet)) => (userID, wallet)
          case None => throw PortfolioNotFoundException(portfolioID)
        }
      }

      // perform the update
      w <- conn.executeFuture("UPDATE users SET wallet = wallet - ? WHERE userID = ?", js.Array(amount, userID))
        .map(_.affectedRows) map checkCount(_ => f"Could not be credit wallet with $amount%.2f")
    } yield wallet - amount
  }

  override def purchasePerks(portfolioID: String, perkCodes: js.Array[String]): Future[PurchasePerksResponse] = {
    val perks = Perk.availablePerks
    val perkMapping = js.Dictionary(perks.map(p => p.code -> p): _ *)
    val perksCost = (perkCodes flatMap perkMapping.get).map(_.cost).sum

    for {
      funds <- debitPortfolio(portfolioID, perksCost)
      w <- insertPerks(portfolioID, perkCodes)
    } yield new PurchasePerksResponse(funds, count = w)
  }

  //////////////////////////////////////////////////////////////////
  //    Portfolio Functions
  //////////////////////////////////////////////////////////////////

  override def cancelOrder(orderID: String): Future[OrderOutcome] = {
    completeOrder(orderID, fulfilled = false, message = "Canceled by user")
  }

  override def closePortfolio(portfolioID: String): Future[ClosePortfolioResponse] = {
    for {
      rankings <- findPortfolioRankings(portfolioID)
      recommendation <- closePortfolio(portfolioID, rankings)
    } yield recommendation
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
      yes <- ensurePortfolioIsOpen(portfolioID)
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

  override def creditPortfolio(portfolioID: String, amount: Double): Future[Double] = debitPortfolio(portfolioID, -amount)

  override def debitPortfolio(portfolioID: String, amount: Double): Future[Double] = {
    for {
      // get the portfolio's funds
      funds <- conn.queryFuture[PortfolioData]("SELECT funds FROM portfolios WHERE portfolioID = ?",
        js.Array(portfolioID)).map(_._1.headOption).map(_.flatMap(_.funds.toOption) match {
        case Some(funds) if funds < amount => throw InsufficientFundsException(funds, amount)
        case Some(funds) => funds
        case None => throw PortfolioNotFoundException(portfolioID)
      })

      // perform the update
      w <- conn.executeFuture("UPDATE portfolios SET funds = funds - ? WHERE portfolioID = ?",
        js.Array(amount, portfolioID)).map(_.affectedRows) map checkUpdate(_ => PortfolioNotFoundException(portfolioID))
    } yield funds - amount
  }

  override def decreasePosition(portfolioID: String, orderID: String, priceType: String, symbol: String, exchange: String, quantity: Double): Future[OrderOutcome] = {
    val outcome = for {
      yes <- ensurePortfolioIsOpen(portfolioID)
      metrics <- findPositionMetrics(portfolioID, symbol, exchange, quantity, priceType)
      funds <- creditPortfolio(portfolioID, amount = metrics.marketValue.orZero - metrics.commission.orZero)
      w0 <- updatePosition(portfolioID, symbol, exchange, quantity, isBuying = false)
      order <- completeOrder(orderID, negotiatedPrice = metrics.lastTrade, fulfilled = true)
      xp = 5 + Math.min(10.0, Math.max(0, 1e+5 * metrics.gainLoss.orZero))
      w1 <- grantXP(portfolioID, xp = xp.toInt)
    } yield new OrderOutcome(negotiatedPrice = metrics.lastTrade, fulfilled = true, xp = xp, w = w0)

    outcome recoverWith {
      case PortfolioClosedException(portfolioID, closedTime) =>
        completeOrder(orderID, fulfilled = false, message = s"Portfolio $portfolioID was closed at ${closedTime.orNull}")
      case PortfolioNotFoundException(portfolioID) =>
        completeOrder(orderID, fulfilled = false, message = s"Portfolio $portfolioID was not found")
      case e: Exception =>
        completeOrder(orderID, fulfilled = false, message = s"Portfolio $portfolioID: ${e.getMessage}")
    }
  }

  override def increasePosition(portfolioID: String, orderID: String, priceType: String, symbol: String, exchange: String, quantity: Double): Future[OrderOutcome] = {
    val outcome = for {
      yes <- ensurePortfolioIsOpen(portfolioID)
      mv <- computeMarketValue(symbol, exchange, quantity, priceType)
      funds <- debitPortfolio(portfolioID, amount = mv.marketValue + mv.commission)
      w <- updatePosition(portfolioID, symbol, exchange, quantity, isBuying = true)
      order <- completeOrder(orderID, negotiatedPrice = mv.lastTrade, fulfilled = true)
    } yield new OrderOutcome(positionID = js.undefined, negotiatedPrice = mv.lastTrade, fulfilled = true, w = w)

    outcome recoverWith {
      case e@InsufficientFundsException(actual, expected) =>
        completeOrder(orderID, fulfilled = false, message = e.getMessage)
      case PortfolioClosedException(portfolioID, closedTime) =>
        completeOrder(orderID, fulfilled = false, message = s"Portfolio $portfolioID is closed at ${closedTime.orNull}")
      case PortfolioNotFoundException(portfolioID) =>
        completeOrder(orderID, fulfilled = false, message = s"Portfolio $portfolioID was not found")
      case e: Exception =>
        completeOrder(orderID, fulfilled = false, message = e.getMessage)
    }
  }

  override def liquidatePortfolio(portfolioID: String): Future[PortfolioEquity] = {
    for {
      proceeds <- computePortfolioEquity(portfolioID)
      funds <- creditPortfolio(portfolioID, proceeds.equity)
      wallet <- creditWallet(portfolioID, proceeds.equity + proceeds.cash)
      w <- closePositions(portfolioID)
    } yield proceeds
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////
  //    System Functions
  /////////////////////////////////////////////////////////////////////////////////////////////////

  override def trackEvent(event: EventSourceData): Future[Ok] = {
    import event._
    conn.executeFuture(
      """|INSERT INTO eventsource (
         |  command, type, response, responseTimeMillis, contestID, portfolioID, userID,
         |  orderID, orderType, priceType, negotiatedPrice, quantity, symbol, exchange, xp, failed
         |)
         |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
         |""".stripMargin, js.Array(
        command, `type`, response, responseTimeMillis, contestID, portfolioID, userID,
        orderID, orderType, priceType, negotiatedPrice, quantity, symbol, exchange, xp, failed)
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

  private def closePortfolio(portfolioID: String, rankings: Seq[ContestRanking]): Future[ClosePortfolioResponse] = {
    for {
      proceeds <- liquidatePortfolio(portfolioID)
      recommendation = determineAwards(portfolioID, rankings)
      w0 <- foreCloseOrders(portfolioID)
      w1 <- updateIf(recommendation.awardedXP > 0) { () => grantXP(portfolioID, recommendation.awardedXP) }
      w2 <- updateIf(recommendation.awardCodes.nonEmpty) { () => grantAwards(portfolioID, recommendation.awardCodes) }
      w3 <- updatePlayerStatistics(portfolioID)(PlayerStatistics(gamesCompleted = +1))
    } yield new ClosePortfolioResponse(proceeds = proceeds, recommendation = recommendation, foreCloseOrders = w0, xp = w1, awards = w2)
  }

  private def closePositions(portfolioID: String): Future[Int] = {
    (for {
      _ <- conn.executeFuture("UPDATE positions SET quantity = 0 WHERE portfolioID = ?", js.Array(portfolioID)).map(_.affectedRows)
      w <- conn.executeFuture("UPDATE portfolios SET closedTime = now() WHERE portfolioID = ?", js.Array(portfolioID)).map(_.affectedRows)
    } yield w) map checkCount(_ => s"Could not update portfolio $portfolioID")
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

  private def computePortfolioEquity(portfolioID: String): Future[PortfolioEquity] = {
    conn.queryFuture[PortfolioEquity](
      """|SELECT P.funds AS cash, SUM(S.lastTrade) AS equity
         |FROM positions PS
         |INNER JOIN portfolios P ON P.portfolioID = PS.portfolioID
         |INNER JOIN stocks S ON S.symbol = PS.symbol AND S.exchange = PS.exchange
         |WHERE PS.portfolioID = ?
         |""".stripMargin, js.Array(portfolioID)).map(_._1.headOption) map {
      case Some(proceeds) => proceeds
      case None => new PortfolioEquity(cash = 0.0, equity = 0.0)
    }
  }

  private def determineAwards(portfolioID: String, rankings: Seq[ContestRanking]): AwardsRecommendation = {
    var items: List[(String, Int)] = Nil
    val myRanking = rankings.find(_.portfolioID.contains(portfolioID))
    items = Award.CHKDFLAG -> 5 :: items
    items = (myRanking collect { case ranking if ranking.rankNum.contains(1) => Award.GLDTRPHY -> 100 }).toList ::: items
    items = (myRanking collect { case ranking if ranking.rankNum.contains(2) => Award.SLVRTRPHY -> 50 }).toList ::: items
    items = (myRanking collect { case ranking if ranking.rankNum.contains(3) => Award.BRNZTRPHY -> 25 }).toList ::: items
    items = (myRanking collect { case ranking if ranking.gainLoss.exists(_ >= 25.0) => Award.PAYDIRT -> 25 }).toList ::: items
    items = (myRanking collect { case ranking if ranking.gainLoss.exists(_ >= 50.0) => Award.MADMONEY -> 50 }).toList ::: items
    items = (myRanking collect { case ranking if ranking.gainLoss.exists(_ >= 100.0) => Award.CRYSTBAL -> 100 }).toList ::: items
    new AwardsRecommendation(portfolioID, awardCodes = items.map(_._1).toJSArray, awardedXP = items.map(_._2).sum)
  }

  private def ensurePortfolioIsOpen(portfolioID: String): Future[Boolean] = {
    conn.queryFuture[PortfolioData]("SELECT closedTime FROM portfolios WHERE portfolioID = ?", js.Array(portfolioID))
      .map(_._1).map(_.headOption) map {
      case Some(portfolio) if portfolio.closedTime.flat.nonEmpty => throw PortfolioClosedException(portfolioID, portfolio.closedTime)
      case Some(_) => true
      case None => throw PortfolioNotFoundException(portfolioID)
    }
  }

  private def findContestRankings(contestID: String): Future[js.Array[ContestRanking]] = {
    conn.queryFuture[ContestRanking]("SELECT * FROM contest_rankings WHERE contestID = ?", js.Array(contestID))
      .map(_._1).map(ContestRanking.computeRankings(_))
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
         |  C.contestID, U.userID, U.wallet, C.startingBalance, GS.playerCount, GS.isParticipant
         |FROM contests C
         |INNER JOIN users U ON U.userID = ?
         |INNER JOIN (
         |  SELECT contestID, COUNT(*) AS playerCount, SUM(CASE WHEN userID = ? THEN 1 ELSE 0 END) AS isParticipant
         |  FROM portfolios
         |  GROUP BY contestID
         |  LIMIT 1
         |) AS GS ON GS.contestID = C.contestID
         |WHERE C.contestID = ?
         |""".stripMargin, js.Array(userID, userID, contestID)).map(_._1.headOption) map {
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
         |  S.lastTrade - AVG(O.negotiatedPrice) AS gainLoss
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

  private def stopContest(contestID: String): Future[Int] = {
    conn.executeFuture(
      """|UPDATE contests C
         |INNER JOIN contest_statuses CS ON CS.status = 'CLOSED'
         |SET
         |   C.closedTime = now(),
         |   C.statusID = CS.statusID
         |WHERE C.contestID = ?
         |""".stripMargin, js.Array(contestID))
      .map(_.affectedRows) map checkCount(count => s"Contest $contestID could not be closed")
  }

  private def updateIf(condition: Boolean)(task: () => Future[Int]): Future[Int] = {
    if (condition) task() else Future.successful(1)
  }

  private def updatePosition(portfolioID: String, symbol: String, exchange: String, quantity: Double, isBuying: Boolean): Future[Int] = {
    if (isBuying) {
      val positionID = newID
      conn.executeFuture(
        """|INSERT INTO positions (positionID, portfolioID, symbol, exchange, quantity) VALUES (?, ?, ?, ?, ?)
           |ON DUPLICATE KEY UPDATE quantity = quantity + ?, processedTime = now()
           |""".stripMargin, js.Array(
          /* insert */ positionID, portfolioID, symbol, exchange, quantity,
          /* update */ quantity
        )).map(_.affectedRows) map checkCount(_ => "Failed to increase position")
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
        case (count, _) if count > 0 => count
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
        stats.gamesWithdrawn.map(v => 'gamesWithdrawn -> v).toList
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
                        val commission: js.UndefOr[Double]) extends js.Object

}
