package com.shocktrade.common.dao.contest

import org.scalajs.nodejs.mongodb._
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.sjs.JsUnderOrHelper._
import org.scalajs.sjs.OptionHelper._

import scala.concurrent.ExecutionContext
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
    def create(portfolio: PortfolioData) = dao.insert(portfolio)

    @inline
    def cancelOrder(portfolioID: String, orderID: String)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      for {
        portfolio <- dao.findOneFuture[PortfolioData]("_id" $eq portfolioID.$oid, fields = js.Array("orders")) map (_.orDie(s"Portfolio # $portfolioID not found"))
        order = portfolio.orders.toOption.flatMap(_.find(_._id.contains(orderID))) orDie s"Order # $orderID not found"
        result <- dao.findOneAndUpdate(
          filter = doc("_id" $eq portfolioID.$oid),
          update = doc("orders" $pull ("_id" -> orderID), "orderHistory" $addToSet order)
        )
      } yield result
    }

    @inline
    def createOrder(portfolioID: String, order: OrderData)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.findOneAndUpdate(filter = doc("_id" $eq portfolioID.$oid), update = "orders" $addToSet order)
    }

    @inline
    def createOrders(portfolioID: String, orders: Seq[OrderData])(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.findOneAndUpdate(filter = doc("_id" $eq portfolioID.$oid), update = "orders" $addToSet $each(js.Array(orders: _*)))
    }

    @inline
    def findByContest(contestID: String)(implicit ec: ExecutionContext) = {
      dao.find("contestID" $eq contestID).toArrayFuture[PortfolioData]
    }

    @inline
    def findByPlayer(playerID: String)(implicit ec: ExecutionContext) = {
      dao.find("playerID" $eq playerID).toArrayFuture[PortfolioData]
    }

    @inline
    def findHeldSecurities(playerID: String)(implicit ec: ExecutionContext) = {
      dao.find("playerID" $eq playerID, projection = Seq("positions").toProjection).toArrayFuture[PortfolioData] map { portfolios =>
        for {
          portfolio <- portfolios.toList
          position <- portfolio.positions.toList.flatMap(_.toList)
          symbol <- position.symbol.toList
        } yield symbol
      } map (symbols => js.Array(symbols: _*))
    }

    @inline
    def findOneByID(portfolioID: String)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.findOneFuture[PortfolioData](doc("_id" $eq portfolioID.$oid))
    }

    @inline
    def findOneByPlayer(contestID: String, playerID: String)(implicit ec: ExecutionContext) = {
      dao.findOneFuture[PortfolioData](doc("contestID" $eq contestID, "playerID" $eq playerID))
    }

    @inline
    def findPerks(portfolioID: String)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.findOneFuture[PortfolioData]("_id" $eq portfolioID.$oid, fields = js.Array("cashAccount", "perks"))
    }

    @inline
    def findPositions(portfolioID: String)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.findOneFuture[PortfolioData]("_id" $eq portfolioID.$oid, fields = js.Array("positions")) map (_ map (_.positions getOrElse emptyArray))
    }

    @inline
    def purchasePerks(portfolioID: String, perkCodes: js.Array[String], perksCost: Double)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.findOneAndUpdate(
        filter = doc("_id" $eq portfolioID.$oid, "cashAccount.cashFunds" $gte perksCost, "perks" $nin perkCodes),
        update = doc(
          "cashAccount.cashFunds" $inc -perksCost,
          "perks" $addToSet $each(perkCodes)
        ))
    }

    @inline
    def totalInvestment(playerID: String)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      for {
        portfolios <- findByPlayer(playerID)
        results = for {
          portfolio <- portfolios.toSeq
          position <- portfolio.positions.toOption.toSeq.flatMap(_.toSeq)
          totalCost <- position.totalCost.toOption.toSeq
        } yield totalCost
      } yield results.sum
    }

  }

  /**
    * Portfolio DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class PortfolioDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getPortfolioDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("Portfolios").mapTo[PortfolioDAO]
    }
  }

}
