package com.shocktrade.autonomous

import com.shocktrade.autonomous.AutonomousTradingEngine._
import com.shocktrade.autonomous.dao.RobotDAO._
import com.shocktrade.autonomous.dao.{RobotData, TradingStrategy}
import com.shocktrade.common.dao.contest.PortfolioDAO._
import com.shocktrade.common.dao.contest.{OrderData, PortfolioData}
import com.shocktrade.common.dao.quotes.SecuritiesDAO._
import com.shocktrade.common.models.contest.OrderLike._
import com.shocktrade.common.models.quote.ResearchQuote
import org.scalajs.nodejs.moment.Moment
import org.scalajs.nodejs.mongodb.{Db, FindAndModifyWriteOpResult, MongoDB}
import org.scalajs.nodejs.os.OS
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.nodejs.{NodeRequire, console}
import org.scalajs.sjs.DateHelper._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Autonomous Trading Engine
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class AutonomousTradingEngine(dbFuture: Future[Db])(implicit ec: ExecutionContext, mongo: MongoDB, require: NodeRequire) {
  // load modules
  private implicit val os = OS()
  private implicit val moment = Moment()

  // get DAO instances
  private val securitiesDAO = dbFuture.flatMap(_.getSecuritiesDAO)
  private val portfolioDAO = dbFuture.flatMap(_.getPortfolioDAO)
  private val robotDAO = dbFuture.flatMap(_.getRobotDAO)

  /**
    * Invokes the process
    */
  def run(): Unit = {
    console.info(s"${moment().format("MM/DD HH:mm:ss")} Looking for robots....")
    val startTime = System.currentTimeMillis()
    val outcome = for {
      robots <- robotDAO.flatMap(_.findRobots())
      results <- Future.sequence(robots.toSeq.map(operate)) map (_.flatten)
    } yield results

    outcome onComplete {
      case Success(results) =>
        console.log(s"${results.size} orders(s) were created")
        console.log(s"Process completed in ${System.currentTimeMillis() - startTime} msec")
      case Failure(e) =>
        console.error(s"Failed to process robot: ${e.getMessage}")
        e.printStackTrace()
    }
  }

  /**
    * Processes the portfolios for the given robot
    * @param robot the given robot
    * @return a promise of the outcomes
    */
  def operate(robot: RobotData) = {
    robot.playerID.toOption match {
      case Some(playerID) =>
        robot.info("Retrieving portfolios...")
        for {
          portfolios <- portfolioDAO.flatMap(_.findByPlayer(playerID))
          results <- Future.sequence(portfolios map { portfolio =>
            for {
              orders <- playWith(robot, portfolio)
              result <- portfolio._id.toOption match {
                case Some(id) => portfolioDAO.flatMap(_.createOrders(id.toHexString(), orders).toFuture)
                case None => Future.successful(New[FindAndModifyWriteOpResult])
              }
            } yield result
          } toSeq)
        //
        } yield results

      case None =>
        robot.error(s"No player ID found")
        Future.successful(Nil)
    }
  }

  private def playWith(robot: RobotData, portfolio: PortfolioData) = {
    // lookup the trading strategy
    val tradingStrategy = getTradingStrategy(robot)
    robot.info(s"Playing with portfolio #${portfolio._id.orNull} using ${tradingStrategy.name getOrElse "Untitled"} strategy")

    // get the positions and active orders
    val positions = portfolio.positions.toList.flatMap(_.toList)
    val orders = portfolio.orders.toList.flatMap(_.toList)

    // compute the outstanding orders cost
    val outstandingOrdersCost = orders.flatMap(_.totalCost.toOption).sum

    // create a collection of symbols we already have orders for / positions in
    val exclusionSymbols = (positions.flatMap(_.symbol.toOption) ::: orders.flatMap(_.symbol.toOption)).toSet

    for {
    // get the collection of eligible quotes
      securities <- tradingStrategy.buyingFlow.flatMap(_.searchOptions).toOption match {
        case Some(options) => securitiesDAO.flatMap(_.research(options)) map (_.toSeq)
        case None => Future.successful(Nil)
      }

      _ = robot.log("Identified %d eligible securities", securities.size)

      // filter out those we already have orders for / positions in
      unownedSecurities = securities.filterNot(_.symbol.exists(exclusionSymbols.contains))

      _ = robot.log("%d unowned securities", unownedSecurities.size)

      // filter out securities with negative ratings
      preferredSecurities = unownedSecurities.filterNot(_.getAdvisory.exists(_.isWarning))

      _ = robot.log("%d preferred securities", preferredSecurities.size)

      // sort the securities - largest spread on top
      sortedSecurities = preferredSecurities.sortBy(q => (-(q.avgVolume10Day getOrElse 0.0), -(q.spread getOrElse 0.0), q.low getOrElse 0.0))

    // determine how much of each security to purchase
    } yield if (sortedSecurities.nonEmpty) createBuyOrders(robot, portfolio, tradingStrategy, sortedSecurities, outstandingOrdersCost) else Nil
  }

  private def createBuyOrders(robot: RobotData,
                              portfolio: PortfolioData,
                              tradingStrategy: TradingStrategy,
                              securities: Seq[ResearchQuote],
                              outstandingOrdersCost: Double) = {
    // display the quotes
    showQuotes(robot, securities)

    val orders = (for {
      buyingFlow <- tradingStrategy.buyingFlow
      cashFunds <- portfolio.cashAccount.flatMap(_.cashFunds)
      availableCash = cashFunds - outstandingOrdersCost
      preferredSpend <- buyingFlow.preferredSpendPerSecurity
      numOfSecuritiesToBuy = (cashFunds / preferredSpend).toInt
      securitiesToBuy = securities.take(numOfSecuritiesToBuy)
      buyOrders = if (availableCash > preferredSpend) {
        robot.log("$ %d cash available. Submitting %d BUY orders...", availableCash, numOfSecuritiesToBuy)
        securitiesToBuy flatMap { security =>
          for {
            low <- security.low.toOption
            computedQuantity = (preferredSpend / low).toLong
            volume = security.avgVolume10Day.map(vol => (vol * 0.25).toLong) getOrElse computedQuantity
            quantity = if (computedQuantity > volume) volume else computedQuantity
          } yield {
            security.toOrder(
              accountType = ACCOUNT_TYPE_CASH,
              orderType = ORDER_TYPE_BUY,
              priceType = PRICE_TYPE_LIMIT,
              price = low,
              quantity = quantity)
          }
        }
      } else {
        robot.log("No cash available for purchases (%d)", availableCash)
        Nil
      }
    } yield buyOrders).toOption.toSeq.flatten

    showOrders(robot, orders)

    orders
  }

  private def getTradingStrategy(robot: RobotData) = {
    robot.tradingStrategy.toOption match {
      case Some(strategy) => strategy
      case None =>
        val defaultStrategy = TradingStrategy.default()
        robot.warn(s"The trading strategy is either not defined or invalid. Defaulting to '${defaultStrategy.name}'")
        defaultStrategy
    }
  }

  private def showQuotes(robot: RobotData, quotes: Seq[ResearchQuote]) = {
    robot.log(s"${quotes.size} securities identified:")
    quotes foreach { q =>
      robot.log("security: %s/%s, price %d, low %d, high %d, volume %d, avgVol %d, spread %d",
        q.symbol, q.exchange, q.lastTrade ?? 0.0, q.low ?? 0.0, q.high ?? 0.0, q.volume ?? 0.0, q.avgVolume10Day ?? 0.0, q.spread ?? 0.0)
    }
  }

  private def showOrders(robot: RobotData, orders: Seq[OrderData]) = {
    robot.log(s"${orders.size} eligible order(s):")
    orders.zipWithIndex foreach { case (o, n) =>
      console.log(s"[${n + 1}] ${o.orderType} / ${o.symbol} @ ${o.price getOrElse "MARKET"} x ${o.quantity} - ${o.priceType} <${o._id}>")
    }
    console.log("")
  }

}

/**
  * Autonomous Trading Engine Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object AutonomousTradingEngine {

  type Quantity = Double

  /**
    * Research Quote Extensions
    * @param quote the given [[ResearchQuote quote]]
    */
  implicit class ResearchQuoteExtensions(val quote: ResearchQuote) extends AnyVal {

    @inline
    def toOrder(accountType: String,
                orderType: String,
                priceType: String,
                price: Double,
                quantity: Double) = {
      val now = new js.Date()
      new OrderData(
        symbol = quote.symbol,
        exchange = quote.exchange,
        accountType = accountType,
        orderType = orderType,
        priceType = priceType,
        price = price,
        quantity = quantity,
        creationTime = now - 6.hours, // TODO remove the 6-hour delta after testing
        expirationTime = now + 3.days
      )
    }

  }

  /**
    * Robots Extensions
    * @param robot the given [[RobotData robot]]
    */
  implicit class RobotsExtensions(val robot: RobotData) extends AnyVal {

    @inline
    def log(format: String, args: js.Any*)(implicit moment: Moment) = {
      console.log(s"$now [${robot.name.orNull}] " + format, args: _*)
    }

    @inline
    def info(format: String, args: js.Any*)(implicit moment: Moment) = {
      console.info(s"$now [${robot.name.orNull}] " + format, args: _*)
    }

    @inline
    def error(format: String, args: js.Any*)(implicit moment: Moment) = {
      console.error(s"$now [${robot.name.orNull}] " + format, args: _*)
    }

    @inline
    def warn(format: String, args: js.Any*)(implicit moment: Moment) = {
      console.warn(s"$now [${robot.name.orNull}] " + format, args: _*)
    }

    @inline
    private def now(implicit moment: Moment) = s"${moment().format("MM/DD HH:mm:ss")}"

  }

}