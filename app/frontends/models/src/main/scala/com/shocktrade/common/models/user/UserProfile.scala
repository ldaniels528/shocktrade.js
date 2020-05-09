package com.shocktrade.common.models.user

import scala.scalajs.js

/**
 * Represents a User Profile model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserProfile(val userID: js.UndefOr[String],
                  val username: js.UndefOr[String],
                  val email: js.UndefOr[String],
                  val equity: js.UndefOr[Double],
                  val wallet: js.UndefOr[Double],
                  val totalXP: js.UndefOr[Int],
                  val awards: js.UndefOr[js.Array[String]],
                  val gamesCompleted: js.UndefOr[Int],
                  val gamesCreated: js.UndefOr[Int],
                  val gamesDeleted: js.UndefOr[Int],
                  val gamesJoined: js.UndefOr[Int],
                  val gamesWithdrawn: js.UndefOr[Int],
                  val lastLoginTime: js.UndefOr[js.Date]) extends UserProfileLike with PlayerStatistics

/**
 * User Profile Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object UserProfile {

  def apply(userID: js.UndefOr[String] = js.undefined,
            username: js.UndefOr[String] = js.undefined,
            email: js.UndefOr[String] = js.undefined,
            equity: js.UndefOr[Double] = js.undefined,
            wallet: js.UndefOr[Double] = js.undefined,
            totalXP: js.UndefOr[Int] = js.undefined,
            awards: js.UndefOr[js.Array[String]] = js.undefined,
            gamesCompleted: js.UndefOr[Int] = js.undefined,
            gamesCreated: js.UndefOr[Int] = js.undefined,
            gamesDeleted: js.UndefOr[Int] = js.undefined,
            gamesJoined: js.UndefOr[Int] = js.undefined,
            gamesWithdrawn: js.UndefOr[Int] = js.undefined,
            lastLoginTime: js.UndefOr[js.Date] = js.undefined): UserProfile = {
    new UserProfile(
      userID, username, email, equity, wallet, totalXP, awards,
      gamesCompleted, gamesCreated, gamesDeleted, gamesJoined, gamesWithdrawn, lastLoginTime)
  }

}
