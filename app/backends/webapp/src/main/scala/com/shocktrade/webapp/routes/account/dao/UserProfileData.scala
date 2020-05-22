package com.shocktrade.webapp.routes.account.dao

import com.shocktrade.common.models.user.{PlayerStatistics, UserProfileLike}

import scala.scalajs.js

/**
 * Represents a User Profile data model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class UserProfileData(val userID: js.UndefOr[String],
                      val username: js.UndefOr[String],
                      var email: js.UndefOr[String],
                      val equity: js.UndefOr[Double],
                      val wallet: js.UndefOr[Double],
                      val totalXP: js.UndefOr[Double],
                      val gamesCompleted: js.UndefOr[Int],
                      val gamesCreated: js.UndefOr[Int],
                      val gamesDeleted: js.UndefOr[Int],
                      val gamesJoined: js.UndefOr[Int],
                      val gamesWithdrawn: js.UndefOr[Int],
                      val trophiesGold: js.UndefOr[Int],
                      val trophiesSilver: js.UndefOr[Int],
                      val trophiesBronze: js.UndefOr[Int],
                      val lastLoginTime: js.UndefOr[js.Date]) extends UserProfileLike with PlayerStatistics