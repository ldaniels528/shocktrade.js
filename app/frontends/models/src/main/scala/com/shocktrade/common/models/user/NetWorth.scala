package com.shocktrade.common.models.user

import scala.scalajs.js

/**
 * Represents a user's net-worth
 * @param userID   the user's ID
 * @param username the username
 * @param wallet   the user's cash
 * @param funds    the user's uninvested funds
 * @param equity   the user's investment equity
 */
class NetWorth(val userID: js.UndefOr[String],
               val username: js.UndefOr[String],
               val wallet: js.UndefOr[Double],
               val funds: js.UndefOr[Double],
               val equity: js.UndefOr[Double]) extends js.Object