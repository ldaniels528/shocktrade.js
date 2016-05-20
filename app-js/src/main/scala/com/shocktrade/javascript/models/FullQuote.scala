package com.shocktrade.javascript.models

import com.github.ldaniels528.meansjs.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * Full Quote
  */
@js.native
trait FullQuote extends OrderQuote with ClassifiedQuote {
  var change52Week: js.UndefOr[Double]
  var movingAverage50Day: js.UndefOr[Double]
  var movingAverage200Day: js.UndefOr[Double]
  var change52WeekSNP500: js.UndefOr[Double]
  var beta: js.UndefOr[Double]

  var revenue: js.UndefOr[Double]
  var revenuePerShare: js.UndefOr[Double]
  var revenueGrowthQuarterly: js.UndefOr[Double]
  var grossProfit: js.UndefOr[Double]
  var EBITDA: js.UndefOr[Double]
  var netIncomeAvailToCommon: js.UndefOr[Double]
  var dilutedEPS: js.UndefOr[Double]
  var earningsGrowthQuarterly: js.UndefOr[Double]

  var totalCash: js.UndefOr[Double]
  var totalDebt: js.UndefOr[Double]
  var currentRatio: js.UndefOr[Double]
  var totalCashPerShare: js.UndefOr[Double]
  var totalDebtOverEquity: js.UndefOr[Double]
  var bookValuePerShare: js.UndefOr[Double]
  var returnOnAssets: js.UndefOr[Double]
  var profitMargin: js.UndefOr[Double]
  var mostRecentQuarterDate: js.UndefOr[Double]
  var returnOnEquity: js.UndefOr[Double]
  var operatingMargin: js.UndefOr[Double]
  var fiscalYearEndDate: js.UndefOr[Double]

  var enterpriseValue: js.UndefOr[Double]
  var trailingPE: js.UndefOr[Double]
  var forwardPE: js.UndefOr[Double]
  var pegRatio: js.UndefOr[Double]
  var priceOverSales: js.UndefOr[Double]
  var priceOverBookValue: js.UndefOr[Double]
  var enterpriseValueOverRevenue: js.UndefOr[Double]
  var enterpriseValueOverEBITDA: js.UndefOr[Double]
  var operatingCashFlow: js.UndefOr[Double]
  var leveredFreeCashFlow: js.UndefOr[Double]

  var avgVolume3Month: js.UndefOr[Double]
  var avgVolume10Day: js.UndefOr[Double]
  var sharesOutstanding: js.UndefOr[Double]
  var sharesFloat: js.UndefOr[Double]
  var pctHeldByInsiders: js.UndefOr[Double]
  var pctHeldByInstitutions: js.UndefOr[Double]
  var sharesShort: js.UndefOr[Double]
  var shortRatio: js.UndefOr[Double]
  var shortPctOfFloat: js.UndefOr[Double]
  var sharesShortPriorMonth: js.UndefOr[Double]

  var forwardAnnualDividendRate: js.UndefOr[Double]
  var forwardAnnualDividendYield: js.UndefOr[Double]
  var trailingAnnualDividendYield: js.UndefOr[Double]
  var divYield5YearAvg: js.UndefOr[Double]
  var payoutRatio: js.UndefOr[Double]
  var dividendDate: js.UndefOr[Double]
  var exDividendDate: js.UndefOr[Double]
  var lastSplitFactor: js.UndefOr[Double]
  var lastSplitDate: js.UndefOr[Double]

  var legalType: js.UndefOr[String]
  var products: js.UndefOr[js.Array[js.Object]]
}

/**
  * Full Quote Singleton
  */
object FullQuote {

  def apply(symbol: js.UndefOr[String] = js.undefined,
            active: Boolean = true) = {
    val quote = New[FullQuote]
    quote.symbol = symbol
    quote.active = active
    quote
  }

}