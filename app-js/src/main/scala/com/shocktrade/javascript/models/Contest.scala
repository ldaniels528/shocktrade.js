package com.shocktrade.javascript.models

import com.github.ldaniels528.meansjs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.UndefOr

/**
  * Contest Model
  */
@js.native
trait Contest extends js.Object {
  var _id: js.UndefOr[BSONObjectID]
  var name: String
  var creator: ContestCreator
  var startTime: UndefOr[js.Date]
  var status: String
  var rankings: UndefOr[Rankings]
  var messages: js.Array[Message]
  var participants: js.Array[Participant]
  var invitationOnly: Boolean

  // administrative fields
  var error: UndefOr[String]
  var rankingsHidden: UndefOr[Boolean]
  var deleting: Boolean
  var joining: Boolean
  var quitting: Boolean
  var starting: Boolean
}

/**
  * Contest Model Singleton
  */
object Contest {
  val MaxPlayers = 24

}

@js.native
trait ContestCreator extends js.Object {
  var _id: js.UndefOr[BSONObjectID]
  var name: String
}

@js.native
trait ParticipantRanking extends js.Object {
  var _id: js.UndefOr[BSONObjectID]
  var facebookID: String
  var name: String
  var rank: String
  var totalEquity: Double
  var gainLoss: Double
}

/**
  * Contest Participant Rankings
  */
@js.native
trait Rankings extends js.Object {
  var participants: js.Array[ParticipantRanking]
  var leader: UndefOr[ParticipantRanking]
  var player: UndefOr[ParticipantRanking]
}

/**
  * Contest Participant Rankings Singleton
  */
object Rankings {

  def apply(participants: js.Array[ParticipantRanking] = emptyArray,
            leader: UndefOr[ParticipantRanking] = js.undefined,
            player: UndefOr[ParticipantRanking] = js.undefined) = {
    val rankings = New[Rankings]
    rankings.participants = participants
    rankings.leader = leader
    rankings.player = player
    rankings
  }
}

@js.native
trait Message extends js.Object {
  var _id: js.UndefOr[BSONObjectID]
  var sender: PlayerRef
  var text: String
  var recipient: UndefOr[PlayerRef]
  var sentTime: js.Date
}

@js.native
trait PlayerRef extends js.Object {
  var _id: js.UndefOr[BSONObjectID]
  var name: String
  var facebookID: String
}

object PlayerRef {

  def apply(userId: BSONObjectID, facebookID: String, name: String) = {
    val ref = New[PlayerRef]
    ref._id = userId
    ref.facebookID = facebookID
    ref.name = name
    ref
  }
}

@js.native
trait Participant extends js.Object {
  var _id: js.UndefOr[BSONObjectID]
  var name: String
  var facebookID: String
  var cashAccount: CashAccount
  var marginAccount: UndefOr[MarginAccount]
  var orders: js.Array[Order]
  var closedOrders: js.Array[ClosedOrder]
  var performance: js.Array[Performance]
  var perks: js.Array[String]
  var positions: js.Array[Position]
}

@js.native
trait CashAccount extends js.Object {
  var cashFunds: Double
  var asOfDate: js.Date
}

@js.native
trait MarginAccount extends js.Object {
  var cashFunds: Double
  var borrowedFunds: Double
  var initialMargin: Double
  var interestPaid: Double
  var asOfDate: js.Date
}

@js.native
trait Position extends js.Object {
  var _id: js.UndefOr[BSONObjectID]
  var symbol: String
  var exchange: String
  var pricePaid: BigDecimal
  var quantity: Long
  var commission: BigDecimal
  var processedTime: js.Date
  var accountType: String
  var netValue: Double
}
