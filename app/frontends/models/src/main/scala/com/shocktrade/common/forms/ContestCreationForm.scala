package com.shocktrade.common.forms

import com.shocktrade.common.forms.ContestCreationForm.{GameBalance, GameDuration, LevelCap}
import com.shocktrade.common.util.StringHelper._
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.ScalaJsHelper._

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

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

  val GameDurations: js.Array[GameDuration] = js.Array(
    new GameDuration(label = "3 Days", value = 3),
    new GameDuration(label = "1 Week", value = 7),
    new GameDuration(label = "2 Weeks", value = 14),
    new GameDuration(label = "3 Weeks", value = 21),
    new GameDuration(label = "4 Weeks", value = 28),
    new GameDuration(label = "5 Weeks", value = 35),
    new GameDuration(label = "6 Weeks", value = 42),
    new GameDuration(label = "7 Weeks", value = 49),
    new GameDuration(label = "8 Weeks", value = 56))

  val LevelCaps: js.Array[LevelCap] = (1 to 25) map { n => new LevelCap(label = s"Level $n", value = n) } toJSArray

  val StartingBalances: js.Array[GameBalance] = js.Array(
    new GameBalance(label = "$ 1,000", value = 1000.00),
    new GameBalance(label = "$ 2,500", value = 2500.00),
    new GameBalance(label = "$ 5,000", value = 5000.00),
    new GameBalance(label = "$10,000", value = 10000.00),
    new GameBalance(label = "$25,000", value = 25000.00),
    new GameBalance(label = "$50,000", value = 50000.00),
    new GameBalance(label = "$75,000", value = 75000.00),
    new GameBalance(label = "$100,000", value = 100000.00))

  /**
   * Game Balance
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  class GameBalance(val label: String, val value: Double) extends js.Object

  /**
   * Game Duration
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  class GameDuration(val label: String, val value: Int) extends js.Object

  /**
   * Level Cap
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  class LevelCap(val label: String, val value: Int) extends js.Object

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
      if (form.startingBalance.flat.isEmpty) messages.push("The starting balance is required")
      messages
    }

    def toRequest: ContestCreationRequest = new ContestCreationRequest(
      name = form.name,
      userID = form.userID,
      contestID = js.undefined,
      startingBalance = form.startingBalance.map(_.value),
      startAutomatically = form.startAutomatically,
      duration = form.duration.map(_.value),
      friendsOnly = form.friendsOnly,
      invitationOnly = form.invitationOnly,
      levelCapAllowed = form.levelCapAllowed,
      levelCap = form.levelCap.map(_.value),
      perksAllowed = form.perksAllowed,
      robotsAllowed = form.robotsAllowed
    )

  }

}