package com.shocktrade.common.models.quote

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Complete Quote
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class CompleteQuote(var change52Week: js.UndefOr[Double] = js.undefined,
                    var change52WeekSNP500: js.UndefOr[Double] = js.undefined,
                    var high52Week: js.UndefOr[Double] = js.undefined,
                    var low52Week: js.UndefOr[Double] = js.undefined,

                    var movingAverage50Day: js.UndefOr[Double] = js.undefined,
                    var movingAverage200Day: js.UndefOr[Double] = js.undefined,

                    var revenue: js.UndefOr[Double] = js.undefined,
                    var revenuePerShare: js.UndefOr[Double] = js.undefined,
                    var revenueGrowthQuarterly: js.UndefOr[Double] = js.undefined,
                    var grossProfit: js.UndefOr[Double] = js.undefined,
                    var EBITDA: js.UndefOr[Double] = js.undefined,
                    var netIncomeAvailToCommon: js.UndefOr[Double] = js.undefined,
                    var dilutedEPS: js.UndefOr[Double] = js.undefined,
                    var earningsGrowthQuarterly: js.UndefOr[Double] = js.undefined,

                    var totalCash: js.UndefOr[Double] = js.undefined,
                    var totalDebt: js.UndefOr[Double] = js.undefined,
                    var currentRatio: js.UndefOr[Double] = js.undefined,
                    var totalCashPerShare: js.UndefOr[Double] = js.undefined,
                    var totalDebtOverEquity: js.UndefOr[Double] = js.undefined,
                    var bookValuePerShare: js.UndefOr[Double] = js.undefined,
                    var returnOnAssets: js.UndefOr[Double] = js.undefined,
                    var profitMargin: js.UndefOr[Double] = js.undefined,
                    var mostRecentQuarterDate: js.UndefOr[Double] = js.undefined,
                    var returnOnEquity: js.UndefOr[Double] = js.undefined,
                    var operatingMargin: js.UndefOr[Double] = js.undefined,
                    var fiscalYearEndDate: js.UndefOr[Double] = js.undefined,

                    var enterpriseValue: js.UndefOr[Double] = js.undefined,
                    var trailingPE: js.UndefOr[Double] = js.undefined,
                    var forwardPE: js.UndefOr[Double] = js.undefined,
                    var pegRatio: js.UndefOr[Double] = js.undefined,
                    var priceOverSales: js.UndefOr[Double] = js.undefined,
                    var priceOverBookValue: js.UndefOr[Double] = js.undefined,
                    var enterpriseValueOverRevenue: js.UndefOr[Double] = js.undefined,
                    var enterpriseValueOverEBITDA: js.UndefOr[Double] = js.undefined,
                    var operatingCashFlow: js.UndefOr[Double] = js.undefined,
                    var leveredFreeCashFlow: js.UndefOr[Double] = js.undefined,

                    var avgVolume3Month: js.UndefOr[Double] = js.undefined,
                    //var avgVolume10Day: js.UndefOr[Double] = js.undefined,
                    var sharesOutstanding: js.UndefOr[Double] = js.undefined,
                    var sharesFloat: js.UndefOr[Double] = js.undefined,
                    var pctHeldByInsiders: js.UndefOr[Double] = js.undefined,
                    var pctHeldByInstitutions: js.UndefOr[Double] = js.undefined,
                    var sharesShort: js.UndefOr[Double] = js.undefined,
                    var shortRatio: js.UndefOr[Double] = js.undefined,
                    var shortPctOfFloat: js.UndefOr[Double] = js.undefined,
                    var sharesShortPriorMonth: js.UndefOr[Double] = js.undefined,

                    var forwardAnnualDividendRate: js.UndefOr[Double] = js.undefined,
                    var forwardAnnualDividendYield: js.UndefOr[Double] = js.undefined,
                    var trailingAnnualDividendYield: js.UndefOr[Double] = js.undefined,
                    var divYield5YearAvg: js.UndefOr[Double] = js.undefined,
                    var payoutRatio: js.UndefOr[Double] = js.undefined,
                    var dividendDate: js.UndefOr[Double] = js.undefined,
                    var exDividendDate: js.UndefOr[Double] = js.undefined,
                    var lastSplitFactor: js.UndefOr[Double] = js.undefined,
                    var lastSplitDate: js.UndefOr[Double] = js.undefined,

                    var products: js.UndefOr[js.Array[js.Object]] = js.undefined,

                    // classification
                    var legalType: js.UndefOr[String] = js.undefined,
                    var assetClass: js.UndefOr[String] = js.undefined,
                    var assetType: js.UndefOr[String] = js.undefined,

                    // standard codes
                    var naicsNumber: js.UndefOr[Int] = js.undefined,
                    var sicNumber: js.UndefOr[Int] = js.undefined,

                    // summary
                    var businessSummary: js.UndefOr[String] = js.undefined,
                    var executives: js.UndefOr[js.Array[CompleteQuote.Executive]] = js.undefined) extends ResearchQuote

/**
  * Complete Quote Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object CompleteQuote {

  /**
    * Company Executive
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  @ScalaJSDefined
  class Executive() extends js.Object

}