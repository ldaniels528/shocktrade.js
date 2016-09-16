package com.shocktrade.common.models

import com.shocktrade.common.models.ProfileLike.QuoteFilter

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Profile model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class Profile(var _id: js.UndefOr[String] = js.undefined,
              var facebookID: js.UndefOr[String] = js.undefined,
              var name: js.UndefOr[String] = js.undefined,
              var country: js.UndefOr[String] = js.undefined,
              var level: js.UndefOr[Int] = js.undefined,
              var rep: js.UndefOr[Int] = js.undefined,
              var netWorth: js.UndefOr[Double] = js.undefined,
              var totalXP: js.UndefOr[Int] = js.undefined,
              var awards: js.UndefOr[js.Array[String]] = js.undefined,
              var favoriteSymbols: js.UndefOr[js.Array[String]] = js.undefined,
              var recentSymbols: js.UndefOr[js.Array[String]] = js.undefined,
              var filters: js.UndefOr[js.Array[QuoteFilter]] = js.undefined,
              var friends: js.UndefOr[js.Array[String]] = js.undefined,
              var accomplishments: js.UndefOr[js.Array[String]] = js.undefined,
              var acquaintances: js.UndefOr[js.Array[String]] = js.undefined,
              var isAdmin: js.UndefOr[Boolean] = js.undefined,
              var lastSymbol: js.UndefOr[String] = js.undefined,
              var lastLoginTime: js.UndefOr[js.Date] = js.undefined) extends ProfileLike

/**
  * Profile Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object Profile {

  /**
    * Profile Enrichment
    * @param profile the given [[Profile profile]]
    */
  implicit class ProfileEnrichment(val profile: Profile) extends AnyVal {

    @inline
    def nextLevelXP = profile.totalXP.map(_ + 1000)

  }

}