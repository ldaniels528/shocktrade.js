package com.shocktrade.common.forms

import com.shocktrade.util.StringHelper._
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Contest Creation Form
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class ContestCreateForm(val name: js.UndefOr[String],
                        val playerId: js.UndefOr[String],
                        val playerName: js.UndefOr[String],
                        val facebookId: js.UndefOr[String],
                        val startingBalance: js.UndefOr[Double],
                        val startAutomatically: js.UndefOr[Boolean],
                        val duration: js.UndefOr[Int],
                        val friendsOnly: js.UndefOr[Boolean],
                        val invitationOnly: js.UndefOr[Boolean],
                        val levelCapAllowed: js.UndefOr[Boolean],
                        val levelCap: js.UndefOr[String],
                        val perksAllowed: js.UndefOr[Boolean],
                        val robotsAllowed: js.UndefOr[Boolean]) extends js.Object

/**
  * Contest Creation Form
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ContestCreateForm {

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