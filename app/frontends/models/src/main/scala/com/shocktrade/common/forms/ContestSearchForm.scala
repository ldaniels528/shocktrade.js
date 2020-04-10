package com.shocktrade.common.forms

import com.shocktrade.common.forms.ContestCreationForm.{GameBalance, GameDuration}
import com.shocktrade.common.forms.ContestSearchForm.ContestStatus
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Contest Search Form
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestSearchForm(var userID: js.UndefOr[String],
                        var buyIn: js.UndefOr[GameBalance],
                        var continuousTrading: js.UndefOr[Boolean],
                        var duration: js.UndefOr[GameDuration],
                        var friendsOnly: js.UndefOr[Boolean],
                        var invitationOnly: js.UndefOr[Boolean],
                        var levelCap: js.UndefOr[Int],
                        var levelCapAllowed: js.UndefOr[Boolean],
                        var myGamesOnly: js.UndefOr[Boolean],
                        var nameLike: js.UndefOr[String],
                        var perksAllowed: js.UndefOr[Boolean],
                        var robotsAllowed: js.UndefOr[Boolean],
                        var status: js.UndefOr[ContestStatus]) extends js.Object


/**
 * Contest Search Form Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ContestSearchForm {

  val contestStatuses: js.Array[ContestStatus] = js.Array(
    new ContestStatus(statusID = 1, description = "Active and Queued"),
    new ContestStatus(statusID = 2, description = "Active Only"),
    new ContestStatus(statusID = 3, description = "Queued Only"),
    new ContestStatus(statusID = 4, description = "All")
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