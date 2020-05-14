package com.shocktrade.common.models.user

import com.shocktrade.common.models.contest.GameLevel

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * Represents a User Profile-like model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait UserProfileLike extends js.Object {

  def userID: js.UndefOr[String]

  def username: js.UndefOr[String]

  def equity: js.UndefOr[Double]

  def wallet: js.UndefOr[Double]

  def totalXP: js.UndefOr[Int]

  def lastLoginTime: js.UndefOr[js.Date]

}

/**
 * UserProfile-Like Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object UserProfileLike {
  private val levelFactor = 1000
  private val repFactor = 5000

  /**
   * User Profile Enrichment
   * @param profile the given [[UserProfile user profile]]
   */
  final implicit class UserProfileLikeEnrichment(val profile: UserProfile) extends AnyVal {

    @inline
    def getLevel: js.UndefOr[Int] = for {
      xp <- profile.totalXP
      level = xp / levelFactor + 1
    } yield Math.max(1, level)

    @inline
    def getLevelDescription: js.UndefOr[String] = getLevel flatMap {
      case level if level < GameLevel.Levels.length => GameLevel.Levels(level).description
      case _ => GameLevel.Levels.lastOption.map(_.description).orUndefined
    }

    @inline
    def nextLevelXP: js.UndefOr[Int] = for {
      level <- getLevel
    } yield level * levelFactor

    @inline
    def getStars: js.UndefOr[js.Array[Int]] = {
      val maxStars = 5
      for {
        xp <- profile.totalXP
        half = if (xp % repFactor > 0) List(1) else List(0)
        full = (1 to Math.min(maxStars, xp / repFactor)).map(_ => 2).toList
        empty = ((half.length + full.length) until maxStars).map(_ => 0).toList
      } yield (empty ::: half ::: full).reverse.take(maxStars).toJSArray
    }

    @inline
    def getTotalXP: js.UndefOr[Int] = for {totalXP <- profile.totalXP} yield Math.max(0, totalXP)

    @inline
    def userID_! : String = profile.userID.getOrElse(throw js.JavaScriptException("User ID is required"))

  }

}