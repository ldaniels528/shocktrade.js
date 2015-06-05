package com.shocktrade.javascript.app.model

import com.shocktrade.javascript.app.model.Contest._

/**
 * Contest
 * @author lawrence.daniels@gmail.com
 */
case class Contest(id: String,
                   name: String,
                   `type`: String,
                   status: String,
                   error: Option[String],
                   var messages: List[Message] = Nil,
                   var participants: List[Participant] = Nil)

object Contest {


  case class ClosedOrder()

  case class Message()

  case class Order()

  case class Participant(id: String,
                         name: String,
                         var closedOrders: Option[List[ClosedOrder]],
                         var orders: Option[List[Order]],
                         var performance: Option[List[Performance]],
                         var perks: Option[List[String]],
                         var positions: Option[List[Position]],
                         var cashAccount: Option[CashAccount],
                         var marginAccount: Option[MarginAccount])

  case class Performance()

  case class Position()

  case class CashAccount(var cashFunds: Double)

  case class MarginAccount(var cashFunds: Double)

}