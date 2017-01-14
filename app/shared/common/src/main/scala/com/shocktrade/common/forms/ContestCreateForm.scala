package com.shocktrade.common.forms

import com.shocktrade.common.forms.ContestCreateForm.{GameBalance, GameDuration}
import com.shocktrade.common.util.StringHelper._
import io.scalajs.util.ScalaJsHelper._
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Contest Creation Form
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class ContestCreateForm(var name: js.UndefOr[String] = js.undefined,
                        var playerId: js.UndefOr[String] = js.undefined,
                        var playerName: js.UndefOr[String] = js.undefined,
                        var facebookId: js.UndefOr[String] = js.undefined,
                        var startingBalance: js.UndefOr[GameBalance] = js.undefined,
                        var startAutomatically: js.UndefOr[Boolean] = js.undefined,
                        var duration: js.UndefOr[GameDuration] = js.undefined,
                        var friendsOnly: js.UndefOr[Boolean] = js.undefined,
                        var invitationOnly: js.UndefOr[Boolean] = js.undefined,
                        var levelCapAllowed: js.UndefOr[Boolean] = js.undefined,
                        var levelCap: js.UndefOr[String] = js.undefined,
                        var perksAllowed: js.UndefOr[Boolean] = js.undefined,
                        var robotsAllowed: js.UndefOr[Boolean] = js.undefined) extends js.Object

/**
  * Contest Creation Form
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ContestCreateForm {

  @ScalaJSDefined
  class GameBalance(val label: String, val value: Double) extends js.Object

  /**
    * Game Duration
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  @ScalaJSDefined
  class GameDuration(var label: String, var value: Int) extends js.Object

  /**
    * Contest Creation Extensions
    * @param form the given [[ContestCreateForm form]]
    */
  implicit class ContestCreationExtensions(val form: ContestCreateForm) extends AnyVal {

    @inline
    def validate: js.Array[String] = {
      val messages = emptyArray[String]
      if (!form.name.exists(_.nonBlank)) messages.push("The game name is required")
      if (!form.playerId.exists(_.nonBlank) || !form.playerName.exists(_.nonBlank)) messages.push("The creator information is missing")
      if (form.levelCapAllowed.isTrue && form.levelCap.isEmpty) messages.push("Level cap must be specified")
      if (form.duration.flat.isEmpty) messages.push("The game duration is required")
      messages
    }

  }

}