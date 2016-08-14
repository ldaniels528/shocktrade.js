package com.shocktrade.javascript.models.contest

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Participant Model
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Participant(var _id: js.UndefOr[String],
                  var name: js.UndefOr[String],
                  var facebookID: js.UndefOr[String],
                  var cashAccount: js.UndefOr[CashAccount],
                  var marginAccount: js.UndefOr[MarginAccount],
                  var orders: js.UndefOr[js.Array[Order]],
                  var closedOrders: js.UndefOr[js.Array[ClosedOrder]],
                  var performance: js.UndefOr[js.Array[Performance]],
                  var perks: js.UndefOr[js.Array[String]],
                  var positions: js.UndefOr[js.Array[Position]]) extends js.Object

/**
  * Participant Companion
  * @author lawrence.daniels@gmail.com
  */
object Participant {

  /**
    * Participant Enrichment
    * @param participant the given [[Participant participant]]
    */
  implicit class ContestEnrichment(val participant: Participant) extends AnyVal {

    @inline
    def findPosition(p: Position => Boolean) = for {
      positions <- participant.positions.toOption
      position <- positions.find(p)
    } yield position

  }

}