package com.shocktrade.common.models.user

import scala.scalajs.js

/**
 * Represents a User Profile-like model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait UserProfileLike extends UserLike {

  var followers: js.UndefOr[js.Array[String]]

  def username: js.UndefOr[String]

  def country: js.UndefOr[String]

  def level: js.UndefOr[Int]

  def rep: js.UndefOr[Int]

  def netWorth: js.UndefOr[Double]

  def wallet: js.UndefOr[Double]

  def totalXP: js.UndefOr[Int]

  def favoriteSymbols: js.UndefOr[js.Array[String]]

  def recentSymbols: js.UndefOr[js.Array[String]]

  def friends: js.UndefOr[js.Array[String]]

  def awards: js.UndefOr[js.Array[String]]

  def lastLoginTime: js.UndefOr[js.Date]

}
