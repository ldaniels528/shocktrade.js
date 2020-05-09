package com.shocktrade.common.models.user

import scala.scalajs.js

/**
 * Represents the Player Statistics data model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait PlayerStatistics extends js.Object {

  def gamesCompleted: js.UndefOr[Int]

  def gamesCreated: js.UndefOr[Int]

  def gamesDeleted: js.UndefOr[Int]

  def gamesJoined: js.UndefOr[Int]

  def gamesWithdrawn: js.UndefOr[Int]

}

/**
 * Player Statistics
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PlayerStatistics {

  def apply(gamesCompleted: js.UndefOr[Int] = js.undefined,
            gamesCreated: js.UndefOr[Int] = js.undefined,
            gamesDeleted: js.UndefOr[Int] = js.undefined,
            gamesJoined: js.UndefOr[Int] = js.undefined,
            gamesWithdrawn: js.UndefOr[Int] = js.undefined): PlayerStatistics = {
    UserProfile(
      gamesCompleted = gamesCompleted,
      gamesCreated = gamesCreated,
      gamesDeleted = gamesDeleted,
      gamesJoined = gamesJoined,
      gamesWithdrawn = gamesWithdrawn)
  }

}