package com.shocktrade.server.trading

import java.util.Date

import com.ldaniels528.commons.helpers.OptionHelper._
import com.shocktrade.controllers.ProfileResources._
import com.shocktrade.models.contest.PerkTypes._
import com.shocktrade.models.contest._
import com.shocktrade.util.BSONHelper._
import com.shocktrade.util.DateUtil._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONDocument => BS, _}
import reactivemongo.core.commands.{FindAndModify, LastError, Update}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Success, Try}

/**
 * Contest Data Access Object
 * @author lawrence.daniels@gmail.com
 */
object ContestDAO {
  private lazy val mc = db.collection[BSONCollection]("Contests")

  /////////////////////////////////////////////////////////////////////////////////
  //        Contests
  /////////////////////////////////////////////////////////////////////////////////

  /**
   * Creates a new contest
   * @param c the given [[Contest]]
   * @param ec the implicit [[ExecutionContext execution context]]
   * @return a promise of the [[LastError outcome]]
   */
  def createContest(c: Contest)(implicit ec: ExecutionContext): Future[LastError] = mc.insert(c)

  def closeContest(c: Contest)(implicit ec: ExecutionContext) = {
    db.command(FindAndModify(
      collection = "Contests",
      query = BS("_id" -> c.id),
      modify = new Update(BS(
        "$set" -> BS("status" -> ContestStatuses.CLOSED)),
        fetchNewObject = true),
      upsert = false)) map (_ flatMap (_.seeAsOpt[Contest]))
  }

  /**
   * Deletes a contest by ID
   * @param contestId the given contest ID
   * @param ec the implicit [[ExecutionContext execution context]]
   * @return a promise of the [[LastError outcome]]
   */
  def deleteContestByID(contestId: BSONObjectID)(implicit ec: ExecutionContext): Future[LastError] = {
    mc.remove(query = BS("_id" -> contestId), firstMatchOnly = true)
  }

  /**
   * Queries all active contests that haven't been update in 5 minutes
   * <pre>
   * db.Contests.count({"status": "ACTIVE",
   * "$or" : [ { processedTime : { "$lte" : new Date() } }, { processedTime : { "$exists" : false } } ],
   * "$or" : [ { expirationTime : { "$lte" : new Date() } }, { expirationTime : { "$exists" : false } } ] })
   * </pre>
   * @param lastProcessedTime the last time an update was performed
   * @return a [[reactivemongo.api.Cursor]] of [[Contest]] instances
   */
  def getActiveContests(asOfDate: Date, lastProcessedTime: Date)(implicit ec: ExecutionContext) = {
    mc.find(BS(
      "status" -> ContestStatuses.ACTIVE,
      "startTime" -> BS("$lte" -> asOfDate),
      "$or" -> BSONArray(Seq(BS("processedTime" -> BS("$lte" -> lastProcessedTime)), BS("processedTime" -> BS("$exists" -> false)))) //,
      // "$or" -> BSONArray(Seq(BS("expirationTime" -> BS("$gte" -> asOfDate)), BS("expirationTime" -> BS("$exists" -> false))))
    )).cursor[Contest]
  }

  def findTypedContestByID[T](contestId: BSONObjectID, fields: Seq[String])(implicit reader: BSONDocumentReader[T], ec: ExecutionContext): Future[Option[T]] = {
    mc.find(BS("_id" -> contestId), fields.toBsonFields).cursor[T].headOption
  }

  def findContestByID(contestId: BSONObjectID, fields: Seq[String])(implicit ec: ExecutionContext): Future[Option[Contest]] = {
    mc.find(BS("_id" -> contestId), fields.toBsonFields).cursor[Contest].headOption
  }

  def findContests(searchOptions: SearchOptions, fields: Seq[String])(implicit ec: ExecutionContext): Future[Seq[Contest]] = {
    mc.find(createQuery(searchOptions), fields.toBsonFields).cursor[Contest].collect[Seq]()
  }

  def findContestsByPlayerName(playerName: String)(implicit ec: ExecutionContext): Future[Seq[Contest]] = {
    mc.find(BS("participants.name" -> playerName, "status" -> ContestStatuses.ACTIVE)).cursor[Contest].collect[Seq]()
  }

  def findContestsByPlayerID(playerId: BSONObjectID)(implicit ec: ExecutionContext): Future[Seq[Contest]] = {
    mc.find(BS("participants._id" -> playerId, "status" -> ContestStatuses.ACTIVE)).cursor[Contest].collect[Seq]()
  }

  def joinContest(contestId: BSONObjectID, participant: Participant)(implicit ec: ExecutionContext): Future[Option[Contest]] = {
    db.command(FindAndModify(
      collection = "Contests",
      query = BS("_id" -> contestId, "playerCount" -> BS("$lt" -> Contest.MaxPlayers) /*, "invitationOnly" -> false*/),
      modify = new Update(
        BS("$inc" -> BS("playerCount" -> 1),
          "$addToSet" -> BS("participants" -> participant)), fetchNewObject = true),
      upsert = false)) map (_ flatMap (_.seeAsOpt[Contest]))
  }

  def quitContest(contestId: BSONObjectID, playerId: BSONObjectID)(implicit ec: ExecutionContext): Future[Option[Contest]] = {
    db.command(FindAndModify(
      collection = "Contests",
      query = BS("_id" -> contestId),
      modify = new Update(
        BS("$inc" -> BS("playerCount" -> -1),
          "$pull" -> BS("participants" -> BS("_id" -> playerId))), fetchNewObject = true),
      upsert = false)) map (_ flatMap (_.seeAsOpt[Contest]))
  }

  def startContest(contestId: BSONObjectID, startTime: Date)(implicit ec: ExecutionContext): Future[Option[Contest]] = {
    db.command(FindAndModify(
      collection = "Contests",
      query = BS("_id" -> contestId, "startTime" -> BS("$exists" -> false)),
      modify = new Update(BS("$set" -> BS("startTime" -> startTime)), fetchNewObject = true),
      fields = None,
      upsert = false)) map (_ flatMap (_.seeAsOpt[Contest]))
  }

  private def createQuery(so: SearchOptions) = {
    var q = BS()
    so.activeOnly.foreach { isSet =>
      if (isSet) q = q ++ BS("status" -> ContestStatuses.ACTIVE)
    }
    so.available.foreach { isSet =>
      if (isSet) q = q ++ BS("playerCount" -> BS("$lt" -> Contest.MaxPlayers))
    }
    so.levelCap.foreach { lc =>
      val levelCap = Try(lc.toInt).toOption.getOrElse(0)
      q = q ++ BS("levelCap" -> BS("$gte" -> levelCap))
    }
    so.perksAllowed.foreach { isSet =>
      if (isSet) q = q ++ BS("perksAllowed" -> true)
    }
    so.robotsAllowed.foreach { isSet =>
      if (isSet) q = q ++ BS("robotsAllowed" -> true)
    }
    q
  }

  /////////////////////////////////////////////////////////////////////////////////
  //        Margin Account
  /////////////////////////////////////////////////////////////////////////////////

  def updateMarginAccount(contestId: BSONObjectID, playerId: BSONObjectID, account: MarginAccount)(implicit ec: ExecutionContext): Future[Option[Contest]] = {
    db.command(FindAndModify(
      collection = "Contests",
      query = BS("_id" -> contestId, "participants._id" -> playerId),
      modify = new Update(BS("$set" -> BS("participants.$.marginAccount" -> account)), fetchNewObject = true),
      upsert = false)) map (_ flatMap (_.seeAsOpt[Contest]))
  }

  def adjustMarginAccountFunds(contestId: BSONObjectID, playerId: BSONObjectID, deltaAmount: Double)(implicit ec: ExecutionContext): Future[Option[Contest]] = {
    db.command(FindAndModify(
      collection = "Contests",
      query = BS("_id" -> contestId, "participants._id" -> playerId),
      modify = new Update(BS(
        "$inc" -> BS("participants.$.fundsAvailable" -> -deltaAmount),
        "$inc" -> BS("participants.$.marginAccount.depositedFunds" -> deltaAmount)), fetchNewObject = true),
      upsert = false)) map (_ flatMap (_.seeAsOpt[Contest]))
  }

  /////////////////////////////////////////////////////////////////////////////////
  //        Messages
  /////////////////////////////////////////////////////////////////////////////////

  def createMessage(contestId: BSONObjectID, message: Message)(implicit ec: ExecutionContext): Future[Option[Contest]] = {
    db.command(FindAndModify(
      collection = "Contests",
      query = BS("_id" -> contestId),
      modify = new Update(BS("$addToSet" -> BS("messages" -> message)), fetchNewObject = true), upsert = false))
      .map(_ flatMap (_.seeAsOpt[Contest]))
  }

  /////////////////////////////////////////////////////////////////////////////////
  //        Orders
  /////////////////////////////////////////////////////////////////////////////////

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

  def closeOrder(contestId: BSONObjectID, playerId: BSONObjectID, orderId: BSONObjectID)(implicit ec: ExecutionContext): Future[Option[Contest]] = {
    (for {
      order <- ContestDAO.findOrderByID(contestId, orderId) map (_ orDie s"Order not found")
      contest_? <- db.command(FindAndModify(
        collection = "Contests",
        query = BS("_id" -> contestId, "participants._id" -> playerId),
        modify = new Update(BS(
          "$pull" -> BS("participants.$.orders" -> BS("_id" -> orderId)),
          "$addToSet" -> BS("participants.$.orderHistory" -> order)),
          fetchNewObject = true),
        upsert = false))
    } yield contest_?) map (_ flatMap (_.seeAsOpt[Contest]))
  }

  def createOrder(contestId: BSONObjectID, playerId: BSONObjectID, order: Order)(implicit ec: ExecutionContext): Future[Option[Contest]] = {
    db.command(FindAndModify(
      collection = "Contests",
      query = BS("_id" -> contestId, "participants._id" -> playerId),
      modify = new Update(BS("$addToSet" -> BS("participants.$.orders" -> order)), fetchNewObject = true),
      upsert = false)) map (_ flatMap (_.seeAsOpt[Contest]))
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

  def findOrderByID(contestId: BSONObjectID, orderId: BSONObjectID)(implicit ec: ExecutionContext): Future[Option[Order]] = {
    mc.find(BS("_id" -> contestId, "participants.orders" -> BS("$elemMatch" -> BS("_id" -> orderId))))
      .cursor[Contest].headOption map (_ flatMap (_.participants.flatMap(_.orders.find(_.id == orderId)).headOption))
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

  /////////////////////////////////////////////////////////////////////////////////
  //        Perks
  /////////////////////////////////////////////////////////////////////////////////

  /**
   * Retrieves all of the system-defined perks
   * @return a promise of a sequence of perks
   */
  def findAvailablePerks(contestId: BSONObjectID)(implicit ec: ExecutionContext): Future[Seq[Perk]] = {
    mc.find(BS("_id" -> contestId)).cursor[Contest].headOption map {
      case None => Nil
      case Some(contest) =>
        val startingBalance = contest.startingBalance.toDouble
        Seq(
          Perk(
            code = PerkTypes.FEEWAIVR,
            name = "Fee Waiver",
            cost = 100.00,
            description = "Reduces the commissions the player pays for buying or selling securities"
          ),
          Perk(
            code = PerkTypes.PRFCTIMG,
            name = "Perfect Timing",
            cost = startingBalance * 0.004d,
            description = "Gives the player the ability to create BUY orders for more than cash currently available"
          ),
          Perk(
            code = PerkTypes.PRCHEMNT,
            name = "Purchase Eminent",
            cost = startingBalance * 0.004d,
            description = "Gives the player the ability to create SELL orders for securities not yet owned"
          ),
          Perk(
            code = PerkTypes.MARGIN,
            name = "Rational People think at the Margin",
            cost = startingBalance * 0.01d,
            description = "Gives the player the ability to use margin accounts"
          ))
    }
  }

  /**
   * Purchases the passed perks
   * @param contestId the [[BSONObjectID contest ID]] which represents the contest
   * @param playerId the [[BSONObjectID player ID]] which represents the player whom is purchasing the perks
   * @param perkCodes the given perk codes
   * @param totalCost the total cost of the perks
   * @return a promise of an option of a contest
   */
  def purchasePerks(contestId: BSONObjectID, playerId: BSONObjectID, perkCodes: Seq[PerkType], totalCost: Double)(implicit ec: ExecutionContext) = {
    val q = BS("_id" -> contestId, "participants" -> BS("$elemMatch" -> BS("_id" -> playerId, "fundsAvailable" -> BS("$gte" -> totalCost))))
    val u = BS("$addToSet" -> BS("participants.$.perks" -> BS("$each" -> perkCodes)), "$inc" -> BS("participants.$.fundsAvailable" -> -totalCost))
    db.command(FindAndModify("Contests", q, Update(u, fetchNewObject = true), upsert = false, sort = None)) map (_ flatMap (_.seeAsOpt[Contest]))
  }

  /////////////////////////////////////////////////////////////////////////////////
  //        Positions
  /////////////////////////////////////////////////////////////////////////////////

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
      case None => increasePositionCreate2(c, claim, asOfDate) map (_ => 1)
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

    // attempt to update the matching the record
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

  private def increasePositionCreate2(c: Contest, claim: Claim, asOfDate: Date)(implicit ec: ExecutionContext) = {
    val wo = claim.workOrder

    // attempt to update the matching the record
    performUpdate(
      // find the matching the record
      BS("_id" -> c.id) -> "Contest not found",
      BS("participants" -> BS("$elemMatch" -> BS("_id" -> wo.playerId, "fundsAvailable" -> BS("$gte" -> claim.cost)))) -> "Insufficient funds") {

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
      )
    } andThen {
      case Success(outcome) => outcome
      case Failure(e) =>
        failOrder(c, wo, e.getMessage, asOfDate)
        throw new IllegalStateException(e)
    }
  }

  private def performUpdate(comps: (BS, String)*)(update: BS)(implicit ec: ExecutionContext): Future[Seq[Int]] = {
    // build the query document
    val query = comps.foldLeft[BS](BS()) { case (obj, (e, msg)) => obj ++ e }

    // attempt the update
    for {
      result <- mc.update(query, update, upsert = false, multi = false)
      outcome <- if (result.n == 0) findFailedMatch(comps) else Future.successful(Seq(result.n))
    } yield outcome
  }

  private def findFailedMatch(queryComps: Seq[(BS, String)])(implicit ec: ExecutionContext): Future[Seq[Int]] = {
    Future.sequence {
      queryComps.map { case (comp, message) =>
        mc.find(comp, BS("_id" -> 1)).cursor[BS].headOption map {
          case Some(rec) => 1
          case None =>
            throw new IllegalArgumentException(message)
        }
      }
    }
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
            "$pull" -> BS(
              "participants.$.orders" -> BS("_id" -> wo.id),
              "participants.$.positions" -> BS("_id" -> existingPos.id)),

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
