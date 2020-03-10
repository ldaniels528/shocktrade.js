package com.shocktrade.common.forms

import scala.scalajs.js

/**
 * Represents a Facebook friend form
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class FacebookFriendForm(val id: js.UndefOr[String], val name: js.UndefOr[String]) extends js.Object

/**
 * Facebook Friend Form
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object FacebookFriendForm {

  implicit class FacebookFriendFormEnrichment(val form: FacebookFriendForm) extends AnyVal {

    @inline
    def values: Option[(String, String)] = {
      (for (id <- form.id; name <- form.name) yield (id, name)).toOption
    }

  }

}