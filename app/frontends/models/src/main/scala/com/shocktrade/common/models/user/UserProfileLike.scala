package com.shocktrade.common.models.user

import scala.scalajs.js

/**
 * Represents a User Profile-like model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait UserProfileLike extends js.Object {

  def userID: js.UndefOr[String]

  def username: js.UndefOr[String]

  def equity: js.UndefOr[Double]

  def funds: js.UndefOr[Double]

  def level: js.UndefOr[Int]

  def wallet: js.UndefOr[Double]

  def totalXP: js.UndefOr[Int]

  def lastLoginTime: js.UndefOr[js.Date]

}
