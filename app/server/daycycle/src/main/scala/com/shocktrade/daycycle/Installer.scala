package com.shocktrade.daycycle

import com.shocktrade.daycycle.daemons._
import com.shocktrade.server.common.{LoggerFactory, TradingClock}
import com.shocktrade.server.concurrent.Daemon
import com.shocktrade.server.dao.NewsDAO._
import com.shocktrade.server.dao.NewsSourceData
import com.shocktrade.server.dao.contest.AwardsDAO._
import com.shocktrade.server.dao.contest.PerksDAO._
import com.shocktrade.server.dao.contest.{AwardData, PerkData}
import io.scalajs.nodejs.setImmediate
import io.scalajs.npm.mongodb.{Db, MongoClient, WriteOptions}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  * Application Installer
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class Installer(dbConnectionString: String)(implicit ec: ExecutionContext) {
  implicit val dbFuture: Future[Db] = MongoClient.connectFuture(dbConnectionString)
  private val logger = LoggerFactory.getLogger(getClass)
  private val awardsDAO = dbFuture.flatMap(_.getAwardsDAO)
  private val newsDAO = dbFuture.flatMap(_.getNewsDAO)
  private val perksDAO = dbFuture.flatMap(_.getPerksDAO)

  def install()(implicit tradingClock: TradingClock) = {
    for {
    // setup contest reference data
      r0 <- insertAwards()
      r1 <- insertNewsSources()
      r2 <- insertPerks()

      // load company data
      d0 <- start(new NADSAQCompanyUpdateDaemon(dbFuture))
      d1 <- start(new EodDataCompanyUpdateDaemon(dbFuture))

      // update securities
      d2 <- start(new SecuritiesUpdateDaemon(dbConnectionString))
      d3 <- start(new KeyStatisticsUpdateDaemon(dbFuture))

      // update company profile
      d4 <- start(new BloombergUpdateDaemon(dbFuture))
      d5 <- start(new BarChartProfileUpdateDaemon(dbFuture))
      d6 <- start(new CikUpdateDaemon(dbFuture))

    } yield (r0, r1, r2, d0, d1, d2, d3, d4, d5, d6)
  }

  private def start[T](daemon: Daemon[T])(implicit tradingClock: TradingClock) = {
    val promise = Promise[T]()
    setImmediate(() => {
      logger.info(s"Starting ${daemon.getClass.getSimpleName}...")
      daemon.run(tradingClock) onComplete {
        case Success(v) => promise.success(v)
        case Failure(e) => promise.failure(e)
      }
    })
    promise.future
  }

  private def insertAwards() = {
    val awards = js.Array(
      new AwardData(name = "Told your friends!", code = "FACEBOOK", icon = "images/accomplishments/facebook.jpg", description = "Posted to FaceBook from ShockTrade"),
      new AwardData(name = "Right back at cha!", code = "FBLIKEUS", icon = "images/accomplishments/facebook.jpg", description = "<i>Told your friends</i> and <span class='facebook'><img src='images/contests/icon_facebook.jpg'>Liked</span> ShockTrade on FaceBook (<i>Pays 1 Perk</i>)"),
      new AwardData(name = "A Little bird told me...", code = "TWITTER", icon = "images/accomplishments/twitter.png", description = "Posted a Tweet from ShockTrade"),
      new AwardData(name = "Your colleagues had to know!", code = "LINKEDIN", icon = "images/accomplishments/linkedin.png", description = "Posted to LinkedIn from ShockTrade"),
      new AwardData(name = "Told your followers!", code = "GOOGPLUS", icon = "images/accomplishments/google_plus.jpg", description = "Posted to Google+ from ShockTrade"),
      new AwardData(name = "A Picture is worth a thousand words!", code = "INSTGRAM", icon = "images/accomplishments/instagram.png", description = "Posted to Instagram from ShockTrade"),
      new AwardData(name = "Self-promotion pays!", code = "MEPROMO", icon = "images/accomplishments/instagram.png", description = "Posted to FaceBook, Google+, Instagram, LinkedIn and Twitter from ShockTrade (<i>Pays 1 Perk</i>)"),
      new AwardData(name = "The Ultimate Socialite!", code = "SOCLITE", icon = "images/accomplishments/instagram.png", description = "Earned all social awards"),
      new AwardData(name = "Perks of the Job!", code = "PERK", icon = "images/accomplishments/perk.gif", description = "Earned a Perk"),
      new AwardData(name = "It's time for the Perk-u-lator!", code = "5PERKS", icon = "images/accomplishments/perk.gif", description = "Earned 5 Perks"),
      new AwardData(name = "Perk Master!", code = "10PERKS", icon = "images/accomplishments/perk.gif", description = "Earned 10 Perks"),
      new AwardData(name = "Euro-Tactular!", code = "EUROTACT", icon = "images/accomplishments/euro-tactular.png", description = "Traded the Euro"),
      new AwardData(name = "International Shopper", code = "INTNSHPR", icon = "images/accomplishments/international_shopper.gif", description = "Traded three or more currencies"),
      new AwardData(name = "Pay Dirt!", code = "PAYDIRT", icon = "images/accomplishments/pay_dirt.png", description = "Your portfolio gained 100% or more"),
      new AwardData(name = "Mad Money!", code = "MADMONEY", icon = "images/accomplishments/made_money.png", description = "Your portfolio gained 250% or more"),
      new AwardData(name = "Crystal Ball", code = "CRYSTBAL", icon = "images/accomplishments/crystal_ball.png", description = "Your portfolio gained 500% or more"),
      new AwardData(name = "Checkered Flag", code = "CHKDFLAG", icon = "images/accomplishments/checkered_flag.png", description = "Finished a Game!"),
      new AwardData(name = "Gold Trophy", code = "GLDTRPHY", icon = "images/accomplishments/gold_trophy.png", description = "Came in first place! (out of 14 players)"))
    awardsDAO.flatMap(_.insertMany(docs = awards, new WriteOptions(upsert = true)).toFuture)
  }

  private def insertNewsSources() = {
    val newsSources = js.Array(
      new NewsSourceData(name = "CNN Money: Markets", url = "http://rss.cnn.com/rss/money_markets.rss", priority = 1),
      new NewsSourceData(name = "CNN Money: Latest News", url = "http://rss.cnn.com/rss/money_latest.rss", priority = 2),
      new NewsSourceData(name = "CBNC News", url = "http://www.cnbc.com/id/100003114/device/rss/rss", priority = 3),
      new NewsSourceData(name = "MarketWatch: Real-time Headlines", url = "http://feeds.marketwatch.com/marketwatch/realtimeheadlines/", priority = 4),
      new NewsSourceData(name = "MarketWatch: Stocks to Watch", url = "http://feeds.marketwatch.com/marketwatch/StockstoWatch/", priority = 5),
      new NewsSourceData(name = "NASDAQ Stocks News", url = "http://articlefeeds.nasdaq.com/nasdaq/categories?category=Stocks", priority = 6))
    newsDAO.flatMap(_.insertMany(docs = newsSources, new WriteOptions(upsert = true)).toFuture)
  }

  private def insertPerks() = {
    val perks = js.Array(
      new PerkData(name = "Purchase Eminent", code = "PRCHEMNT", cost = 500.0, description = "Gives the player the ability to create SELL orders for securities not yet owned"),
      new PerkData(name = "Perfect Timing", code = "PRFCTIMG", cost = 500.0, description = "Gives the player the ability to create BUY orders for more than cash currently available"),
      new PerkData(name = "Compounded Daily", code = "CMPDDALY", cost = 1000.0, description = "Gives the player the ability to earn interest on cash not currently invested"),
      new PerkData(name = "Fee Waiver", code = "FEEWAIVR", cost = 2500.0, description = "Reduces the commissions the player pays for buying or selling securities"),
      new PerkData(name = "Rational People think at the Margin", code = "MARGIN", cost = 2500.0, description = "Gives the player the ability to use margin accounts"),
      new PerkData(name = "Savings and Loans", code = "SAVGLOAN", cost = 5000.0, description = "Gives the player the ability to borrow money"),
      new PerkData(name = "Loan Shark", code = "LOANSHRK", cost = 5000.0, description = "Gives the player the ability to loan other players money at any interest rate"),
      new PerkData(name = "The Feeling's Mutual", code = "MUTFUNDS", cost = 5000.0, description = "Gives the player the ability to create and use mutual funds"),
      new PerkData(name = "Risk Management", code = "RISKMGMT", cost = 5000.0, description = "Gives the player the ability to trade options"))
    perksDAO.flatMap(_.insertMany(docs = perks, new WriteOptions(upsert = true)).toFuture)
  }

}
