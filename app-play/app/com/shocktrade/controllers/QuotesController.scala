package com.shocktrade.controllers

import java.util.Date
import javax.inject.Inject

import com.github.ldaniels528.commons.helpers.OptionHelper._
import com.github.ldaniels528.commons.helpers.StringHelper._
import com.shocktrade.dao.SecuritiesDAO
import com.shocktrade.models.quote._
import com.shocktrade.services.googlefinance.GoogleFinanceTradingHistoryService
import com.shocktrade.services.googlefinance.GoogleFinanceTradingHistoryService.GFHistoricalQuote
import com.shocktrade.services.yahoofinance.YFRealtimeStockQuoteService
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo._
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.{BSONDocument => BS, _}

import scala.concurrent.Future

/**
  * Quotes Controller
  * @author lawrence.daniels@gmail.com
  */
class QuotesController @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends MongoController with ReactiveMongoComponents {
  private val securitiesDAO = SecuritiesDAO(reactiveMongoApi)

  ////////////////////////////////////////////////////////////////////////////
  //      API Functions
  ////////////////////////////////////////////////////////////////////////////

  /**
    * Auto-completes symbols and company names
    */
  def autoComplete(searchTerm: String, maxResults: Int) = Action.async { implicit request =>
    securitiesDAO.autoComplete(searchTerm, maxResults) map { quotes =>
      val enriched = quotes map (quote => Json.toJson(quote.copy(icon = getIcon(quote))))
      Ok(JsArray(enriched))
    }
  }

  private def getIcon(quote: AutoCompleteQuote): Option[String] = {
    (quote.assetType map {
      case "Crypto-Currency" => "fa fa-bitcoin st_blue"
      case "Currency" => "fa fa-dollar st_blue"
      case "ETF" => "fa fa-stack-exchange st_blue"
      case _ => "fa fa-globe st_blue"
    }) ?? Some("fa fa-globe st_blue")
  }

  def getExchangeCounts = Action.async {
    securitiesDAO.findExchangeSummaries map (_ map (Json.toJson(_))) map (js => Ok(JsArray(js)))
  }

  def getCachedQuote(symbol: String) = Action {
    getQuoteFromService(symbol) match {
      case Some(jsQuote) => Ok(jsQuote)
      case None => NotFound(s"No quote found for ticker $symbol")
    }
  }

  def getOrderQuote(symbol: String) = Action {
    getQuoteFromService(symbol) match {
      case Some(quote) => Ok(Json.toJson(quote))
      case None => NotFound(JS("symbol" -> symbol, "status" -> "error", "message" -> "Symbol not found"))
    }
  }

  /**
    * Retrieves pricing for a collection of symbols
    * POST http://localhost:9000/api/quotes/pricing <JsArray>
    */
  def getPricing = Action.async { request =>
    val results = for {
      js <- request.body.asJson
      symbols <- js.asOpt[Array[String]]
    } yield securitiesDAO.findSnapshotQuotes(symbols)

    results match {
      case Some(futureQuotes) =>
        futureQuotes map (quotes => Ok(Json.toJson(quotes)))
      case None =>
        Future.successful(BadRequest("JSON request expected"))
    }
  }

  def getQuote(symbol: String) = Action.async {
    val results = for {
      quote_? <- securitiesDAO.findFullQuote(symbol)
      advisory_? = getAdvisoryTuple(quote_?)
      naicsCode_? <- getNaicsCode(quote_?)
      products <- getProducts(quote_?)
      sicCode_? <- getSicCode(quote_?)
      enrichedQuote_? = quote_? map { quote =>
        val beta = quote.getAs[Double]("beta")
        val quoteJs = Json.toJson(quote).asInstanceOf[JsObject]

        // add the values to the JSON object
        quoteJs ++ JS(
          "advisory" -> advisory_?.map(_._1),
          "advisoryType" -> advisory_?.map(_._2),
          "betaDescription" -> getBetaDescription(beta),
          "naicsDescription" -> naicsCode_?.map(_.description),
          "products" -> JsArray(products),
          "riskLevel" -> getRiskLevel(beta),
          "sicDescription" -> sicCode_?.map(_.description))
      }
    } yield enrichedQuote_?

    results map {
      case Some(quote) => Ok(quote)
      case None => Ok(JS())
    }
  }

  private def getAdvisoryTuple(quote_? : Option[BS]) = {
    for {
      quote <- quote_?
      symbol <- quote.getAs[String]("symbol")
      exchange <- quote.getAs[String]("exchange")
      (advisory, advisoryType) <- getAdvisory(symbol, exchange)
    } yield (advisory, advisoryType)
  }

  private def getProducts(quote_? : Option[BS]) = {
    quote_? match {
      case Some(quote) => getEnrichedProducts(quote)
      case None => Future.successful(Nil)
    }
  }

  private def getNaicsCode(quote_? : Option[BS]) = {
    quote_? flatMap (_.getAs[Int]("naicsNumber")) match {
      case Some(naicsNumber) => securitiesDAO.findNaicsCodeByNumber(naicsNumber)
      case None => Future.successful(None)
    }
  }

  private def getSicCode(quote_? : Option[BS]) = {
    quote_? flatMap (_.getAs[Int]("sicNumber")) match {
      case Some(sicNumber) => securitiesDAO.findSicCodeByNumber(sicNumber)
      case None => Future.successful(None)
    }
  }

  private def getRiskLevel(beta_? : Option[Double]) = {
    beta_? map {
      case b if b >= 0 && b <= 1.25 => "Low"
      case b if b > 1.25 && b <= 1.9 => "Medium"
      case _ => "High"
    } getOrElse "Unknown"
  }

  private def getEnrichedProducts(baseQuote: BS): Future[Seq[JsObject]] = {
    baseQuote.get("products").flatMap(_.seeAsOpt[Seq[ProductQuote]]) match {
      case Some(products) =>
        // get the product mapping
        val pm = products map { p => (p.symbol, p) }

        // if products exist, load the quotes for each product's symbol
        if (pm.isEmpty) Future.successful(Nil)
        else {
          val symbols = pm.map(_._1)
          for {
          // retrieve the product quotes
            productsQuotes <- securitiesDAO.findProductQuotes(symbols)

            // get the product quote mapping
            pqm = Map(productsQuotes map { pq => (pq.symbol, pq) }: _*)

            // enrich the products
            enrichedProducts = pm.map { case (symbol, p) =>
              val productJs = Json.toJson(p).asInstanceOf[JsObject]
              pqm.get(symbol).map(q => Json.toJson(q).asInstanceOf[JsObject] ++ productJs) getOrElse productJs
            }
          } yield enrichedProducts
        }
      case None =>
        Future.successful(Nil)
    }
  }

  def getQuotes = Action.async { implicit request =>
    val fields = Seq("name", "symbol", "exchange", "open", "close", "lastTrade", "tradeDateTime",
      "high", "low", "high52Week", "low52Week", "spread", "changePct", "volume", "active")

    // attempt to retrieve the symbols from the request
    val result = request.body.asJson map (_.as[Seq[String]])

    // return the promise of the quotes
    result match {
      case Some(symbols) if symbols.nonEmpty =>
        securitiesDAO.findBasicQuotes(symbols).map(quotes => Ok(Json.toJson(quotes)))
      case _ =>
        Future.successful(Ok(JsArray()))
    }
  }

  def getRealtimeQuote(symbol: String) = Action {
    getQuoteFromService(symbol) match {
      case Some(quote) => Ok(Json.toJson(quote))
      case None => Ok(JS())
    }
  }

  /**
    * Retrieve the historical quotes for the given symbol
    */
  def getTradingHistory(symbol: String) = Action {
    // define the start and end dates
    val endDate = new Date()
    val startDate = new DateTime(endDate).plusDays(-45).toDate

    // normalize the ticker
    val ticker = symbol.lastIndexOptionOf(".") map (index => symbol.substring(0, index)) getOrElse symbol
    if (ticker != symbol) {
      Logger.info(s"getTradingHistory: using '$ticker' instead of '$symbol'")
    }

    // get the trading history
    val tradingHistory = GoogleFinanceTradingHistoryService.getTradingHistory(ticker, startDate, endDate).take(30)

    // convert to historical quotes (in JSON)
    Ok(JsArray(transformHistoricalQuotes(tradingHistory)))
  }

  /**
    * Returns the OTC advisory for the given symbol
    */
  private def getAdvisory(symbol: String, exchange: String): Option[(String, String)] = {
    for {
      sym <- Option(symbol) if sym.length > 4
      xchg <- Option(exchange)
      advisory <- Option(sym.last match {
        case 'A' => "Class A asset"
        case 'B' => "Class BS asset"
        case 'C' if exchange == "NASDAQ" => "Exception"
        case 'C' => "Continuance"
        case 'D' => "New issue or reverse split"
        case 'E' => "Delinquent in required SEC filings"
        case 'F' => "Foreign security"
        case 'G' => "First convertible bond"
        case 'H' => "Second convertible bond"
        case 'I' if exchange.contains("OTC") => "Additional warrants or preferreds"
        case 'I' => "Third convertible bond"
        case 'J' => "Voting share - special"
        case 'K' => "Nonvoting (common)"
        case 'L' => "Miscellaneous"
        case 'M' => "Fourth class - preferred shares"
        case 'N' => "Third class - preferred shares"
        case 'O' => "Second class - preferred shares"
        case 'P' => "First class - Preferred shares"
        case 'Q' => "Involved in bankruptcy proceedings"
        case 'R' => "Rights"
        case 'S' => "Shares of beneficial interest"
        case 'T' => "With warrants or rights"
        case 'U' => "Units"
        case 'V' => "Pending issue and distribution"
        case 'W' => "Warrants"
        case 'X' => "Mutual fund"
        case 'Y' => "ADR (American Depositary Receipts)"
        case 'Z' => "Miscellaneous situations, such as stubs, depositary receipts, limited partnership units, or additional warrants or units"
        case _ => null
      })
      advisoryType = sym.last match {
        case 'A' | 'B' | 'N' | 'O' | 'P' | 'R' | 'X' => "INFO"
        case _ => "WARNING"
      }

    } yield (advisory, advisoryType)
  }

  private def transformHistoricalQuotes(tradingHistory: Seq[GFHistoricalQuote]): List[JsValue] = {
    var prev: Option[GFHistoricalQuote] = None
    var prevClose: Option[Double] = None
    var change: Option[Double] = None
    var changePct: Option[Double] = None

    // fold the historical quotes into their JSON equivalents
    tradingHistory.foldLeft[List[JsValue]](Nil) { (list, src) =>
      prev match {
        case Some(q) =>
          change = for {pc <- q.close; c <- src.close} yield c - pc
          changePct = for {
            ch <- change
            c <- src.close
            pct <- if (c != 0) Some(ch / c) else None
          } yield 100.0d * pct
          prevClose = q.close
        case _ =>
      }
      prev = Some(src)

      // create the JSON-based historical quote
      val js = JS(
        "symbol" -> src.symbol,
        "tradeDate" -> src.tradeDate,
        "prevClose" -> prevClose,
        "open" -> src.open,
        "change" -> change,
        "changePct" -> changePct,
        "high" -> src.high,
        "low" -> src.low,
        "spread" -> src.spread,
        "close" -> src.close,
        "volume" -> src.volume)

      // append the quote
      js :: list
    }
  }

  /**
    * Returns the beta description
    */
  private def getBetaDescription(beta_? : Option[Double]): String = {
    beta_? match {
      case None => "The volatility is unknown"
      case Some(_beta) => _beta match {
        case beta if beta == 0.0 => "Moves independently of the Market"
        case beta if beta == 1.0 => "Moves with the Market"
        case beta if beta < 0.0 => f"Moves ${-beta * 100}%.0f%% opposite of the Market"
        case beta if beta < 1.0 => f"${(1.0 - beta) * 100}%.0f%% less volatile than the Market"
        case beta if beta > 1.0 => f"${(beta - 1.0) * 100}%.0f%% more volatile than the Market"
        case _ => "The volatility could not be determined"
      }
    }
  }

  private def getQuoteFromService(symbol: String) = {
    val q = YFRealtimeStockQuoteService.getQuoteSync(symbol)
    if (q.error.exists(_.nonEmpty)) None
    else Some(JS(
      "symbol" -> q.symbol,
      "name" -> q.name,
      "exchange" -> q.exchange,
      "lastTrade" -> q.lastTrade,
      "time" -> q.time,
      "tradeDateTime" -> q.tradeDateTime,
      "change" -> q.change,
      "changePct" -> q.changePct,
      "prevClose" -> q.prevClose,
      "open" -> q.open,
      "close" -> q.close,
      "ask" -> q.ask,
      "askSize" -> q.askSize,
      "bid" -> q.bid,
      "bidSize" -> q.bidSize,
      "target1Yr" -> q.target1Yr,
      "beta" -> q.beta,
      "nextEarningsDate" -> q.nextEarningsDate,
      "low" -> q.low,
      "high" -> q.high,
      "spread" -> q.spread,
      "low52Week" -> q.low52Week,
      "high52Week" -> q.high52Week,
      "volume" -> q.volume,
      "avgVol3m" -> q.avgVol3m,
      "marketCap" -> q.marketCap,
      "peRatio" -> q.peRatio,
      "eps" -> q.eps,
      "dividend" -> q.dividend,
      "divYield" -> q.divYield,
      "responseTimeMsec" -> q.responseTimeMsec,
      "active" -> true))
  }

}