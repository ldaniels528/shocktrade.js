package com.shocktrade.javascript.models

import com.github.ldaniels528.scalascript.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.UndefOr

/**
  * Contest Model
  */
@js.native
trait Contest extends js.Object {
  var _id: js.UndefOr[BSONObjectID] = js.native
  var name: String = js.native
  var creator: ContestCreator = js.native
  var startTime: UndefOr[js.Date] = js.native
  var status: String = js.native
  var rankings: UndefOr[Rankings] = js.native
  var messages: js.Array[Message] = js.native
  var participants: js.Array[Participant] = js.native
  var invitationOnly: Boolean = js.native

  // administrative fields
  var error: UndefOr[String] = js.native
  var rankingsHidden: UndefOr[Boolean] = js.native
  var deleting: Boolean = js.native
  var joining: Boolean = js.native
  var quitting: Boolean = js.native
  var starting: Boolean = js.native
}

/**
  * Contest Model Singleton
  */
object Contest {
  val MaxPlayers = 24

}

@js.native
trait ContestCreator extends js.Object {
  var _id: js.UndefOr[BSONObjectID] = js.native
  var name: String = js.native
}

@js.native
trait ParticipantRanking extends js.Object {
  var _id: js.UndefOr[BSONObjectID] = js.native
  var facebookID: String = js.native
  var name: String = js.native
  var rank: String = js.native
  var totalEquity: Double = js.native
  var gainLoss: Double = js.native
}

/**
  * Contest Participant Rankings
  */
@js.native
trait Rankings extends js.Object {
  var participants: js.Array[ParticipantRanking] = js.native
  var leader: UndefOr[ParticipantRanking] = js.native
  var player: UndefOr[ParticipantRanking] = js.native
}

/**
  * Contest Participant Rankings Singleton
  */
object Rankings {

  def apply(participants: js.Array[ParticipantRanking] = emptyArray,
            leader: UndefOr[ParticipantRanking] = js.undefined,
            player: UndefOr[ParticipantRanking] = js.undefined) = {
    val rankings = makeNew[Rankings]
    rankings.participants = participants
    rankings.leader = leader
    rankings.player = player
    rankings
  }
}

@js.native
trait Message extends js.Object {
  var _id: js.UndefOr[BSONObjectID] = js.native
  var sender: PlayerRef = js.native
  var text: String = js.native
  var recipient: UndefOr[PlayerRef] = js.native
  var sentTime: js.Date = js.native
}

@js.native
trait PlayerRef extends js.Object {
  var _id: js.UndefOr[BSONObjectID] = js.native
  var name: String = js.native
  var facebookID: String = js.native
}

object PlayerRef {

  def apply(userId: BSONObjectID, facebookID: String, name: String) = {
    val ref = makeNew[PlayerRef]
    ref._id = userId
    ref.facebookID = facebookID
    ref.name = name
    ref
  }
}

@js.native
trait Participant extends js.Object {
  var _id: js.UndefOr[BSONObjectID] = js.native
  var name: String = js.native
  var facebookID: String = js.native
  var cashAccount: CashAccount = js.native
  var marginAccount: UndefOr[MarginAccount] = js.native
  var orders: js.Array[Order] = js.native
  var closedOrders: js.Array[ClosedOrder] = js.native
  var performance: js.Array[Performance] = js.native
  var perks: js.Array[String] = js.native
  var positions: js.Array[Position] = js.native
}

@js.native
trait CashAccount extends js.Object {
  var cashFunds: Double = js.native
  var asOfDate: js.Date = js.native
}

@js.native
trait MarginAccount extends js.Object {
  var cashFunds: Double = js.native
  var borrowedFunds: Double = js.native
  var initialMargin: Double = js.native
  var interestPaid: Double = js.native
  var asOfDate: js.Date = js.native
}

@js.native
trait Order extends js.Object {
  var _id: js.UndefOr[BSONObjectID] = js.native
  var symbol: String = js.native
}

@js.native
trait ClosedOrder extends js.Object {
  var _id: js.UndefOr[BSONObjectID] = js.native
  var symbol: String = js.native
}

@js.native
trait Performance extends js.Object {
  var _id: js.UndefOr[BSONObjectID] = js.native
  var symbol: String = js.native
}

@js.native
trait Position extends js.Object {
  var _id: js.UndefOr[BSONObjectID] = js.native
  var symbol: String = js.native
  var exchange: String = js.native
  var pricePaid: BigDecimal = js.native
  var quantity: Long = js.native
  var commission: BigDecimal = js.native
  var processedTime: js.Date = js.native
  var accountType: String = js.native
}
