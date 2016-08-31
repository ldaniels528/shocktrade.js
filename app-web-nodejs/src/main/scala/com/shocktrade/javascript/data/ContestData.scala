package com.shocktrade.javascript.data

import org.scalajs.nodejs.util.ScalaJsHelper._
import com.shocktrade.javascript.forms.ContestCreateForm
import com.shocktrade.javascript.models.contest.{ContestLike, Participant}
import org.scalajs.nodejs.mongodb.ObjectID

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Contest Data Model
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class ContestData(var _id: js.UndefOr[ObjectID] = js.undefined,
                  var name: js.UndefOr[String],
                  var creator: js.UndefOr[String],
                  var startTime: js.UndefOr[js.Date],
                  var status: js.UndefOr[String],
                  var participants: js.UndefOr[js.Array[Participant]],

                  // indicators
                  var friendsOnly: js.UndefOr[Boolean],
                  var invitationOnly: js.UndefOr[Boolean],
                  var levelCap: js.UndefOr[String],
                  var perksAllowed: js.UndefOr[Boolean],
                  var robotsAllowed: js.UndefOr[Boolean]) extends ContestLike

/**
  * Contest Data Model Companion
  * @author lawrence.daniels@gmail.com
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
      creator = form.playerId,
      startTime = new js.Date(), // form.duration.
      status = "ACTIVE",
      participants = js.Array[Participant](),
      friendsOnly = form.friendsOnly ?? false,
      invitationOnly = form.invitationOnly ?? false,
      levelCap = form.levelCap,
      perksAllowed = form.perksAllowed ?? false,
      robotsAllowed = form.robotsAllowed ?? false
    )

  }

}