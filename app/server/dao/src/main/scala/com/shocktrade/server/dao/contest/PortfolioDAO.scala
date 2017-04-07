package com.shocktrade.server.dao.contest

import io.scalajs.npm.mongodb._
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.OptionHelper._
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Portfolio DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait PortfolioDAO extends Collection

/**
  * Portfolio DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object PortfolioDAO {

  /**
    * Portfolio DAO Enrichment
    * @param dao the given [[PortfolioDAO Portfolio DAO]]
    */
  implicit class PortfolioDAOEnrichment(val dao: PortfolioDAO) {

    @inline
    def create(portfolio: PortfolioData): js.Promise[InsertWriteOpResult] = dao.insertOne(portfolio)

    @inline
    def cancelOrder(portfolioID: String, orderID: String)(implicit ec: ExecutionContext): Future[FindAndModifyWriteOpResult] = {
      for {
        portfolio <- dao.findOneAsync[PortfolioData]("_id" $eq portfolioID.$oid, fields = js.Array("orders")) map (_.orDie(s"Portfolio # $portfolioID not found"))
        order = portfolio.orders.toOption.flatMap(_.find(_._id.contains(orderID))) orDie s"Order # $orderID not found"
        result <- dao.findOneAndUpdate(
          filter = doc("_id" $eq portfolioID.$oid),
          update = doc("orders" $pull ("_id" -> orderID), "orderHistory" $addToSet order),
          options = new FindAndUpdateOptions(returnOriginal = false))
      } yield result
    }

    @inline
    def createOrder(portfolioID: String, order: OrderData): js.Promise[FindAndModifyWriteOpResult] = {
      dao.findOneAndUpdate(
        filter = doc("_id" $eq portfolioID.$oid),
        update = "orders" $addToSet order,
        options = new FindAndUpdateOptions(returnOriginal = false))
    }

    @inline
    def createOrders(portfolioID: String, orders: Seq[OrderData]): js.Promise[FindAndModifyWriteOpResult] = {
      dao.findOneAndUpdate(
        filter = doc("_id" $eq portfolioID.$oid),
        update = "orders" $addToSet $each(js.Array(orders: _*)),
        options = new FindAndUpdateOptions(returnOriginal = false))
    }

    @inline
    def findByContest(contestID: String)(implicit ec: ExecutionContext): js.Promise[js.Array[PortfolioData]] = {
      dao.find[PortfolioData]("contestID" $eq contestID).toArray()
    }

    @inline
    def findByPlayer(playerID: String)(implicit ec: ExecutionContext): js.Promise[js.Array[PortfolioData]] = {
      dao.find[PortfolioData]("playerID" $eq playerID).toArray()
    }

    @inline
    def findHeldSecurities(playerID: String)(implicit ec: ExecutionContext): Future[js.Array[String]] = {
      dao.find[PortfolioData](doc("playerID" $eq playerID, "active" $eq true), projection = Seq("positions").toProjection).toArray() map { portfolios =>
        for {
          portfolio <- portfolios.toList
          position <- portfolio.positions.toList.flatMap(_.toList)
          symbol <- position.symbol.toList
        } yield symbol
      } map (symbols => js.Array(symbols: _*))
    }

    @inline
    def findOneByID(portfolioID: String)(implicit ec: ExecutionContext): Future[Option[PortfolioData]] = {
      dao.findOneAsync[PortfolioData]("_id" $eq portfolioID.$oid)
    }

    @inline
    def findOneByPlayer(contestID: String, playerID: String)(implicit ec: ExecutionContext): Future[Option[PortfolioData]] = {
      dao.findOneAsync[PortfolioData](doc("contestID" $eq contestID, "playerID" $eq playerID))
    }

    @inline
    def findPerks(portfolioID: String)(implicit ec: ExecutionContext): Future[Option[PortfolioData]] = {
      dao.findOneAsync[PortfolioData]("_id" $eq portfolioID.$oid, fields = js.Array("cashAccount", "perks"))
    }

    @inline
    def findPositions(portfolioID: String)(implicit ec: ExecutionContext): Future[Option[js.Array[PositionData]]] = {
      dao.findOneAsync[PortfolioData]("_id" $eq portfolioID.$oid, fields = js.Array("positions")) map (_ map (_.positions getOrElse emptyArray))
    }

    @inline
    def purchasePerks(portfolioID: String, perkCodes: js.Array[String], perksCost: Double): js.Promise[FindAndModifyWriteOpResult] = {
      dao.findOneAndUpdate(
        filter = doc("_id" $eq portfolioID.$oid, "cashAccount.funds" $gte perksCost, "perks" $nin perkCodes),
        update = doc("cashAccount.funds" $inc -perksCost, "perks" $addToSet $each(perkCodes)),
        options = new FindAndUpdateOptions(returnOriginal = false))
    }

    @inline
    def totalInvestment(playerID: String)(implicit ec: ExecutionContext): Future[Double] = {
      for {
        portfolios <- findByPlayer(playerID)
        results = for {
          portfolio <- portfolios.toSeq
          position <- portfolio.positions.toOption.toSeq.flatMap(_.toSeq)
          totalCost <- position.totalCost.toOption.toSeq
        } yield totalCost
      } yield results.sum
    }

    @inline
    def transferCashFunds(portfolioID: String, amount: Double): js.Promise[FindAndModifyWriteOpResult] = {
      dao.findOneAndUpdate(
        filter = doc("_id" $eq portfolioID.$oid, "cashAccount.funds" $gte amount),
        update = $inc("cashAccount.funds" -> -amount, "marginAccount.funds" -> amount),
        options = new FindAndUpdateOptions(returnOriginal = false))
    }

    @inline
    def transferMarginFunds(portfolioID: String, amount: Double): js.Promise[FindAndModifyWriteOpResult] = {
      dao.findOneAndUpdate(
        filter = doc("_id" $eq portfolioID.$oid, "marginAccount.funds" $gte amount),
        update = $inc("cashAccount.funds" -> amount, "marginAccount.funds" -> -amount),
        options = new FindAndUpdateOptions(returnOriginal = false))
    }

  }

  /**
    * Portfolio DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class PortfolioDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getPortfolioDAO: PortfolioDAO = {
      db.collection("Portfolios").asInstanceOf[PortfolioDAO]
    }
  }

}
