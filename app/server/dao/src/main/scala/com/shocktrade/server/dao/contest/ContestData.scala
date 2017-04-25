package com.shocktrade.server.dao.contest

import com.shocktrade.common.forms.ContestCreateForm
import com.shocktrade.common.models.contest.{ChatMessage, ContestLike, Participant}
import com.shocktrade.common.models.user.User
import io.scalajs.npm.mongodb.ObjectID
import io.scalajs.util.DateHelper._
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.duration._
import scala.scalajs.js

/**
  * Contest Data Model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ContestData(var _id: js.UndefOr[ObjectID],
                  var name: js.UndefOr[String],
                  var creator: js.UndefOr[User],
                  var startTime: js.UndefOr[js.Date],
                  var expirationTime: js.UndefOr[js.Date],
                  var startingBalance: js.UndefOr[Double],
                  var status: js.UndefOr[String],
                  var messages: js.UndefOr[js.Array[ChatMessage]],
                  // participants & rankings
                  var participants: js.UndefOr[js.Array[Participant]],
                  // indicators
                  var friendsOnly: js.UndefOr[Boolean],
                  var invitationOnly: js.UndefOr[Boolean],
                  var levelCap: js.UndefOr[String],
                  var perksAllowed: js.UndefOr[Boolean],
                  var robotsAllowed: js.UndefOr[Boolean]) extends ContestLike

/**
  * Contest Data Model Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ContestData {

  /**
    * Contest Creation Extensions
    * @param form the given [[ContestCreateForm form]]
    */
  implicit class ContestCreation(val form: ContestCreateForm) extends AnyVal {

    @inline
    def toContest = new ContestData(
      _id = js.undefined,
      name = form.name,
      creator = User(_id = form.playerId, facebookID = form.facebookId, name = form.playerName),
      startingBalance = form.startingBalance.map(_.value),
      startTime = new js.Date(),
      expirationTime = form.duration.map(_.value.days + new js.Date()),
      status = ContestLike.StatusActive,
      participants = js.Array(new Participant(_id = form.playerId, facebookID = form.facebookId, name = form.playerName)),
      messages = js.Array(
        new ChatMessage(
          sender = User(_id = form.playerId, facebookID = form.facebookId, name = form.playerName),
          text = s"Welcome to ${form.name}"
        )),
      friendsOnly = form.friendsOnly ?? false,
      invitationOnly = form.invitationOnly ?? false,
      levelCap = form.levelCap,
      perksAllowed = form.perksAllowed ?? false,
      robotsAllowed = form.robotsAllowed ?? false
    )
  }

}