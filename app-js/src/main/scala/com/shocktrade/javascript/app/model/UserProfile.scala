package com.shocktrade.javascript.app.model

/**
 * User Profile
 * @author lawrence.daniels@gmail.com
 */
case class UserProfile(id: String = null,
                       name: String,
                       admin: Boolean = false,
                       var netWorth: Double = 0.00,
                       country: String = "us",
                       level: Int = 1,
                       lastSymbol: String = "MSFT")