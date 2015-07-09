package com.shocktrade.javascript.models

import scala.scalajs.js

/**
 * Represents a Facebook Profile
 * @author lawrence.daniels@gmail.com
 */
trait FacebookProfile extends js.Object {
  var id: String = js.native
  var first_name: String = js.native
  var last_name: String = js.native
  var name: String = js.native
  var gender: String = js.native
  var link: String = js.native
  var locale: String = js.native
  var updated_time: js.Date = js.native
  var timezone: Int = js.native
  var verified: Boolean = js.native

}

/**
 * Facebook Profile Singleton
 */
object FacebookProfile {

  implicit class EnrichedFacebookProfile(val profile: FacebookProfile) extends AnyVal {

    def dynamic = profile.asInstanceOf[js.Dynamic]

  }
}