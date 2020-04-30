package com.shocktrade.common.models.user

import scala.scalajs.js

/**
 * Online Player Status
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class OnlineStatus(var userID: String,
                   var connected: Boolean = false,
                   var lastUpdatedTime: Double = js.Date.now()) extends js.Object