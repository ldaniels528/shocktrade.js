package com.shocktrade.javascript.forms

import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Contest Search Form
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class ContestSearchForm(var activeOnly: js.UndefOr[Boolean],
                        var available: js.UndefOr[Boolean],
                        var friendsOnly: js.UndefOr[Boolean],
                        var invitationOnly: js.UndefOr[Boolean],
                        var levelCap: js.UndefOr[String],
                        var levelCapAllowed: js.UndefOr[Boolean],
                        var perksAllowed: js.UndefOr[Boolean],
                        var robotsAllowed: js.UndefOr[Boolean]) extends js.Object


/**
  * Contest Search Form Companion
  * @author lawrence.daniels@gmail.com
  */
object ContestSearchForm {

  /**
    * Contest Search Validations
    * @param form the given [[ContestSearchForm form]]
    */
  implicit class ContestSearchValidations(val form: ContestSearchForm) extends AnyVal {

    def validate: js.Array[String] = {
      val messages = emptyArray[String]
      if (form.levelCapAllowed.isTrue && form.levelCap.isEmpty) messages.push("Level cap must be specified")
      messages
    }

  }

}