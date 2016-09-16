package com.shocktrade.common.forms

import com.shocktrade.common.forms.NewGameForm.GameDuration
import com.shocktrade.common.models.PlayerRef

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * New Game Form
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class NewGameForm(var name: js.UndefOr[String] = js.undefined,
                  var duration: js.UndefOr[GameDuration] = js.undefined,
                  var friendsOnly: js.UndefOr[Boolean] = js.undefined,
                  var perksAllowed: js.UndefOr[Boolean] = js.undefined,
                  var player: js.UndefOr[PlayerRef] = js.undefined,
                  var robotsAllowed: js.UndefOr[Boolean] = js.undefined,
                  var startAutomatically: js.UndefOr[Boolean] = js.undefined,
                  var startingBalance: js.UndefOr[Int] = js.undefined) extends js.Object

/**
  * New Game Form Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object NewGameForm {

  /**
    * Game Duration
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  @ScalaJSDefined
  class GameDuration(var label: String, var value: Int) extends js.Object

}