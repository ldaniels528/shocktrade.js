package com.shocktrade.javascript.app.model

/**
 * Created by ldaniels on 6/5/15.
 */
case class MarketStatus(stateChanged: Boolean = false, active: Boolean = false, sysTime: Double, delay: Double, start: Double, end: Double)
