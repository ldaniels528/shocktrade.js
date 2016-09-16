package com.shocktrade.common.models

import com.shocktrade.common.models.ProfileLike.QuoteFilter

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Profile-like model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
trait ProfileLike extends js.Object {

  def facebookID: js.UndefOr[String]

  def name: js.UndefOr[String]

  def country: js.UndefOr[String]

  def level: js.UndefOr[Int]

  def rep: js.UndefOr[Int]

  def netWorth: js.UndefOr[Double]

  def totalXP: js.UndefOr[Int]

  def favoriteSymbols: js.UndefOr[js.Array[String]]

  def recentSymbols: js.UndefOr[js.Array[String]]

  def filters: js.UndefOr[js.Array[QuoteFilter]]

  def friends: js.UndefOr[js.Array[String]]

  def accomplishments: js.UndefOr[js.Array[String]]

  def acquaintances: js.UndefOr[js.Array[String]]

  def lastLoginTime: js.UndefOr[js.Date]

}

/**
  * Profile-Like Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ProfileLike {

  @ScalaJSDefined
  class QuoteFilter(var _id: js.UndefOr[String] = js.undefined,
                    var name: js.UndefOr[String] = js.undefined,
                    var conditions: js.UndefOr[js.Array[Condition]] = js.undefined,
                    var ascending: js.UndefOr[Boolean] = js.undefined,
                    var exchange: js.UndefOr[js.Array[String]] = js.undefined,
                    var sortField: js.UndefOr[String] = js.undefined,
                    var maxResults: js.UndefOr[Int] = js.undefined,
                    var timeFrame: js.UndefOr[String] = js.undefined) extends js.Object

  @ScalaJSDefined
  class Condition(var _id: js.UndefOr[String] = js.undefined,
                  var field: js.UndefOr[String] = js.undefined,
                  var operator: js.UndefOr[String] = js.undefined,
                  var value: js.Any = js.undefined) extends js.Object

}