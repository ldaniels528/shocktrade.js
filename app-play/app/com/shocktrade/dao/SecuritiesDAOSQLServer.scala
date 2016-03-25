package com.shocktrade.dao

import java.util.Date

import anorm.SqlParser._
import anorm._
import com.shocktrade.dao.SecuritiesDAOSQLServer._
import com.shocktrade.models.quote._
import org.joda.time.DateTime
import play.api.Logger
import play.api.Play.current
import play.api.db.DB
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ExecutionContext, Future}

/**
  * Securities DAO SQL Server implementation
  * @author lawrence.daniels@gmail.com
  */
class SecuritiesDAOSQLServer() extends SecuritiesDAO {

  override def autoComplete(searchTerm: String, maxResults: Int)(implicit ec: ExecutionContext) = Future {
    DB.withConnection { implicit c =>
      SQL(
        """SELECT symbol, [name], exchange, assetType FROM dbo.securities
            WHERE symbol LIKE {searchTerm} OR [name] LIKE {searchTerm}
        """)
        .on("searchTerm" -> s"%$searchTerm%")
        .executeQuery().as(AutoCompleteQuoteParser.*)
    }
  }

  override def findBasicQuotes(symbols: Seq[String])(implicit ec: ExecutionContext) = Future {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM dbo.securities WHERE symbol IN {symbols}")
        .on("symbols" -> symbols)
        .executeQuery().as(BasicQuoteParser.*)
    }
  }

  override def findByFilter(filter: QuoteFilter, fields: Seq[String], maxResults: Int)(implicit ec: ExecutionContext) = Future {
    val tenDaysAgo = new DateTime().minusDays(10)

    // start with active stocks whose last trade date was updated in the last 10 days
    val query: String = {
      val sql = new StringBuilder(255)
      sql.append("SELECT * FROM dbo.securities WHERE active = true")

      filter.changeMin.foreach(v => sql.append(s" AND changePct >= $v"))
      filter.changeMax.foreach(v => sql.append(s" AND changePct <= $v"))
      filter.marketCapMin.foreach(v => sql.append(s" AND marketCap >= $v"))
      filter.marketCapMax.foreach(v => sql.append(s" AND marketCap <= $v"))
      filter.priceMin.foreach(v => sql.append(s" AND lastTrade >= $v"))
      filter.priceMax.foreach(v => sql.append(s" AND lastTrade <= $v"))
      filter.spreadMin.foreach(v => sql.append(s" AND spread >= $v"))
      filter.spreadMax.foreach(v => sql.append(s" AND spread <= $v"))
      filter.volumeMin.foreach(v => sql.append(s" AND volume >= $v"))
      filter.volumeMax.foreach(v => sql.append(s" AND volume <= $v"))
      filter.maxResults.foreach(v => sql.append(s" AND volume <= $v"))
      Logger.info(s"SQL: $sql")
      sql.toString()
    }

    DB.withConnection { implicit c =>
      SQL(query).executeQuery().as(ResearchQuoteParser.*)
    }
  }

  override def findExchangeSummaries(implicit ec: ExecutionContext) = Future {
    DB.withConnection { implicit c =>
      SQL("SELECT exchange, COUNT(*) AS count FROM dbo.securities WHERE exchange IS NOT NULL GROUP BY exchange")
        .executeQuery().as(ExchangeSummaryParser.*)
    }
  }

  override def findFullQuote(symbol: String)(implicit ec: ExecutionContext): Future[Option[BSONDocument]] = ???

  override def findSectorQuote(symbol: String)(implicit ec: ExecutionContext) = Future {
    DB.withConnection { implicit c =>
      SQL("SELECT TOP 1 * FROM dbo.securities WHERE symbol = {symbol}")
        .on("symbol" -> symbol)
        .executeQuery().as(SectorQuoteParser.*).headOption
    }
  }

  override def findSnapshotQuotes(symbols: Seq[String])(implicit ec: ExecutionContext) = Future {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM dbo.securities WHERE symbol IN {symbols}")
        .on("symbol" -> symbols)
        .executeQuery().as(QuoteSnapshotParser.*)
    }
  }

  override def findNaicsCodeByNumber(naicsNumber: Int)(implicit ec: ExecutionContext) = Future {
    DB.withConnection { implicit c =>
      SQL("SELECT TOP 1 * FROM dbo.naics_codes WHERE naicsNumber = {naicsNumber}")
        .on("naicsNumber" -> naicsNumber)
        .executeQuery().as(NaicsCodeParser.*).headOption
    }
  }

  override def findNaicsCodeByNumbers(naicsNumbers: Seq[Int])(implicit ec: ExecutionContext) = Future {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM dbo.naics_codes WHERE naicsNumber IN {naicsNumbers}")
        .on("naicsNumbers" -> naicsNumbers)
        .executeQuery().as(NaicsCodeParser.*)
    }
  }

  override def findProductQuotes(symbols: Seq[String])(implicit ec: ExecutionContext) = Future {
    DB.withConnection { implicit c =>
      SQL(s"SELECT ${ProductQuote.Fields.mkString(", ")} FROM dbo.securities WHERE symbol IN {symbols}")
        .on("symbols" -> symbols)
        .executeQuery().as(ProductQuoteParser.*)
    }
  }

  override def findSicCodeByNumber(sicNumber: Int)(implicit ec: ExecutionContext) = Future {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM dbo.sic_codes WHERE sicNumber = {sicNumber}")
        .on("sicNumber" -> sicNumber)
        .executeQuery().as(SicCodeParser.*).headOption
    }
  }

  override def findSicCodeByNumbers(sicNumbers: Seq[Int])(implicit ec: ExecutionContext) = Future {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM dbo.sic_codes WHERE sicNumber IN {sicNumbers}")
        .on("sicNumbers" -> sicNumbers)
        .executeQuery().as(SicCodeParser.*)
    }
  }

  override def findQuotesBySymbols(symbols: Seq[String])(implicit ec: ExecutionContext) = Future {
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM dbo.securities WHERE symbol IN {symbols}")
        .on("symbols" -> symbols)
        .executeQuery().as(SectorQuoteParser.*)
    }
  }

}

/**
  * Securities DAO SQL Server Companion Object
  * @author lawrence.daniels@gmail.com
  */
object SecuritiesDAOSQLServer {

  val AutoCompleteQuoteParser = for {
    symbol <- str("symbol")
    name <- get[Option[String]]("name")
    exchange <- get[Option[String]]("exchange")
    assetType <- get[Option[String]]("assetType")
  } yield AutoCompleteQuote(symbol, name, exchange, assetType)

  val BasicQuoteParser = for {
    symbol <- str("symbol")
    exchange <- get[Option[String]]("exchange")
    lastTrade <- get[Option[Double]]("lastTrade")
    tradeDateTime <- get[Option[Date]]("tradeDateTime")
    changePct <- get[Option[Double]]("changePct")
    prevClose <- get[Option[Double]]("prevClose")
    open <- get[Option[Double]]("open")
    close <- get[Option[Double]]("close")
    low <- get[Option[Double]]("low")
    high <- get[Option[Double]]("high")
    spread <- get[Option[Double]]("spread")
    low52Week <- get[Option[Double]]("low52Week")
    high52Week <- get[Option[Double]]("high52Week")
    volume <- get[Option[Long]]("volume")
    name <- get[Option[String]]("name")
    active <- get[Option[Boolean]]("active")
  } yield BasicQuote(symbol, exchange, lastTrade, tradeDateTime, changePct, prevClose, open, close, low, high, spread, low52Week, high52Week, volume, name, active)

  val ProductQuoteParser = for {
    symbol <- str("symbol")
    exchange <- get[Option[String]]("exchange")
    lastTrade <- get[Option[Double]]("lastTrade")
    open <- get[Option[Double]]("open")
    close <- get[Option[Double]]("close")
    change <- get[Option[Double]]("change")
    changePct <- get[Option[Double]]("changePct")
    spread <- get[Option[Double]]("spread")
    volume <- get[Option[Long]]("volume")
    name <- get[Option[String]]("name")
    sector <- get[Option[String]]("sector")
    industry <- get[Option[String]]("industry")
  } yield ProductQuote(symbol, exchange, lastTrade, open, close, change, changePct, spread, volume, name, sector, industry)

  val ExchangeSummaryParser = for {
    exchange <- str("exchange")
    count <- long("count")
  } yield ExchangeSummary(exchange, count)

  val NaicsCodeParser = for {
    naicsNumber <- int("naicsNumber")
    description <- str("description")
  } yield NaicsCode(naicsNumber, description)

  val QuoteSnapshotParser = for {
    symbol <- str("symbol")
    name <- get[Option[String]]("name")
    lastTrade <- get[Option[Double]]("lastTrade")
    tradeDate <- get[Option[Date]]("tradeDate")
  } yield QuoteSnapshot(symbol, name, lastTrade, tradeDate)

  val ResearchQuoteParser = for {
    symbol <- str("symbol")
    exchange <- get[Option[String]]("exchange")
    name <- get[Option[String]]("name")
    lastTrade <- get[Option[Double]]("lastTrade")
    tradeDateTime <- get[Option[Date]]("tradeDateTime")
    changePct <- get[Option[Double]]("changePct")
    prevClose <- get[Option[Double]]("prevClose")
    open <- get[Option[Double]]("open")
    close <- get[Option[Double]]("close")
    low <- get[Option[Double]]("low")
    high <- get[Option[Double]]("high")
    spread <- get[Option[Double]]("spread")
    volume <- get[Option[Long]]("volume")
  } yield ResearchQuote(symbol, exchange, name, lastTrade, tradeDateTime, changePct, prevClose, open, close, low, high, spread, volume)

  val SectorQuoteParser = for {
    symbol <- str("symbol")
    market <- get[Option[String]]("market")
    sector <- get[Option[String]]("sector")
    industry <- get[Option[String]]("industry")
    subIndustry <- get[Option[String]]("subIndustry")
    lastTrade <- get[Option[Double]]("lastTrade")
  } yield SectorQuote(symbol, market, sector, industry, subIndustry, lastTrade)

  val SicCodeParser = for {
    sicNumber <- int("sicNumber")
    description <- str("description")
  } yield SicCode(sicNumber, description)

}