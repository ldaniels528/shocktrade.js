package com.shocktrade.javascript.app.model

import java.util.Date

/**
 * Created by ldaniels on 6/5/15.
 */
case class TransactionHistory(symbol: String,
                              tradeDate: Date,
                              prevClose: Double,
                              open: Double,
                              change: Double,
                              changePct: Double,
                              high: Double,
                              low: Double,
                              spread: Double,
                              close: Double,
                              volume: Double)