package com.shocktrade.common.forms

import com.shocktrade.common.forms.ContestCreationForm.{GameBalance, GameDuration}
import com.shocktrade.common.forms.ContestSearchForm.ContestStatus
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Contest Search Form
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestSearchForm(var userID: js.UndefOr[String] = js.undefined,
                        var buyIn: js.UndefOr[GameBalance] = js.undefined,
                        var continuousTrading: js.UndefOr[Boolean] = js.undefined,
                        var duration: js.UndefOr[GameDuration] = js.undefined,
                        var friendsOnly: js.UndefOr[Boolean] = js.undefined,
                        var invitationOnly: js.UndefOr[Boolean] = js.undefined,
                        var levelCap: js.UndefOr[Int] = js.undefined,
                        var levelCapAllowed: js.UndefOr[Boolean] = js.undefined,
                        var myGamesOnly: js.UndefOr[Boolean] = js.undefined,
                        var nameLike: js.UndefOr[String] = js.undefined,
                        var perksAllowed: js.UndefOr[Boolean] = js.undefined,
                        var robotsAllowed: js.UndefOr[Boolean] = js.undefined,
                        var status: js.UndefOr[ContestStatus] = js.undefined) extends js.Object


/**
 * Contest Search Form Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ContestSearchForm {

  val ACTIVE_AND_QUEUED = 1
  val ACTIVE_ONLY = 2
  val QUEUED_ONLY = 3
  val ALL = 4

  val contestStatuses: js.Array[ContestStatus] = js.Array(
    new ContestStatus(statusID = ACTIVE_AND_QUEUED, description = "Active and Queued"),
    new ContestStatus(statusID = ACTIVE_ONLY, description = "Active Only"),
    new ContestStatus(statusID = QUEUED_ONLY, description = "Queued Only"),
    new ContestStatus(statusID = ALL, description = "All")
  )

  /**
   * Represents a contest status
   * @param statusID    the status ID
   * @param description the status description
   */
  class ContestStatus(val statusID: Int, val description: String) extends js.Object

  /**
   * Contest Search Validations
   * @param form the given [[ContestSearchForm form]]
   */
  final implicit class ContestSearchValidations(val form: ContestSearchForm) extends AnyVal {

    def validate: js.Array[String] = {
      val messages = new js.Array[String]()
      if (form.levelCapAllowed.isTrue && form.levelCap.isEmpty) messages.push("Level cap must be specified")
      messages
    }

  }

}