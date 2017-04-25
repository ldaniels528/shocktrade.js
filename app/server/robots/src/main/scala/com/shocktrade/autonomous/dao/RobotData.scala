package com.shocktrade.autonomous.dao

import io.scalajs.npm.mongodb.ObjectID

import scala.scalajs.js

/**
  * Robot Data
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class RobotData(val _id: js.UndefOr[ObjectID] = js.undefined,
                val playerID: js.UndefOr[String],
                val facebookID: js.UndefOr[String],
                val name: js.UndefOr[String],
                val tradingStrategy: js.UndefOr[TradingStrategy],
                val lastActivated: js.UndefOr[js.Date],
                val active: js.UndefOr[Boolean]) extends js.Object