package com.shocktrade.server.tqm.data

import com.shocktrade.javascript.models.contest._
import org.scalajs.nodejs.mongodb.ObjectID

import scala.scalajs.js

/**
  * Contest Data Model for TQM
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ContestData extends js.Object with ContestLike {
  var _id: js.UndefOr[ObjectID] = js.native
  var name: js.UndefOr[String] = js.native
  var creator: js.UndefOr[String] = js.native
  var startTime: js.UndefOr[js.Date] = js.native
  var status: js.UndefOr[String] = js.native
  var participants: js.UndefOr[js.Array[Participant]] = js.native

  // indicators
  var friendsOnly: js.UndefOr[Boolean] = js.native
  var invitationOnly: js.UndefOr[Boolean] = js.native
  var levelCap: js.UndefOr[String] = js.native
  var perksAllowed: js.UndefOr[Boolean] = js.native
  var robotsAllowed: js.UndefOr[Boolean] = js.native

  // administrative fields
  var lastUpdate: js.UndefOr[Double] = js.native
  var nextUpdate: js.UndefOr[Double] = js.native

}

/**
  * Contest Data Companion
  * @author lawrence.daniels@gmail.com
  */
object ContestData {

  /**
    * Contest Enrichment
    * @param contest the given [[ContestData contest]]
    */
  implicit class ContestEnrichment(val contest: ContestData) extends AnyVal {

    @inline
    def findEligibleOrders(asOfTime: Double = js.Date.now()) = for {
      participants <- contest.participants.toOption.map(_.toSeq).toList
      participant <- participants
      orders <- participant.orders.toOption.map(_.toSeq).toList
      order <- orders.filter(!_.isExpired(asOfTime))
    } yield (participant, order)

    def findParticipant(p: Participant => Boolean) = for {
      participants <- contest.participants.toOption
      participant <- participants.find(p)
    } yield participant

  }

}
