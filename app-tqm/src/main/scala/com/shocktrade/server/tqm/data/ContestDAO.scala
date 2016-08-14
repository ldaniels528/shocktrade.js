package com.shocktrade.server.tqm.data

import org.scalajs.nodejs._
import org.scalajs.nodejs.mongodb._
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.scalajs.js

/**
  * Contest Data Access Object
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ContestDAO extends Collection

/**
  * Contest DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object ContestDAO {

  /**
    * Contest DAO Extensions
    * @param contestDAO the given [[ContestDAO Contest DAO]]
    */
  implicit class ContestDAOExtensions(val contestDAO: ContestDAO) {

    @inline
    def findNext(processingHost: String, updateDelay: FiniteDuration)(implicit ec: ExecutionContext) = {
      val thisUpdate = js.Date.now()
      val nextUpdate = thisUpdate + updateDelay
      contestDAO.findOneAndUpdate(
        filter = doc("status" $eq "ACTIVE", $or("nextUpdate" $exists false, "nextUpdate" $lte thisUpdate)),
        update = $set(doc("lastUpdate" -> thisUpdate, "nextUpdate" -> nextUpdate, "processedHost" -> processingHost)),
        options = new FindAndUpdateOptions(upsert = false, returnOriginal = false)
      ).toFuture map {
        case result if result.isOk => result.valueAs[ContestData]
        case result =>
          console.error("error => %j", result.lastErrorObject)
          throw new RuntimeException(s"Contest could not be updated: ${result.lastErrorObject}")
      }
    }

    @inline
    def insertPosition(wo: WorkOrder)(implicit ec: ExecutionContext) = {
      contestDAO.findOneAndUpdate(
        filter = doc(
          "_id" $eq wo.contestID,
          "participants._id" $eq wo.participantID,
          "participants.cashAccount.cashFunds" $gte wo.totalCost),
        update = doc(
          "participants.$.cashAccount.cashFunds" $inc -wo.totalCost,
          "participants.$.cashAccount.asOfDate" $set wo.claim.asOfTime,
          "participants.$.orders" $pull doc("_id" -> wo.order._id),
          $addToSet(
            "participants.$.positions" -> wo.toNewPosition,
            "participants.$.closedOrders" -> wo.toClosedOrder("Processed")
          )
        ),
        options = new FindAndUpdateOptions(upsert = false, returnOriginal = false)
      ) map {
        case result if result.isOk && result.value != null => result.valueAs[ContestData]
        case result =>
          console.error("error: %j", result)
          None
      }
    }

    @inline
    def reducePosition(wo: WorkOrder)(implicit ec: ExecutionContext) = {
      for {
      // find the contest with the position we want to update
        contestOpt <- contestDAO.findOneFuture[ContestData](selector = doc(
          "_id" $eq wo.contestID,
          "participants._id" $eq wo.participantID,
          "participants.positions" $elemMatch("symbol" $eq wo.claim.symbol, "quantity" $gte wo.claim.quantity)),
          fields = js.Array("participants.positions"))

        // find the existing position
        existingPosOpt = for {
          contest <- contestOpt
          participant <- contest.participants.toOption.flatMap(_.headOption)
          positions <- participant.positions.toOption
          position <- positions.find(p => p.symbol.contains(wo.claim.symbol) && p.quantity.exists(_ >= wo.claim.quantity))
        } yield position

        // atomically update the position
        updatedContestOpt <- existingPosOpt match {
          case Some(existingPos) =>
            val reducedPos = existingPos.copy(quantity = existingPos.quantity.map(_ - wo.claim.quantity))
            contestDAO.bulkWrite(js.Array(
              // step 1
              updateOne(
                filter = doc(
                  "_id" $eq wo.contestID,
                  "participants._id" $eq wo.participantID,
                  "participants.positions" $elemMatch("_id" $eq reducedPos._id, "quantity" $gte wo.claim.quantity)),
                update = doc(
                  "participants.$.cashAccount.cashFunds" $inc wo.totalCost,
                  "participants.$.cashAccount.asOfDate" $set wo.claim.asOfTime,
                  $pull(
                    "participants.$.orders" -> doc("_id" -> wo.order._id),
                    "participants.$.positions" -> doc("_id" -> reducedPos._id)
                  ),
                  $addToSet(
                    "participants.$.POSITIONS" -> reducedPos,
                    "participants.$.closedOrders" -> wo.toClosedOrder("Processed")
                  ))),

              // step 2
              updateOne(
                filter = doc("_id" $eq wo.contestID, "participants._id" $eq wo.participantID),
                update = doc(
                  "participants.$.POSITIONS" $pull doc("_id" -> reducedPos._id),
                  "participants.$.positions" $addToSet reducedPos
                )
              )
            ))
          case None =>
            die(s"A position for ${wo.claim.symbol} x ${wo.claim.quantity} was not found")
        }

      } yield updatedContestOpt
    }

  }

  /**
    * Contest DAO Constructor
    * @param db the given [[Db database]]
    */
  implicit class ContestDAOConstructor(val db: Db) extends AnyVal {

    @inline
    def getContestDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("Contests").mapTo[ContestDAO]
    }
  }

}