package com.shocktrade.server

import java.net.InetAddress
import java.util.Date

import com.shocktrade.controllers.Application._
import com.shocktrade.models.contest._
import com.shocktrade.util.BSONHelper._
import com.shocktrade.util.DateUtil._
import org.joda.time.DateTime
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONArray, BSONDocument => BS, BSONObjectID}
import reactivemongo.core.commands.{FindAndModify, Update}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.{implicitConversions, postfixOps}
import scala.util.Try

/**
 * Trading Data Access Object
 * @author lawrence.daniels@gmail.com
 */
object TradingDAO {
  private lazy val mc = db.collection[BSONCollection]("Contests")

  /**
   * Closes all expired orders; resulting in closed orders in order history
   * @param c the given [[Contest contest]]
   * @param asOfDate the given [[Date effective date]]
   * @param ec the implicit [[ExecutionContext execution context]]
   * @return a promise of the number of orders updated
   */
  def closeExpiredOrders(c: Contest, asOfDate: Date)(implicit ec: ExecutionContext) = {
    // find the expired orders
    val expiredOrders = getExpiredWorkOrders(c, asOfDate)

    // perform the updates
    Future.sequence(expiredOrders map { order =>
      mc.update(
        BS("_id" -> c.id, "participants" -> BS("$elemMatch" -> BS("_id" -> order.playerId))),
        BS(
          // set the lastMarketClose and lastUpdatedTime properties
          "$set" -> BS("lastMarketClose" -> asOfDate, "lastUpdatedTime" -> new Date()),

          // remove the expired orders
          "$pull" -> BS("participants.$.orders" -> BS("_id" -> order.id)),

          // add the orders to the order history
          "$addToSet" -> BS("participants.$.orderHistory" -> order.toClosedOrder(asOfDate, "Expired"))),
        upsert = false, multi = false) map (_.n)
    }) map (_.sum)
  }

  /**
   * Queries all active contests that haven't been update in 5 minutes
   * <pre>
   * db.Contests.count({"status": "ACTIVE",
   *    "$or" : [ { processedTime : { "$lte" : new Date() } }, { processedTime : { "$exists" : false } } ],
   *    "$or" : [ { expirationTime : { "$lte" : new Date() } }, { expirationTime : { "$exists" : false } } ] })
   * </pre>
   * @param asOfDate the last time an update was performed
   * @return a [[reactivemongo.api.Cursor]] of [[Contest]] instances
   */
  def getActiveContests(asOfDate: Date)(implicit ec: ExecutionContext) = {
    mc.find(BS(
      "status" -> ContestStatus.ACTIVE,
      "$or" -> BSONArray(Seq(BS("processedTime" -> BS("$lte" -> new DateTime(asOfDate).minusMinutes(5).toDate)), BS("processedTime" -> BS("$exists" -> false)))),
      "$or" -> BSONArray(Seq(BS("expirationTime" -> BS("$gte" -> asOfDate)), BS("expirationTime" -> BS("$exists" -> false))))
    )).cursor[Contest]
  }

  /**
   * Fails the given work order; creating a new closed order in the process
   * @param c the given [[Contest contest]]
   * @param wo the given [[WorkOrder work order]]
   * @param message the given error message
   * @param asOfDate the given [[Date effective date]]
   * @param ec the implicit [[ExecutionContext execution context]]
   * @return a promise of the number of orders updated
   */
  def failOrder(c: Contest, wo: WorkOrder, message: String, asOfDate: Date)(implicit ec: ExecutionContext) = {
    mc.update(
      // find the matching the record
      BS("_id" -> c.id, "participants" -> BS("$elemMatch" -> BS("_id" -> wo.playerId))),
      BS(
        // set the last update time
        "$set" -> BS("lastUpdatedTime" -> new Date()),

        // remove the order
        "$pull" -> BS("participants.$.orders" -> BS("_id" -> wo.id)),

        // create the order history record
        "$addToSet" -> BS("participants.$.orderHistory" -> wo.toClosedOrder(asOfDate, message))),
      upsert = false, multi = false) map (_.n)
  }

  /**
   * Attempts to retrieve a position matching the given criteria
   * @param contest the given [[Contest contest]]
   * @param claim the given [[Claim claim]]
   * @param minimumQty the minimum number of shares required to fullfill the claim
   * @return an option of a [[Position position]]
   */
  def findPosition(contest: Contest, claim: Claim, minimumQty: BigDecimal): Option[Position] = {
    for {
    // find the player by name
      player <- contest.participants find (_.id == claim.workOrder.playerId)

      // is there an existing position?
      position <- player.positions find (p => p.symbol == claim.symbol && p.quantity >= minimumQty)
    } yield position
  }

  /**
   * Increases a position by creating or updating a position
   * @param c the given [[Contest contest]]
   * @param claim the given [[Claim claim]]
   * @param asOfDate the given [[Date effective date]]
   * @param ec the implicit [[ExecutionContext execution context]]
   * @return a promise of the number of positions updated
   */
  def increasePosition(c: Contest, claim: Claim, asOfDate: Date)(implicit ec: ExecutionContext) = {
    findPosition(c, claim, 0.0d) match {
      case Some(position) => increasePositionUpdate(c, claim, position, asOfDate)
      case None => increasePositionCreate(c, claim, asOfDate)
    }
  }

  /**
   * Increases a position by creating a new position
   * @param c the given [[Contest contest]]
   * @param claim the given [[Claim claim]]
   * @param asOfDate the given [[Date effective date]]
   * @param ec the implicit [[ExecutionContext execution context]]
   * @return a promise of the number of positions updated
   */
  private def increasePositionCreate(c: Contest, claim: Claim, asOfDate: Date)(implicit ec: ExecutionContext) = {
    val wo = claim.workOrder
    mc.update(
      // find the matching the record
      BS("_id" -> c.id,
        "participants" -> BS("$elemMatch" -> BS("_id" -> wo.playerId, "fundsAvailable" -> BS("$gte" -> claim.cost)))),

      BS(
        // set the last update time
        "$set" -> BS("lastUpdatedTime" -> new Date(), "participants.$.lastTradeTime" -> new Date()),

        // deduct funds
        "$inc" -> BS("participants.$.fundsAvailable" -> -claim.cost),

        // remove the order
        "$pull" -> BS("participants.$.orders" -> BS("_id" -> wo.id)),

        "$addToSet" -> BS(
          // create the new position
          "participants.$.positions" -> claim.toPosition,

          // create the order history record
          "participants.$.orderHistory" -> wo.toClosedOrder(asOfDate, "Processed"))
      ),
      upsert = false, multi = false) map (_.n)
  }

  /**
   * Increases a position by updating an existing position
   * @param c the given [[Contest contest]]
   * @param claim the given [[Claim claim]]
   * @param asOfDate the given [[Date effective date]]
   * @param ec the implicit [[ExecutionContext execution context]]
   * @return a promise of the number of positions updated
   */
  private def increasePositionUpdate(c: Contest, claim: Claim, pos: Position, asOfDate: Date)(implicit ec: ExecutionContext) = {
    // create the increased position
    val increasedPos = claim.toPositionIncrease(pos)

    // perform the update
    val wo = claim.workOrder
    mc.update(
      // find the matching the record
      BS("_id" -> c.id,
        "participants" -> BS("$elemMatch" -> BS("_id" -> wo.playerId, "fundsAvailable" -> BS("$gte" -> claim.cost))),
        "participants.positions" -> BS("$elemMatch" -> BS("_id" -> pos.id))),

      BS(
        // set the last update time
        "$set" -> BS("lastUpdatedTime" -> new Date(), "participants.$.lastTradeTime" -> new Date()),

        // deduct funds
        "$inc" -> BS("participants.$.fundsAvailable" -> -claim.cost),

        // remove the order and existing position
        "$pull" -> BS("participants.$.orders" -> BS("_id" -> wo.id), "participants.$.positions" -> BS("_id" -> pos.id)),

        // add the new position (Phase 1) the order history records
        "$addToSet" -> BS(
          "participants.$.positions_TEMP" -> increasedPos,
          "participants.$.orderHistory" -> wo.toClosedOrder(asOfDate, "Processed"))),
      upsert = false, multi = false) map { result =>
      // if the update successful, perform phase 2 of the commit
      if (result.n > 0) {
        commitUpdatedPosition(c, wo, increasedPos)
        result.n
      }
      else throw new Exception(s"The position for ${claim.symbol} could not be updated (cost: ${claim.cost})")
    }
  }

  /**
   * Reduces a position
   * @param c the given [[Contest contest]]
   * @param claim the given [[Claim claim]]
   * @param asOfDate the given [[Date effective date]]
   * @param ec the implicit [[ExecutionContext execution context]]
   * @return a promise of the number of positions updated
   */
  def reducePosition(c: Contest, claim: Claim, asOfDate: Date)(implicit ec: ExecutionContext) = {
    // lookup the existing position
    findPosition(c, claim, claim.quantity) match {
      case Some(existingPos) =>
        // create the reduced position
        val reducedPosition = claim.toPositionDecrease(existingPos)

        // perform the atomic update
        val wo = claim.workOrder
        mc.update(
          // find the matching the record
          BS("_id" -> c.id,
            "participants._id" -> wo.playerId,
            "participants.positions" -> BS("$elemMatch" -> BS("_id" -> existingPos.id,
              "symbol" -> claim.symbol, "quantity" -> BS("$gte" -> claim.quantity)))),

          // set the last update time
          BS(
            "$set" -> BS("lastUpdatedTime" -> new Date(), "participants.$.lastTradeTime" -> new Date()),

            // increase the funds
            "$inc" -> BS("participants.$.fundsAvailable" -> claim.proceeds),

            // remove the order and existing position
            "$pull" -> BS("participants.$.orders" -> BS("_id" -> wo.id), "participants.$.positions" -> BS("_id" -> existingPos.id)),

            // add the performance & order history records
            "$addToSet" -> BS(
              "participants.$.positions_TEMP" -> reducedPosition,
              "participants.$.performance" -> claim.toPerformance(existingPos),
              "participants.$.orderHistory" -> wo.toClosedOrder(asOfDate, "Processed"))),
          upsert = false, multi = false) map { result =>
          if (result.n > 0) {
            commitUpdatedPosition(c, wo, reducedPosition)
            result.n
          } else {
            throw new Exception(s"A qualifying position could not be found")
          }
        }

      // if the update successful, perform phase 2 of the commit
      case None =>
        throw new Exception(s"A qualifying position could not be found")
    }
  }

  /**
   * Attempts to attain a lock on a contest for processing
   * @param contestId the given [[BSONObjectID contest ID]]
   * @param ec the implicit [[ExecutionContext execution context]]
   * @return a promise of an option of a locked  [[Contest contest]] and an [[Date lock expiration time]]
   */
  def lockContest(contestId: BSONObjectID)(implicit ec: ExecutionContext): Future[Option[(Contest, Date)]] = {
    val expirationTime = new DateTime(new Date()).plusHours(1).toDate
    val lockHost = Try(InetAddress.getLocalHost.getHostName).toOption.orNull
    db.command(FindAndModify(
      collection = "Contests",
      query = BS("_id" -> contestId, "$or" -> BSONArray(BS("locked" -> false), BS("lockExpirationTime" -> BS("$lte" -> new Date())), BS("lockExpirationTime" -> BS("$exists" -> false)))),
      modify = new Update(BS("$set" -> BS("locked" -> true, "lockHost" -> lockHost, "lockTime" -> new Date(), "lockExpirationTime" -> expirationTime)), fetchNewObject = true),
      fields = None,
      upsert = false)) map (_ flatMap (_.seeAsOpt[Contest])) map (_ map ((_, expirationTime)))
  }

  /**
   * Releases the processing lock
   * @param contestId the given [[BSONObjectID contest ID]]
   * @param expirationTime the given [[Date lock expiration time]]
   * @param ec the implicit [[ExecutionContext execution context]]
   * @return a promise of an option of an unlocked [[Contest contest]]
   */
  def unlockContest(contestId: BSONObjectID, expirationTime: Date)(implicit ec: ExecutionContext): Future[Option[Contest]] = {
    db.command(FindAndModify(
      collection = "Contests",
      query = BS("_id" -> contestId, "locked" -> true, "lockExpirationTime" -> expirationTime),
      modify = new Update(BS("$set" -> BS("locked" -> false, "lockExpirationTime" -> new Date())), fetchNewObject = true),
      fields = None,
      upsert = false)) map (_ flatMap (_.seeAsOpt[Contest]))
  }

  /**
   * Performs a secondary update because MongoDB doesn't allow a record to be pulled and added to the sub-document
   * in a single atomic operation
   * @param c the given [[Contest contest]]
   * @param wo the given [[WorkOrder work order]]
   * @param tmpPos the given ephemeral [[Position position]]
   * @param ec the implicit [[ExecutionContext execution context]]
   * @return a promise of the [[reactivemongo.core.commands.LastError last error]] result
   */
  private def commitUpdatedPosition(c: Contest, wo: WorkOrder, tmpPos: Position)(implicit ec: ExecutionContext) = {
    mc.update(
      BS("_id" -> c.id,
        "participants" -> BS("$elemMatch" -> BS("_id" -> wo.playerId)),
        "positions_TEMP" -> BS("$elemMatch" -> BS("_id" -> tmpPos.id))),

      BS("$pull" -> BS("participants.$.positions_TEMP" -> BS("_id" -> tmpPos.id))) ++
        (if (tmpPos.quantity > 0) BS("$addToSet" -> BS("participants.$.positions" -> tmpPos)) else BS()))
  }

  def getExpiredWorkOrders(c: Contest, asOfDate: Date): Seq[WorkOrder] = {
    for {
      p <- c.participants
      o <- p.orders filter (_.expirationTime.exists(_ < asOfDate))
    } yield toWorkOrder(p, o)
  }

  def getOpenWorkOrders(c: Contest, asOfDate: Date): Seq[WorkOrder] = {
    for {
      p <- c.participants
      o <- p.orders filter (o => o.expirationTime.isEmpty || o.expirationTime.exists(_ >= asOfDate))
    } yield toWorkOrder(p, o)
  }

  private def toWorkOrder(p: Participant, o: Order): WorkOrder = {
    WorkOrder(
      id = o.id,
      playerId = p.id,
      symbol = o.symbol,
      exchange = o.exchange,
      orderTime = o.creationTime,
      expirationTime = o.expirationTime,
      orderType = o.orderType,
      price = Option(o.price),
      priceType = o.priceType,
      quantity = o.quantity,
      commission = o.commission,
      volumeAtOrderTime = o.volumeAtOrderTime,
      emailNotify = o.emailNotify
    )
  }

}
