package com.shocktrade.server.dao.securities

import com.shocktrade.common.models.quote.KeyStatistics
import io.scalajs.npm.mongodb.ObjectID

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Key Statistics Data object
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class KeyStatisticsData(var _id: js.UndefOr[ObjectID],
                        var symbol: js.UndefOr[String],
                        var exchange: js.UndefOr[String],
                        var ask: js.UndefOr[Double],
                        var askSize: js.UndefOr[Double],
                        var averageDailyVolume10Day: js.UndefOr[Double],
                        var averageVolume: js.UndefOr[Double],
                        var averageVolume10days: js.UndefOr[Double],
                        var beta: js.UndefOr[Double],
                        var bid: js.UndefOr[Double],
                        var bidSize: js.UndefOr[Double],
                        var dayHigh: js.UndefOr[Double],
                        var dayLow: js.UndefOr[Double],
                        var dividendRate: js.UndefOr[Double],
                        var dividendYield: js.UndefOr[Double],
                        var exDividendDate: js.UndefOr[Double],
                        var expireDate: js.UndefOr[Double],
                        var movingAverage50Day: js.UndefOr[Double],
                        var high52Week: js.UndefOr[Double],
                        var low52Week: js.UndefOr[Double],
                        var fiveYearAvgDividendYield: js.UndefOr[Double],
                        var forwardPE: js.UndefOr[Double],
                        var marketCap: js.UndefOr[Double],
                        var maxAge: js.UndefOr[Int],
                        var navPrice: js.UndefOr[Double],
                        var openInterest: js.UndefOr[Double],
                        var postMarketChange: js.UndefOr[Double],
                        var postMarketChangePercent: js.UndefOr[Double],
                        var postMarketPrice: js.UndefOr[Double],
                        var postMarketSource: js.UndefOr[String],
                        var postMarketTime: js.UndefOr[Double],
                        var preMarketChange: js.UndefOr[Double],
                        var preMarketPrice: js.UndefOr[Double],
                        var preMarketSource: js.UndefOr[String],
                        var previousClose: js.UndefOr[Double],
                        var priceToSalesTrailing12Months: js.UndefOr[Double],
                        var regularMarketDayLow: js.UndefOr[Double],
                        var regularMarketOpen: js.UndefOr[Double],
                        var regularMarketPreviousClose: js.UndefOr[Double],
                        var regularMarketVolume: js.UndefOr[Double],
                        var strikePrice: js.UndefOr[Double],
                        var totalAssets: js.UndefOr[Double],
                        var trailingAnnualDividendRate: js.UndefOr[Double],
                        var trailingAnnualDividendYield: js.UndefOr[Double],
                        var trailingPE: js.UndefOr[Double],
                        var movingAverage200Day: js.UndefOr[Double],
                        var volume: js.UndefOr[Double],
                        var `yield`: js.UndefOr[Double],
                        var ytdReturn: js.UndefOr[Double],
                        var lastUpdated: js.UndefOr[js.Date]) extends KeyStatistics
