package com.shocktrade.server

import java.net.InetAddress
import java.util.Date

import com.shocktrade.controllers.Application._
import com.shocktrade.models.contest.OrderType.OrderType
import com.shocktrade.models.contest.PriceType.PriceType
import com.shocktrade.models.contest._
import com.shocktrade.util.BSONHelper._
import org.joda.time.DateTime
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONArray, BSONDocument => BS, BSONObjectID}
import reactivemongo.core.commands.{FindAndModify, Update}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.Try

/**
 * Trading Data Access Object
 * @author lawrence.daniels@gmail.com
 */
object TradingDAO {
  private lazy val mc = db.collection[BSONCollection]("Contests")

  /**
   * Queries all active contests that haven't been update in 5 minutes
   * @param asOfDate the last time an update was performed
   * @return a [[reactivemongo.api.Cursor]] of [[Contest]] instances
   */
  def getActiveContests(asOfDate: Date)(implicit ec: ExecutionContext) = {
    /*
      db.Contests.count({
          "status": "ACTIVE",
          "$or" : [ { processedTime : { "$lte" : new Date() } }, { processedTime : { "$exists" : false } } ],
          "$or" : [ { expirationTime : { "$lte" : new Date() } }, { expirationTime : { "$exists" : false } } ] })
     */
    mc.find(BS(
      "status" -> ContestStatus.ACTIVE,
      "$or" -> BSONArray(Seq(BS("processedTime" -> BS("$lte" -> new DateTime(asOfDate).minusMinutes(5).toDate)), BS("processedTime" -> BS("$exists" -> false)))),
      "$or" -> BSONArray(Seq(BS("expirationTime" -> BS("$gte" -> asOfDate)), BS("expirationTime" -> BS("$exists" -> false))))
    )).cursor[Contest]
  }

  def closeExpiredOrders(c: Contest, asOfDate: Date) = {
    // find the expired orders
    val expiredOrders = getExpiredWorkOrders(c, asOfDate)

    // perform the updates
    (expiredOrders map { order =>
      mc.update(
        BS("_id" -> c.id, "participants" -> BS("$elemMatch" -> BS("name" -> order.playerName))),
        BS(
          // set the lastMarketClose and lastUpdatedTime properties
          "$set" -> BS("lastMarketClose" -> asOfDate, "lastUpdatedTime" -> new Date()),

          // remove the expired orders
          "$pull" -> BS("participants.$.orders" -> BS("_id" -> order._id)),

          // add the orders to the order history
          "$addToSet" -> BS("participants.$.orderHistory" -> toOrderHistory(order, asOfDate, "Expired"))),
        upsert = false, multi = false) map (_.n)
    }).sum
  }

  def failOrder(c: Contest, wo: WorkOrder, message: String, asOfDate: Date) = {
    val result = mc.update(
      // find the matching the record
      BS("_id" -> c.id, "participants" -> BS("$elemMatch" -> BS("name" -> wo.playerName))),
      BS(
        // set the last update time
        "$set" -> BS("lastUpdatedTime" -> new Date()),

        // remove the order
        "$pull" -> BS("participants.$.orders" -> BS("_id" -> wo._id)),

        // create the order history record
        "$addToSet" -> BS("participants.$.orderHistory" -> toOrderHistory(wo, asOfDate, message))),
      upsert = false, multi = false) map (_.n)
  }

  def findPosition(contest: Contest, claim: Claim, minimumQty: BigDecimal): Option[Position] = {
    for {
    // find the player by name
      player <- contest.participants find (_.name == claim.workOrder.playerName)

      // is there an existing position?
      position <- player.positions find (p => p.symbol == claim.symbol && p.quantity >= minimumQty)
    } yield position
  }

  def increasePosition(c: Contest, claim: Claim, asOfDate: Date) = {
    findPosition(c, claim, 0.0d) match {
      case Some(position) => increasePositionUpdate(c, claim, position, asOfDate)
      case None => increasePositionCreate(c, claim, asOfDate)
    }
  }

  /**
   * Increases a position by creating a new position
   */
  private def increasePositionCreate(c: Contest, claim: Claim, asOfDate: Date) = {
    val wo = claim.workOrder
    mc.update(
      // find the matching the record
      BS("_id" -> c.id,
        "participants" -> BS("$elemMatch" -> BS("name" -> wo.playerName, "fundsAvailable" -> BS("$gte" -> claim.cost)))),
      BS(
        // set the last update time
        "$set" -> BS("lastUpdatedTime" -> new Date(), "participants.$.lastTradeTime" -> new Date()),

        // deduct funds
        "$inc" -> BS("participants.$.fundsAvailable" -> -claim.cost),

        // remove the order
        "$pull" -> BS("participants.$.orders" -> BS("_id" -> wo._id)),

        "$addToSet" -> BS(
          // create the new position
          "participants.$.positions" -> toPosition(claim),

          // create the order history record
          "participants.$.orderHistory" -> toOrderHistory(wo, asOfDate, "Processed"))
      ),
      upsert = false, multi = false) map (_.n)
  }

  /**
   * Increases a position by removing an existing position, and creating a new aggregate position
   */
  private def increasePositionUpdate(c: Contest, claim: Claim, pos: Position, asOfDate: Date) = {
    // create the increased position
    val increasedPos = toPositionIncrease(claim, pos)

    // perform the update
    val wo = claim.workOrder
    mc.update(
      // find the matching the record
      BS("_id" -> c.id,
        "participants" -> BS("$elemMatch" -> BS("name" -> wo.playerName, "fundsAvailable" -> BS("$gte" -> claim.cost))),
        "participants.positions" -> BS("$elemMatch" -> BS("_id" -> pos.id))),
      BS(
        // set the last update time
        "$set" -> BS("lastUpdatedTime" -> new Date(), "participants.$.lastTradeTime" -> new Date()),

        // deduct funds
        "$inc" -> BS("participants.$.fundsAvailable" -> -claim.cost),

        // remove the order and existing position
        "$pull" -> BS("participants.$.orders" -> BS("_id" -> wo._id), "participants.$.positions" -> BS("_id" -> pos.id)),

        // add the new position (Phase 1) the order history records
        "$addToSet" -> BS(
          "participants.$.positions_TEMP" -> increasedPos,
          "participants.$.orderHistory" -> toOrderHistory(wo, asOfDate, "Processed"))),
      upsert = false, multi = false) map { result =>
      // if the update successful, perform phase 2 of the commit
      if (result.n > 0) {
        commitModifiedPosition(c, wo, increasedPos)
        result.n
      }
      else throw new Exception(s"The position for ${claim.symbol} could not be updated (cost: ${claim.cost})")
    }
  }

  def reducePosition(c: Contest, claim: Claim, asOfDate: Date) = {
    // lookup the existing position
    findPosition(c, claim, claim.quantity) match {
      case Some(existingPos) =>
        // create the reduced position
        val reducedPosition = toPositionDecrease(claim, existingPos)

        // perform the atomic update
        val wo = claim.workOrder
        mc.update(
          // find the matching the record
          BS("_id" -> c.id,
            "participants.name" -> wo.playerName,
            "participants.positions" -> BS("$elemMatch" -> BS("_id" -> existingPos.id,
              "symbol" -> claim.symbol, "quantity" -> BS("$gte" -> claim.quantity)))),

          // set the last update time
          BS(
            "$set" -> BS("lastUpdatedTime" -> new Date(), "participants.$.lastTradeTime" -> new Date()),

            // increase the funds
            "$inc" -> BS("participants.$.fundsAvailable" -> claim.proceeds),

            // remove the order and existing position
            "$pull" -> BS("participants.$.orders" -> BS("_id" -> wo._id), "participants.$.positions" -> BS("_id" -> existingPos.id)),

            // add the performance & order history records
            "$addToSet" -> BS(
              "participants.$.positions_TEMP" -> reducedPosition,
              "participants.$.performance" -> toPerformance(claim, existingPos),
              "participants.$.orderHistory" -> toOrderHistory(wo, asOfDate, "Processed"))),
          upsert = false, multi = false) map { result =>
          if (result.n > 0) {
            commitModifiedPosition(c, wo, reducedPosition)
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

  def unlockContest(contestId: BSONObjectID, expirationTime: Date)(implicit ec: ExecutionContext): Future[Option[Contest]] = {
    db.command(FindAndModify(
      collection = "Contests",
      query = BS("_id" -> contestId, "locked" -> true, "lockExpirationTime" -> expirationTime),
      modify = new Update(BS("$set" -> BS("locked" -> false, "lockExpirationTime" -> new Date())), fetchNewObject = true),
      fields = None,
      upsert = false)) map (_ flatMap (_.seeAsOpt[Contest]))
  }

  private def commitModifiedPosition(c: Contest, wo: WorkOrder, tmpPos: BS) {
    // get the ID and quantity of the temporary position
    val tmpId = tmpPos.getAs[BSONObjectID]("_id")
    val qty = tmpPos.getAs[BigDecimal]("quantity")

    // perform the update
    mc.update(
      BS("_id" -> c.id,
        "participants" -> BS("$elemMatch" -> BS("name" -> wo.playerName)),
        "positions_TEMP" -> BS("$elemMatch" -> BS("_id" -> tmpId))),
      BS(
        "$pull" -> BS("participants.$.positions_TEMP" -> BS("_id" -> tmpId))) ++
        (if (qty.exists(_ > 0)) BS("$addToSet" -> BS("participants.$.positions" -> tmpPos)) else BS()))
  }

  def getExpiredWorkOrders(c: Contest, asOfDate: Date): Seq[WorkOrder] = {
    (for {
      p <- c.participants
      o <- p.orders filter (_.expirationTime.exists(_.before(asOfDate)))
    } yield toWorkOrder(p, o)).flatten
  }

  def getOpenWorkOrders(c: Contest, asOfDate: Date): Seq[WorkOrder] = {
    (for {
      p <- c.participants
      o <- p.orders filter (o => o.expirationTime.isEmpty || o.expirationTime.exists(t => t == asOfDate || t.after(asOfDate)))
    } yield toWorkOrder(p, o)).flatten
  }

  private def toOrderHistory(wo: WorkOrder, asOfDate: Date, message: String) = {
    import wo._
    BS("_id" -> _id,
      "orderType" -> orderType,
      "symbol" -> symbol,
      "exchange" -> exchange,
      "limitPrice" -> limitPrice,
      "priceType" -> priceType,
      "quantity" -> quantity,
      "volumeAtOrderTime" -> volumeAtOrderTime,
      "commission" -> commission,
      "orderTime" -> orderTime,
      "expirationTime" -> expirationTime,
      "processedTime" -> asOfDate,
      "message" -> message)
  }

  private def toPerformance(claim: Claim, existingPos: Position) = {
    BS("_id" -> BSONObjectID.generate,
      "orderId" -> claim.workOrder._id,
      "symbol" -> claim.symbol,
      "exchange" -> claim.exchange,
      "pricePaid" -> existingPos.pricePaid,
      "priceSold" -> claim.price,
      "commision1" -> existingPos.commission,
      "commision2" -> claim.commission,
      "quantity" -> claim.quantity,
      "proceeds" -> claim.proceeds,
      "cost" -> existingPos.cost,
      "purchasedDate" -> existingPos.processedTime,
      "soldDate" -> claim.purchaseTime)
  }

  private def toPosition(claim: Claim) = {
    import claim._
    BS("_id" -> BSONObjectID.generate,
      "symbol" -> symbol,
      "exchange" -> exchange,
      "price" -> price,
      "quantity" -> quantity,
      "commission" -> commission,
      "purchaseTime" -> purchaseTime)
  }

  private def toPositionIncrease(claim: Claim, existingPos: Position) = {
    import claim._

    // compute the adjusted quantity
    val totalQuantity = existingPos.quantity + claim.quantity

    // compute the adjusted price
    val adjPrice = (price * quantity) + (existingPos.pricePaid * existingPos.quantity)

    // create the position
    BS("_id" -> BSONObjectID.generate,
      "symbol" -> symbol,
      "exchange" -> exchange,
      "price" -> adjPrice,
      "quantity" -> totalQuantity,
      "commission" -> commission,
      "purchaseTime" -> purchaseTime)
  }

  private def toPositionDecrease(claim: Claim, existingPos: Position) = {
    import claim._

    // compute the adjusted quantity
    val reducedQuantity = existingPos.quantity - claim.quantity

    // create the position
    BS("_id" -> BSONObjectID.generate,
      "symbol" -> symbol,
      "exchange" -> exchange,
      "price" -> price,
      "quantity" -> reducedQuantity,
      "commission" -> commission,
      "purchaseTime" -> purchaseTime)
  }

  private def toWorkOrder(p: Participant, o: Order): WorkOrder = {
    WorkOrder(
      _id = o.id,
      playerName = p.name,
      orderType = o.orderType,
      symbol = o.symbol,
      exchange = o.exchange,
      limitPrice = Option(o.price),
      priceType = o.priceType,
      quantity = o.quantity,
      volumeAtOrderTime = o.volumeAtOrderTime,
      orderTime = o.creationTime,
      commission = o.commission,
      expirationTime = o.expirationTime.getOrElse(new DateTime().plusDays(1).toDate)
    )
  }

  /**
   * Represents a claim, which becomes a new position
   */
  case class Claim(symbol: String,
                   exchange: String,
                   price: BigDecimal,
                   quantity: BigDecimal,
                   commission: BigDecimal,
                   purchaseTime: Date,
                   workOrder: WorkOrder) {

    /**
     * Computes the cost of the order (BUY orders)
     */
    def cost: BigDecimal = price * quantity + commission

    /**
     * Computes the proceeds of the order (SELL orders)
     */
    def proceeds: BigDecimal = price * quantity - commission

  }

  /**
   * Represents a temporary data transfer object for order claiming
   */
  case class WorkOrder(_id: BSONObjectID,
                       playerName: String,
                       orderType: OrderType,
                       symbol: String,
                       exchange: String,
                       limitPrice: Option[BigDecimal],
                       priceType: PriceType,
                       quantity: BigDecimal,
                       volumeAtOrderTime: Long,
                       orderTime: Date,
                       commission: BigDecimal,
                       expirationTime: Date)

}
