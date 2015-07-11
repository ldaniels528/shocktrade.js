package com.shocktrade.javascript.models

import java.util.Date

import scala.scalajs.js

/**
 * Represents a Contest
 * @author lawrence.daniels@gmail.com
 */
trait Contest extends js.Object {
  var _id: js.Dynamic = js.native
  var name: String = js.native
  var creator: ContestCreator = js.native
  var startTime: js.Date = js.native
  var status: String = js.native
  var rankings: js.Dynamic = js.native
  var messages: js.Array[Message] = js.native
  var participants: js.Array[Participant] = js.native
  var invitationOnly: Boolean = js.native
}

/**
 * Contest Singleton
 */
object Contest {
  val MaxPlayers = 24

}

trait ContestCreator extends js.Object {
  var _id: js.Dynamic = js.native
  var name: String = js.native
}

trait Ranking extends js.Object {
  var _id: js.Dynamic = js.native
  var facebookID: String = js.native
  var name: String = js.native
  var rank: String = js.native
  var totalEquity: Double = js.native
  var gainLoss: Double = js.native
}

trait Rankings extends js.Object {
  var participants: js.Array[js.Dynamic] = js.native
  var leader: js.Dynamic = js.native
  var player: js.Dynamic = js.native
}

trait Message extends js.Object {
  var _id: js.Dynamic = js.native
  var sender: PlayerRef = js.native
  var text: String = js.native
  var recipient: js.UndefOr[PlayerRef] = js.native
  var sentTime: js.Date = js.native
}

trait PlayerRef extends js.Object {
  var _id: js.Dynamic = js.native
  var name: String = js.native
  var facebookID: String = js.native
}

trait Participant extends js.Object {
  var _id: js.Dynamic = js.native
  var name: String = js.native
  var facebookID: String = js.native
  var cashAccount: CashAccount = js.native
  var marginAccount: js.UndefOr[MarginAccount] = js.native
  var orders: js.Array[Order] = js.native
  var closedOrders: js.Array[ClosedOrder] = js.native
  var performance: js.Array[Performance] = js.native
  var perks: js.Array[String] = js.native
  var positions: js.Array[Position] = js.native
}

trait CashAccount extends js.Object {
  var cashFunds: Double = js.native
  var asOfDate: js.Date = js.native
}

trait MarginAccount extends js.Object {
  var cashFunds: Double = js.native
  var borrowedFunds: Double = js.native
  var initialMargin: Double = js.native
  var interestPaid: Double = js.native
  var asOfDate: js.Date = js.native

}

trait Order extends js.Object {
  var _id: js.Dynamic = js.native
  var symbol: String = js.native
}

trait ClosedOrder extends js.Object {
  var _id: js.Dynamic = js.native
  var symbol: String = js.native
}

trait Performance extends js.Object {
  var _id: js.Dynamic = js.native
  var symbol: String = js.native
}

trait Position extends js.Object {
  var _id: js.Dynamic = js.native
  var symbol: String = js.native
  var exchange: String = js.native
  var pricePaid: BigDecimal = js.native
  var quantity: Long = js.native
  var commission: BigDecimal = js.native
  var processedTime: Date = js.native
  var accountType: String = js.native
}
