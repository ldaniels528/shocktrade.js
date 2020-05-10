package com.shocktrade.webapp.routes.contest.dao

import com.shocktrade.common.models.contest.ContestLike

import scala.scalajs.js

/**
 * Contest Data Model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestData(val contestID: js.UndefOr[String],
                  val name: js.UndefOr[String],
                  val hostUserID: js.UndefOr[String],
                  val startTime: js.UndefOr[js.Date],
                  val expirationTime: js.UndefOr[js.Date],
                  val startingBalance: js.UndefOr[Double],
                  val status: js.UndefOr[String],
                  val playerCount: js.UndefOr[Int] = js.undefined,
                  val timeOffset: js.UndefOr[Double] = js.undefined,
                  // indicators
                  val friendsOnly: js.UndefOr[Boolean],
                  val invitationOnly: js.UndefOr[Boolean],
                  val levelCap: js.UndefOr[String],
                  val perksAllowed: js.UndefOr[Boolean],
                  val robotsAllowed: js.UndefOr[Boolean]) extends ContestLike
