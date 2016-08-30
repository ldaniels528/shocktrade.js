package com.shocktrade.server.data

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

}

