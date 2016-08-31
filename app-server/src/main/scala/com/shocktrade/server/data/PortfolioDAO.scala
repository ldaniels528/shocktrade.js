package com.shocktrade.server.data

import com.shocktrade.javascript.models.contest.Position
import org.scalajs.nodejs._
import org.scalajs.nodejs.mongodb._
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.scalajs.js

/**
  * Portfolio Data Access Object
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait PortfolioDAO extends Collection

/**
  * Portfolio DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object PortfolioDAO {

  /**
    * Portfolio DAO Extensions
    * @param portfolioDAO the given [[PortfolioDAO Portfolio DAO]]
    */
  implicit class PortfolioDAOExtensions(val portfolioDAO: PortfolioDAO) {

    @inline
    def findNext(processingHost: String, updateDelay: FiniteDuration)(implicit ec: ExecutionContext) = {
      val thisUpdate = js.Date.now()
      val nextUpdate = thisUpdate + updateDelay
      portfolioDAO.findOneAndUpdate(
        filter = doc("status" $eq "ACTIVE", $or("nextUpdate" $exists false, "nextUpdate" $lte thisUpdate)),
        update = $set(doc("lastUpdate" -> thisUpdate, "nextUpdate" -> nextUpdate, "processedHost" -> processingHost)),
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
      portfolioDAO.updateOne(
        filter = doc(
          "_id" $eq wo.portfolioID,
          "cashAccount.cashFunds" $gte wo.totalCost),
        update = doc(
          "cashAccount.cashFunds" $inc -wo.totalCost,
          "cashAccount.asOfDate" $set wo.claim.asOfTime,
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
        portfolioOpt <- portfolioDAO.findOneFuture[PortfolioData]("_id" $eq portfolioID, fields = js.Array("positions"))
        results = for {
          portfolio <- portfolioOpt
          positions <- portfolio.positions.toOption.map(_.filter(_._id.exists(positionIDs.contains)))
          first <- positions.headOption
          symbol <- first.symbol.toOption

          // build the composite position
          newPosition = new Position(
            accountType = first.accountType,
            symbol = first.symbol,
            exchange = first.exchange,
            pricePaid = first.pricePaid,
            quantity = positions.map(_.quantity.getOrElse(0d)).sum,
            commission = positions.map(_.commission.getOrElse(0d)).sum,
            netValue = positions.map(_.netValue.getOrElse(0d)).sum,
            processedTime = js.Date.now()
          )
        } yield (symbol, newPosition, positions)

        updatedPortfolio <- results match {
          case Some((symbol, newPosition, positions)) =>
            // perform some validation
            if (positions.size < 2) die("At least two positions are required")
            if (!positions.forall(_.symbol.contains(symbol))) die("All positions must have the same symbol")

            // perform the update
            portfolioDAO.findOneAndUpdate(
              filter = doc("_id" $eq portfolioID),
              update = doc(
                "positions" $addToSet newPosition,
                $set(positions.map(p => "positions" -> doc("_id" -> p._id)))
              ),
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
      portfolioDAO.updateOne(
        filter = doc(
          "_id" $eq wo.portfolioID,
          "positions" $elemMatch("symbol" $eq wo.claim.symbol, "quantity" $gte wo.claim.quantity)),
        update = doc(
          $inc(
            "cashAccount.cashFunds" -> +wo.totalCost,
            "positions.$.quantity" -> -wo.claim.quantity,
            "positions.$.netValue" -> -wo.claim.quantity * wo.claim.price
          ),
          "cashAccount.asOfDate" $set wo.claim.asOfTime,
          "orders" $pull doc("_id" -> wo.order._id),
          "closedOrders" $addToSet wo.toClosedOrder("Processed")
        ))
    }

    @inline
    def removeEmptyPositions(portfolioID: ObjectID)(implicit ec: ExecutionContext) = {
      portfolioDAO.updateMany(filter = doc("_id" $eq portfolioID), update = doc("positions" $pull ("quantity" $eq 0d)))
    }

  }

  /**
    * Portfolio DAO Constructor
    * @param db the given [[Db database]]
    */
  implicit class PortfolioDAOConstructor(val db: Db) extends AnyVal {

    @inline
    def getPortfolioDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("Portfolios").mapTo[PortfolioDAO]
    }
  }

}