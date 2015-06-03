package com.shocktrade.server.actors

import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.shocktrade.models.quote.StockQuotes
import com.shocktrade.server.actors.YahooKeyStatisticsUpdateActor._
import com.shocktrade.services.yahoofinance.YFKeyStatisticsService
import com.shocktrade.util.BSONHelper._
import play.libs.Akka
import reactivemongo.bson.{BSONDocument => BS}

import scala.concurrent.ExecutionContext

/**
 * Yahoo! Key Statistics Update Actor
 * @author lawrence.daniels@gmail.com
 */
class YahooKeyStatisticsUpdateActor() extends Actor with ActorLogging {
  implicit val ec = context.dispatcher
  val counter = new AtomicInteger()

  override def receive = {
    case RefreshAllKeyStatistics =>
      log.info("Loading symbols for key statistics updates...")
      val mySender = sender()

      counter.set(0)
      var count = 0
      StockQuotes.getSymbolsForKeyStatisticsUpdate foreach { docs =>
        docs.flatMap(_.getAs[String]("symbol")) foreach { symbol =>
          count += 1
          YahooKeyStatisticsUpdateActor ! RefreshKeyStatics(symbol)
        }
        mySender ! count
      }

    case RefreshKeyStatics(symbol) =>
      val ks = YFKeyStatisticsService.getKeyStatisticsSync(symbol)
      import ks._

      StockQuotes.updateQuote(ks.symbol, BS(
        "pctHeldByInsiders" -> pctHeldByInsiders,
        "pctHeldByInstitutions" -> pctHeldByInstitutions,
        "dividendYield5YearAvg" -> dividendYield5YearAvg,
        "change52Week" -> change52Week,
        "high52Week" -> high52Week,
        "low52Week" -> low52Week,
        "movingAverage50Day" -> movingAverage50Day,
        "movingAverage200Day" -> movingAverage200Day,
        "avgVolume3Month" -> avgVolume3Month,
        "avgVolume10Day" -> avgVolume10Day,
        "beta" -> beta,
        "bookValuePerShare" -> bookValuePerShare,
        "currentRatio" -> currentRatio,
        "dilutedEPS" -> dilutedEPS,
        "dividendDate" -> dividendDate,
        "EBITDA" -> EBITDA,
        "enterpriseValue" -> enterpriseValue,
        "enterpriseValueOverEBITDA" -> enterpriseValueOverEBITDA,
        "enterpriseValueOverRevenue" -> enterpriseValueOverRevenue,
        "exDividendDate" -> exDividendDate,
        "fiscalYearEndDate" -> fiscalYearEndDate,
        "sharesFloat" -> sharesFloat,
        "forwardAnnualDividendRate" -> forwardAnnualDividendRate,
        "forwardAnnualDividendYield" -> forwardAnnualDividendYield,
        "forwardPE" -> forwardPE,
        "grossProfit" -> grossProfit,
        "lastSplitDate" -> lastSplitDate,
        "lastSplitFactor" -> lastSplitFactor,
        "leveredFreeCashFlow" -> leveredFreeCashFlow,
        "marketCapIntraday" -> marketCapIntraday,
        "mostRecentQuarterDate" -> mostRecentQuarterDate,
        "netIncomeAvailToCommon" -> netIncomeAvailToCommon,
        "operatingCashFlow" -> operatingCashFlow,
        "operatingMargin" -> operatingMargin,
        "pegRatio5YearExp" -> pegRatio5YearExp,
        "payoutRatio" -> payoutRatio,
        "priceOverBookValue" -> priceOverBookValue,
        "priceOverSales" -> priceOverSales,
        "profitMargin" -> profitMargin,
        "earningsGrowthQuarterly" -> earningsGrowthQuarterly,
        "revenueGrowthQuarterly" -> revenueGrowthQuarterly,
        "returnOnAssets" -> returnOnAssets,
        "returnOnEquity" -> returnOnEquity,
        "revenue" -> revenue,
        "revenuePerShare" -> revenuePerShare,
        "change52WeekSNP500" -> change52WeekSNP500,
        "sharesOutstanding" -> sharesOutstanding,
        "sharesShort" -> sharesShort,
        "sharesShortPriorMonth" -> sharesShortPriorMonth,
        "shortPctOfFloat" -> shortPctOfFloat,
        "shortRatio" -> shortRatio,
        "totalCash" -> totalCash,
        "totalCashPerShare" -> totalCashPerShare,
        "totalDebt" -> totalDebt,
        "totalDebtOverEquity" -> totalDebtOverEquity,
        "trailingAnnualDividendYield" -> trailingAnnualDividendYield,
        "trailingPE" -> trailingPE,

        // administrative fields
        "yfKeyStatsRespTimeMsec" -> responseTimeMsec,
        "yfKeyStatsLastUpdated" -> new Date()
      ))

      // log the statistics
      if (counter.incrementAndGet() % 1000 == 0) {
        log.info(s"Processed ${counter.get} key statistics")
      }

    case message =>
      log.error(s"Unhandled message: $message (${Option(message).map(_.getClass.getName).orNull}})")
      unhandled(message)

  }
}

/**
 * Yahoo! Key Statistics Update Actor Singleton
 * @author lawrence.daniels@gmail.com
 */
object YahooKeyStatisticsUpdateActor {
  private val myActor = Akka.system.actorOf(Props[YahooKeyStatisticsUpdateActor].withRouter(RoundRobinPool(nrOfInstances = 10)), name = "KeyStatisticsUpdate")

  def !(message: Any) = myActor ! message

  def ?(message: Any)(implicit ec: ExecutionContext, timeout: Timeout) = myActor ? message

  case object RefreshAllKeyStatistics

  case class RefreshKeyStatics(symbol: String)

}
