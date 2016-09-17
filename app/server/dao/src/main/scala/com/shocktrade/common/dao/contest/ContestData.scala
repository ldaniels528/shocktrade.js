package com.shocktrade.common.dao.contest

import com.shocktrade.common.forms.ContestCreateForm
import com.shocktrade.common.models.PlayerRef
import com.shocktrade.common.models.contest.{ChatMessage, ContestLike, Participant}
import org.scalajs.nodejs.mongodb.ObjectID
import org.scalajs.sjs.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Contest Data Model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class ContestData(var _id: js.UndefOr[ObjectID] = js.undefined,
                  var name: js.UndefOr[String] = js.undefined,
                  var creator: js.UndefOr[PlayerRef] = js.undefined,
                  var startTime: js.UndefOr[js.Date] = js.undefined,
                  var startingBalance: js.UndefOr[Double] = js.undefined,
                  var status: js.UndefOr[String] = js.undefined,

                  // collections
                  var participants: js.UndefOr[js.Array[Participant]] = js.undefined,
                  var messages: js.UndefOr[js.Array[ChatMessage]] = js.undefined,

                  // indicators
                  var friendsOnly: js.UndefOr[Boolean] = js.undefined,
                  var invitationOnly: js.UndefOr[Boolean] = js.undefined,
                  var levelCap: js.UndefOr[String] = js.undefined,
                  var perksAllowed: js.UndefOr[Boolean] = js.undefined,
                  var robotsAllowed: js.UndefOr[Boolean] = js.undefined) extends ContestLike

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
      name = form.name,
      creator = new PlayerRef(_id = form.playerId, facebookID = form.facebookId, name = form.name),
      startTime = new js.Date(), // form.duration.
      status = ContestLike.StatusActive,
      participants = js.Array[Participant](),
      messages = js.Array[ChatMessage](),
      friendsOnly = form.friendsOnly ?? false,
      invitationOnly = form.invitationOnly ?? false,
      levelCap = form.levelCap,
      perksAllowed = form.perksAllowed ?? false,
      robotsAllowed = form.robotsAllowed ?? false
    )

  }

}