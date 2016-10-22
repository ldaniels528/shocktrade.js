package com.shocktrade.server.dao.contest

import java.util.UUID

import org.scalajs.nodejs.console
import org.scalajs.nodejs.mongodb._
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.sjs.DateHelper._
import org.scalajs.sjs.JsUnderOrHelper._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.scalajs.js

/**
  * Portfolio Update DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait PortfolioUpdateDAO extends PortfolioDAO

/**
  * Portfolio Update DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object PortfolioUpdateDAO {

  /**
    * Portfolio Update DAO Extensions
    * @param dao the given [[PortfolioUpdateDAO Portfolio DAO]]
    */
  implicit class PortfolioDAOExtensions(val dao: PortfolioUpdateDAO) {

    @inline
    def disqualifyOrder(portfolioID: ObjectID, order: OrderData, message: String)(implicit ec: ExecutionContext) = {
      dao.findOneAndUpdate(
        filter = doc("_id" $eq portfolioID, "orders" $elemMatch ("_id" $eq order._id)),
        update = doc(
          "orders" $pull doc("_id" -> order._id),
          "closedOrders" $addToSet order.toClosedOrder(message)
        ))
    }

    @inline
    def findActiveOrders()(implicit ec: ExecutionContext) = {
      dao.find(selector = "status" $eq "ACTIVE", projection = Seq("orders").toProjection).toArrayFuture[PortfolioData]
    }

    @inline
    def findNext(processingHost: String, updateDelay: FiniteDuration)(implicit ec: ExecutionContext) = {
      val lastUpdate = new js.Date()
      val nextUpdate = lastUpdate + updateDelay
      dao.findOneAndUpdate(
        filter = doc("status" $eq "ACTIVE", $or("nextUpdate" $exists false, "nextUpdate" $lte lastUpdate)),
        update = $set(doc(
          "lastUpdate" -> lastUpdate,
          "nextUpdate" -> nextUpdate,
          "processedHost" -> processingHost
        )),
        options = new FindAndUpdateOptions(upsert = false, returnOriginal = false)
      ) map {
        case result if result.isOk => result.valueAs[PortfolioData]
        case result =>
          console.error("failure => %j", result)
          throw new RuntimeException(s"Portfolio could not be updated: ${result.lastErrorObject}")
      }
    }

    @inline
    def insertPosition(wo: WorkOrder)(implicit ec: ExecutionContext) = {
      dao.updateOne(
        filter = doc(
          "_id" $eq wo.portfolioID,
          wo.fundingSource $gte wo.totalCost),
        update = doc(
          wo.fundingSource $inc -wo.totalCost,
          wo.fundingAsOfDate $set wo.claim.asOfTime,
          "orders" $pull doc("_id" -> wo.order._id),
          $addToSet(
            "positions" -> wo.toNewPosition,
            "closedOrders" -> wo.toClosedOrder("Processed")
          )
        ))
    }

    @inline
    def mergePositions(portfolioID: ObjectID, positionIDs: Seq[String])(implicit ec: ExecutionContext) = {
      for {
        portfolioOpt <- dao.findOneFuture[PortfolioData]("_id" $eq portfolioID, fields = js.Array("positions"))
        results = for {
          portfolio <- portfolioOpt
          positions <- portfolio.positions.toOption.map(_.filter(_._id.exists(positionIDs.contains)))
          first <- positions.headOption
          symbol <- first.symbol.toOption

          // build the composite position
          newPosition = new PositionData(
            _id = UUID.randomUUID().toString,
            accountType = first.accountType,
            symbol = first.symbol,
            exchange = first.exchange,
            pricePaid = first.pricePaid,
            quantity = positions.map(_.quantity.orZero).sum,
            commission = positions.map(_.commission.orZero).sum,
            netValue = positions.map(_.netValue.orZero).sum,
            processedTime = new js.Date())
        } yield (symbol, newPosition, positions)

        updatedPortfolio <- results match {
          case Some((symbol, newPosition, positions)) =>
            // perform some validation
            if (positions.size < 2) die("At least two positions are required")
            if (!positions.forall(_.symbol.contains(symbol))) die("All positions must have the same symbol")

            // perform the update
            dao.findOneAndUpdate(
              filter = doc("_id" $eq portfolioID),
              update = doc("positions" $addToSet newPosition, $set(positions.map(p => "positions" -> doc("_id" -> p._id)))),
              options = new FindAndUpdateOptions(upsert = false, returnOriginal = false)
            ) map {
              case result if result.isOk && result.value != null => result.valueAs[PortfolioData]
              case result =>
                console.error("failure => %j | positions => %j", result, js.Array(positions: _*))
                die("Unable to merge position")
            }
          case None =>
            die("The merge could not be completed")
        }

      } yield updatedPortfolio
    }

    @inline
    def reducePosition(wo: WorkOrder)(implicit ec: ExecutionContext) = {
      for {
        portfolio_? <- dao.findOneFuture[PortfolioData]("_id" $eq wo.portfolioID, fields = js.Array("positions"))
        positions = portfolio_?.flatMap(_.positions.toOption).getOrElse(emptyArray)
        positionToSell_? = positions.find(p => p.symbol.contains(wo.claim.symbol) && p.quantity.exists(_ >= wo.claim.quantity))

        result <- positionToSell_? match {
          case Some(positionToSell) =>
            dao.updateOne(
              filter = doc(
                "_id" $eq wo.portfolioID,
                "positions" $elemMatch("symbol" $eq wo.claim.symbol, "quantity" $gte wo.claim.quantity)),
              update = doc(
                $inc(
                  wo.fundingSource -> +wo.totalCost,
                  "positions.$.quantity" -> -wo.claim.quantity,
                  "positions.$.netValue" -> -wo.claim.quantity * wo.claim.price
                ),
                $addToSet(
                  "closedOrders" -> wo.toClosedOrder("Processed"),
                  "performance" -> wo.toPerformance(positionToSell)
                ),
                wo.fundingAsOfDate $set wo.claim.asOfTime,
                "orders" $pull doc("_id" -> wo.order._id)
              )).toFuture
          case None =>
            disqualifyOrder(wo.portfolioID, wo.order, "No suitable position found").toFuture map { _ =>
              New[UpdateWriteOpResultObject]
            }
        }

      } yield result
    }

    @inline
    def removeEmptyPositions(portfolioID: ObjectID)(implicit ec: ExecutionContext) = {
      dao.updateMany(filter = doc("_id" $eq portfolioID), update = doc("positions" $pull ("quantity" $eq 0d)))
    }

  }

  /**
    * Portfolio Update DAO Constructor
    * @param db the given [[Db database]]
    */
  implicit class PortfolioUpdateDAOConstructor(val db: Db) extends AnyVal {

    @inline
    def getPortfolioUpdateDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("Portfolios").mapTo[PortfolioUpdateDAO]
    }
  }

}