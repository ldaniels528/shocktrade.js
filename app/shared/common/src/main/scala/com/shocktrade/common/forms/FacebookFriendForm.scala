package com.shocktrade.common.forms

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Facebook friend form
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class FacebookFriendForm(val id: js.UndefOr[String], val name: js.UndefOr[String]) extends js.Object

/**
  * Facebook Friend Form
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object FacebookFriendForm {

  implicit class FacebookFriendFormEnrichment(val form: FacebookFriendForm) extends AnyVal {

    @inline
    def values = {
      (for (id <- form.id; name <- form.name) yield (id, name)).toOption
    }

  }

}