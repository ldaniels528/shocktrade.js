package com.shocktrade.common.forms

import com.shocktrade.common.forms.ContestCreationForm.{GameBalance, GameDuration, LevelCap}
import com.shocktrade.common.util.StringHelper._
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.js

/**
 * Contest Creation Form
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestCreationForm(var name: js.UndefOr[String] = js.undefined,
                          var userID: js.UndefOr[String] = js.undefined,
                          var startingBalance: js.UndefOr[GameBalance] = js.undefined,
                          var startAutomatically: js.UndefOr[Boolean] = js.undefined,
                          var duration: js.UndefOr[GameDuration] = js.undefined,
                          var friendsOnly: js.UndefOr[Boolean] = js.undefined,
                          var invitationOnly: js.UndefOr[Boolean] = js.undefined,
                          var levelCapAllowed: js.UndefOr[Boolean] = js.undefined,
                          var levelCap: js.UndefOr[LevelCap] = js.undefined,
                          var perksAllowed: js.UndefOr[Boolean] = js.undefined,
                          var robotsAllowed: js.UndefOr[Boolean] = js.undefined) extends js.Object

/**
 * Contest Creation Form
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ContestCreationForm {

  /**
   * Game Balance
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  class GameBalance(val label: String, val value: Double) extends js.Object

  /**
   * Game Duration
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  class GameDuration(var label: String, var value: Int) extends js.Object

  /**
   * Level Cap
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  class LevelCap(var label: String, var value: Int) extends js.Object

  /**
   * Contest Creation Extensions
   * @param form the given [[ContestCreationForm form]]
   */
  implicit class ContestCreationExtensions(val form: ContestCreationForm) extends AnyVal {

    @inline
    def validate: js.Array[String] = {
      val messages = emptyArray[String]
      if (!form.name.exists(_.nonBlank)) messages.push("The game name is required")
      if (form.userID.isEmpty || form.userID.exists(_.isEmpty)) messages.push("The creator information is missing")
      if (form.levelCapAllowed.isTrue && form.levelCap.isEmpty) messages.push("Level cap must be specified")
      if (form.duration.flat.isEmpty) messages.push("The game duration is required")
      messages
    }

  }

}