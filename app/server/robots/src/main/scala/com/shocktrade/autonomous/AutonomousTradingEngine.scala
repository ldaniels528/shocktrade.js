package com.shocktrade.autonomous

import com.shocktrade.autonomous.AutonomousTradingEngine._
import com.shocktrade.autonomous.dao.RobotDAO._
import com.shocktrade.autonomous.dao.{BuyingFlow, RobotData, SellingFlow}
import com.shocktrade.common.dao.contest.PortfolioUpdateDAO._
import com.shocktrade.common.dao.contest._
import com.shocktrade.common.dao.quotes.SecuritiesDAO
import com.shocktrade.common.dao.quotes.SecuritiesDAO._
import com.shocktrade.common.models.contest.OrderLike._
import com.shocktrade.common.models.quote.ResearchQuote
import org.scalajs.nodejs.moment.Moment
import org.scalajs.nodejs.mongodb.{Db, FindAndModifyWriteOpResult, MongoDB}
import org.scalajs.nodejs.npm.numeral.Numeral
import org.scalajs.nodejs.os.OS
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.nodejs.util.Util
import org.scalajs.nodejs.{NodeRequire, console}
import org.scalajs.sjs.DateHelper._
import org.scalajs.sjs.JsUnderOrHelper._

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
  private implicit val moment = Moment()
  private implicit val numeral = Numeral()
  private implicit val util = Util()
  private implicit val os = OS()

  // get DAO instances
  private implicit val securitiesDAO = dbFuture.flatMap(_.getSecuritiesDAO)
  private implicit val portfolioDAO = dbFuture.flatMap(_.getPortfolioUpdateDAO)
  private implicit val robotDAO = dbFuture.flatMap(_.getRobotDAO)

  // create the rule compiler and processor
  private implicit val processor = new RuleProcessor()
  private implicit val compiler = new RuleCompiler()

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
    * @param robot the given [[RobotData robot]]
    * @return a promise of the outcomes
    */
  def operate(robot: RobotData) = {
    robot.playerID.toOption match {
      case Some(playerID) =>
        robot.info("Retrieving portfolios...")
        for {
          portfolios <- portfolioDAO.flatMap(_.findByPlayer(playerID))
          results <- processOrders(robot, portfolios)
        } yield results

      case None =>
        robot.error(s"No player ID found")
        Future.successful(Nil)
    }
  }

  @inline
  private def processOrders(robot: RobotData, portfolios: Seq[PortfolioData]) = {
    Future.sequence {
      for {
        tradingStrategy <- robot.tradingStrategy.toList
        buyingFlow <- tradingStrategy.buyingFlow.toList
        sellingFlow <- tradingStrategy.sellingFlow.toList
        name <- tradingStrategy.name.toList
        portfolio <- portfolios
        portfolioId <- portfolio._id.map(_.toHexString()).toList
      } yield {
        robot.info(s"Playing with portfolio #$portfolioId using the '$name' strategy")

        // create the robot environment
        implicit val env = RobotEnvironment(portfolio)

        for {
        // process the BUY flow
          securities <- buyingFlow.execute()
          buyOrders = if (securities.isEmpty) Nil else createBuyOrders(robot, buyingFlow, securities)

          // process the SELL flow
          positions <- sellingFlow.execute()
          sellOrders = createSellOrders(robot, sellingFlow, positions)

          // combine the buy and sell orders into a single collection
          orders = {
            val combinedOrders = buyOrders ++ sellOrders
            showOrders(robot, combinedOrders)
            combinedOrders
          }

          // persist the orders
          result <- portfolio._id.toOption match {
            case Some(id) if orders.nonEmpty => portfolioDAO.flatMap(_.createOrders(id.toHexString(), orders).toFuture)
            case _ => Future.successful(New[FindAndModifyWriteOpResult])
          }
        } yield result
      }
    }
  }

  @inline
  private def createBuyOrders(robot: RobotData, buyingFlow: BuyingFlow, securities: Seq[ResearchQuote])(implicit env: RobotEnvironment) = {
    // display the quotes
    showQuotes(robot, securities)

    (for {
      cashFunds <- env.portfolio.cashAccount.flatMap(_.cashFunds)
      availableCash = cashFunds - env.outstandingOrdersCost
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
  }

  @inline
  private def createSellOrders(robot: RobotData, flow: SellingFlow, positions: Seq[PositionData]) = {
    Seq.empty[OrderData]
  }

  private def showQuotes(robot: RobotData, quotes: Seq[ResearchQuote]) = {
    robot.log(s"${quotes.size} securities identified:")
    quotes foreach { q =>
      robot.log("| %s/%s | price: %d | low: %d | high: %d | volume: %s | avgVol: %s | spread: %d%% |",
        q.symbol, q.exchange, q.lastTrade.orZero, q.low.orZero, q.high.orZero,
        numeral(q.volume.orZero).format("0,0"), numeral(q.avgVolume10Day.orZero).format("0,0"),
        (q.spread.orZero * 10).toInt / 10.0)
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

  /**
    * Buying Flow Extensions
    * @param flow the given [[BuyingFlow buying flow]]
    */
  implicit class BuyingFlowExtensions(val flow: BuyingFlow) extends AnyVal {

    @inline
    def execute()(implicit ec: ExecutionContext, compiler: RuleCompiler, processor: RuleProcessor, env: RobotEnvironment, securitiesDAO: Future[SecuritiesDAO]) = {
      for {
      // get the collection of eligible quotes
        securities <- flow.searchOptions.toOption match {
          case Some(options) => securitiesDAO.flatMap(_.research(options)) map (_.toSeq)
          case None => Future.successful(Nil)
        }

        // compile the flow
        opCodes = compiler(flow)

      // use the rules to filter out ineligible securities
      } yield processor(opCodes, securities)
    }
  }

  /**
    * Selling Flow Extensions
    * @param flow the given [[BuyingFlow buying flow]]
    */
  implicit class SellingFlowExtensions(val flow: SellingFlow) extends AnyVal {

    @inline
    def execute()(implicit ec: ExecutionContext, compiler: RuleCompiler, processor: RuleProcessor, env: RobotEnvironment, portfolioDAO: Future[PortfolioUpdateDAO]) = {
      Future.successful(Seq.empty[PositionData])
    }
  }

}