package com.shocktrade.controllers

import java.util.Date

import com.shocktrade.models.quote.StockQuotes
import com.shocktrade.services.googlefinance.GoogleFinanceTradingHistoryService
import com.shocktrade.services.googlefinance.GoogleFinanceTradingHistoryService.GFHistoricalQuote
import org.joda.time.DateTime
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{Controller, _}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.{BSONDocument => BS, _}
import reactivemongo.core.commands.GetLastError

import scala.concurrent.Future

/**
 * Quote REST Resources
 * @author lawrence.daniels@gmail.com
 */
object QuoteResources extends Controller with MongoController with ProfileFiltering {
  private lazy val naicsCodes = loadNaicsMappings()
  private lazy val sicCodes = loadSicsMappings()
  private val Stocks = "Stocks"
  lazy val mcQ = db.collection[JSONCollection]("Stocks")
  lazy val mcN = db.collection[JSONCollection]("NAICS")
  lazy val mcP = db.collection[JSONCollection]("Players")
  lazy val mcS = db.collection[JSONCollection]("SIC")

  val limitFields = JS(
    "name" -> 1, "symbol" -> 1, "exchange" -> 1, "lastTrade" -> 1,
    "change" -> 1, "changePct" -> 1, "spread" -> 1, "volume" -> 1)

  val searchFields = JS(
    "name" -> 1, "symbol" -> 1, "exchange" -> 1, "lastTrade" -> 1, "change" -> 1, "changePct" -> 1,
    "open" -> 1, "close" -> 1, "high" -> 1, "low" -> 1, "tradeDate" -> 1, "spread" -> 1, "volume" -> 1)

  // preload the quotes
  // TODO reinstate this later ...
  // StockQuotes.init(searchFields)

  /**
   * Auto-completes symbols and company names
   */
  def autocomplete(searchTerm: String, maxResults: Int) = Action.async { implicit request =>
    mcQ.find(
      // { active : true, $or : [ {symbol : { $regex: ^?0, $options:'i' }}, {name : { $regex: ?0, $options:'i' }} ] }
      JS(/*"active" -> true,*/ "$or" -> JsArray(Seq(
        JS("symbol" -> JS("$regex" -> s"^$searchTerm", "$options" -> "i")),
        JS("name" -> JS("$regex" -> s"^$searchTerm", "$options" -> "i"))))),
      // fields
      JS("symbol" -> 1, "name" -> 1, "exchange" -> 1, "assetType" -> 1))
      .sort(JS("symbol" -> 1))
      .cursor[JsObject]
      .collect[Seq](maxResults) map (r => Ok(JsArray(r)))
  }

  def exploreSectors(userID: String) = Action.async {
    import reactivemongo.core.commands._

    val results = for {
      exchanges <- findStockExchanges(Option(userID))
      quotes <- db.command(new Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "exchange" -> BS("$in" -> exchanges), "assetType" -> BS("$in" -> Seq("Common Stock", "ETF")), "sector" -> BS("$ne" -> BSONNull))),
        GroupField("sector")("total" -> SumValue(1))))) map { results =>
        results.toSeq map (Json.toJson(_))
      }
    } yield quotes
    results map (js => Ok(JsArray(js)))
  }

  def exploreIndustries(userID: String, sector: String) = Action.async {
    import reactivemongo.core.commands._

    val results = for {
      exchanges <- findStockExchanges(Option(userID))
      quotes <- db.command(new Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "exchange" -> BS("$in" -> exchanges), "assetType" -> BS("$in" -> Seq("Common Stock", "ETF")), "sector" -> sector, "industry" -> BS("$ne" -> BSONNull))),
        GroupField("industry")("total" -> SumValue(1))))) map { results =>
        results.toSeq map (Json.toJson(_))
      }
    } yield quotes
    results map (js => Ok(JsArray(js)))
  }

  def exploreSubIndustries(userID: String, sector: String, industry: String) = Action.async {
    import reactivemongo.core.commands._

    val results = for {
      exchanges <- findStockExchanges(Option(userID))
      quotes <- db.command(new Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "exchange" -> BS("$in" -> exchanges), "assetType" -> BS("$in" -> Seq("Common Stock", "ETF")), "sector" -> sector, "industry" -> industry, "subIndustry" -> BS("$ne" -> BSONNull))),
        GroupField("subIndustry")("total" -> SumValue(1))))) map { results =>
        results.toSeq map (Json.toJson(_))
      }
    } yield quotes
    results map (js => Ok(JsArray(js)))
  }

  def exploreQuotesBySubIndustry(userID: String, sector: String, industry: String, subIndustry: String) = Action.async {
    val results = for {
      exchanges <- findStockExchanges(Option(userID))
      quotes <- mcQ.find(JS("active" -> true, "exchange" -> BS("$in" -> exchanges), "assetType" -> JS("$in" -> Seq("Common Stock", "ETF")), "sector" -> sector, "industry" -> industry, "subIndustry" -> subIndustry), searchFields)
        .cursor[JsObject]
        .collect[Seq]()
    } yield quotes
    results map (js => Ok(JsArray(js)))
  }

  def exploreNAICSSectors = Action.async {
    import reactivemongo.core.commands._

    (for {
    // get the NAICS codes
      codes <- naicsCodes
      results <- db.command(new Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "naicsNumber" -> BS("$ne" -> BSONNull))),
        GroupField("naicsNumber")("total" -> SumValue(1))))) map (_.toSeq map { bs =>
        Json.toJson(bs) match {
          case jo: JsObject =>
            val naicsNumber = (jo \ "_id").asOpt[Int].getOrElse(0)
            jo ++ JS("label" -> codes.get(naicsNumber))
          case jv => jv
        }
      })
    } yield results) map (js => Ok(JsArray(js)))
  }

  def exploreSICSectors = Action.async {
    import reactivemongo.core.commands._

    (for {
    // get the SIC codes
      codes <- sicCodes
      results <- db.command(new Aggregate(Stocks, Seq(
        Match(BS("active" -> true, "sicNumber" -> BS("$ne" -> BSONNull))),
        GroupField("sicNumber")("total" -> SumValue(1))))) map (_.toSeq map { bs =>
        Json.toJson(bs) match {
          case jo: JsObject =>
            val sicNumber = (jo \ "_id").asOpt[Int].getOrElse(0)
            jo ++ JS("label" -> codes.get(sicNumber))
          case jv => jv
        }
      })
    } yield results) map (js => Ok(JsArray(js)))
  }

  def getExchangeCounts = Action.async {
    import reactivemongo.core.commands._

    db.command(new Aggregate(Stocks, Seq(
      Match(BS("active" -> true, "exchange" -> BS("$ne" -> BSONNull), "assetType" -> BS("$in" -> BSONArray("Common Stock", "ETF")))),
      GroupField("exchange")("total" -> SumValue(1))))) map { results =>
      results.toSeq map (Json.toJson(_))
    } map (js => Ok(JsArray(js)))
  }

  def getCachedQuote(symbol: String) = Action.async {
    StockQuotes.findRealTimeQuote(symbol) map {
      case Some(js) => Ok(js)
      case None =>
        NotFound(s"No quote found for ticker $symbol")
    }
  }

  def getSectorInfo(symbol: String) = Action.async {
    mcQ.find(JS("symbol" -> symbol), JS("symbol" -> 1, "exchange" -> 1, "sector" -> 1, "industry" -> 1, "subIndustry" -> 1))
      .cursor[JsObject]
      .collect[Seq]() map (js => Ok(JsArray(js)))
  }

  def addExchange(id: String, exchange: String) = Action.async { implicit request =>
    mcP.update(JS("_id" -> BSONObjectID(id)), JS("$addToSet" -> JS("exchanges" -> exchange)),
      new GetLastError, upsert = false, multi = false) map { r =>
      Ok(r.errMsg.getOrElse(""))
    }
  }

  def removeExchange(id: String, exchange: String) = Action.async { implicit request =>
    mcP.update(JS("_id" -> BSONObjectID(id)), JS("$pull" -> JS("exchanges" -> exchange)),
      new GetLastError, upsert = false, multi = false) map { r =>
      Ok(r.errMsg.getOrElse(""))
    }
  }

  def getOrderQuote(symbol: String) = Action.async {
    StockQuotes.findRealTimeQuote(symbol) map {
      case Some(result) => Ok(result)
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
    } yield StockQuotes.findDBaseQuotes(symbols)

    def toPriceQuote(js: JsValue) = (for {
      symbol <- (js \ "symbol").asOpt[String]
      lastTrade <- (js \ "lastTrade").asOpt[Double]
    } yield JS(symbol -> JS("lastTrade" -> lastTrade))) getOrElse JS()

    results match {
      case Some(futureQuotes) =>
        futureQuotes map { case JsArray(quotes) =>
          Ok(JsArray(quotes map toPriceQuote))
        }
      case None =>
        Future.successful(BadRequest("JSON request expected"))
    }
  }

  def getQuote(symbol: String) = Action.async {
    val results = for {
      quote <- StockQuotes.findFullQuote(symbol)
      productQuotes <- getEnrichedProducts(quote.getOrElse(JS()))
      naicsMap <- naicsCodes
      sicMap <- sicCodes
      enhanced = quote map { q =>
        // lookup the OTC advisory
        val advisoryTuple = for {
          symbol <- (q \ "symbol").asOpt[String]
          exchange <- (q \ "exchange").asOpt[String]
          (advisory, advisoryType) <- getAdvisory(symbol, exchange)
        } yield (advisory, advisoryType)

        // lookup the SIC and NAICS code
        val sicNumber = Option(q \ "sicNumber") flatMap (_.asOpt[Int])
        val naicsNumber = Option(q \ "naicsNumber") flatMap (_.asOpt[Int])
        val sicDescription = sicNumber flatMap sicMap.get
        val naicsDescription = naicsNumber flatMap naicsMap.get
        val betaDescription = getBetaDescription((q \ "beta").asOpt[Double])

        // add the values to the JSON object
        JS("betaDescription" -> betaDescription,
          "sicDescription" -> sicDescription,
          "naicsDescription" -> naicsDescription,
          "advisory" -> (advisoryTuple map (_._1)),
          "advisoryType" -> (advisoryTuple map (_._2))) ++ q ++
          (if (productQuotes.nonEmpty) JS("products" -> productQuotes) else JS())
      }
    } yield enhanced

    results map {
      case Some(quote) => Ok(quote)
      case None => Ok(JS())
    }
  }

  private def getEnrichedProducts(baseQuote: JsObject): Future[Seq[JsObject]] = {
    baseQuote \ "products" match {
      case JsArray(products) =>
        // get the product mapping
        val pm = products flatMap { p =>
          for {
            symbol <- (p \ "symbol").asOpt[String]
          } yield (symbol, p)
        }

        // if products exist, load the quotes for each product's symbol
        if (pm.isEmpty) Future.successful(Nil)
        else {
          val symbols = pm.map(_._1)
          for {
          // retrieve the product quotes
            productsQuotes <- mcQ.find(JS("symbol" -> JS("$in" -> symbols)), limitFields).cursor[JsObject].collect[Seq]()

            // get the product quote mapping
            pqm = Map(productsQuotes flatMap { pq =>
              for {
                symbol <- (pq \ "symbol").asOpt[String]
              } yield (symbol, pq)
            }: _*)

            // create the enriched products
            enrichedProducts = pm map {
              case (symbol, product: JsObject) => product ++ pqm.getOrElse(symbol, JS())
            }

          } yield enrichedProducts
        }

      case _ =>
        Future.successful(Nil)
    }
  }

  def getQuotes = Action.async { implicit request =>
    // attempt to retrieve the symbols from the request
    val result = for {
      js <- request.body.asJson
      symbols <- js.asOpt[Array[String]] if symbols.nonEmpty
    } yield StockQuotes.findDBaseQuotes(symbols)

    // return the promise of the quotes
    (result match {
      case Some(futureQuotes) => futureQuotes
      case None => Future.successful(JsArray())
    }) map (r => Ok(r))
  }

  def getRealtimeQuote(symbol: String) = Action.async {
    StockQuotes.findRealTimeQuote(symbol) map {
      case Some(js) => Ok(js)
      case None => Ok(JS())
    }
  }

  def getRiskLevel(symbol: String) = Action.async {
    val results = for {
      quote_? <- mcQ.find(JS("symbol" -> symbol)).cursor[JsObject].collect[Seq](1) map (_.headOption)
      result = quote_? match {
        case None => "Unknown"
        case Some(quote) =>
          val beta_? = (quote \ "beta").asOpt[Double]
          beta_? match {
            case Some(beta) if beta >= 0 && beta <= 1.25 => "Low";
            case Some(beta) if beta > 1.25 && beta <= 1.9 => "Medium";
            case Some(beta) => "High"
            case None => "Unknown"
          }
      }

    } yield result
    results map { r => Ok(r) }
  }

  /**
   * Retrieve the historical quotes for the given symbol
   */
  def getTradingHistory(symbol: String) = Action {
    // define the start and end dates
    val endDate = new Date()
    val startDate = new DateTime(endDate).plusDays(-45).toDate

    // get the trading history
    val tradingHistory = GoogleFinanceTradingHistoryService.getTradingHistory(symbol, startDate, endDate)

    // convert to historical quotes (in JSON)
    val quotes = transformHistoricalQuotes(tradingHistory)
    Ok(JsArray(quotes.take(30)))
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
        case 'C' => if (exchange == "NASDAQ") "Exception" else "Continuance"
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

  def findQuotesBySymbols(symbols: Seq[String]): Future[Seq[Quote]] = {
    mcQ.find(JS("symbol" -> JS("$in" -> symbols)), JS("symbol" -> 1, "exchange" -> 1, "industry" -> 1, "sector" -> 1, "lastTrade" -> 1))
      .cursor[Quote]
      .collect[Seq]()
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

  /**
   * Loads the NAICS codes mapping
   */
  private def loadNaicsMappings(): Future[Map[Int, String]] = {
    mcN.find(JS()).cursor[JsObject].collect[Seq]() map {
      _ flatMap { js =>
        for {
          code <- (js \ "naicsNumber").asOpt[Int]
          description <- (js \ "description").asOpt[String]
        } yield (code, description)
      }
    } map (f => Map(f: _*))
  }

  /**
   * Loads the SIC codes mapping
   */
  private def loadSicsMappings(): Future[Map[Int, String]] = {
    mcS.find(JS()).cursor[JsObject].collect[Seq]() map {
      _ flatMap { js =>
        for {
          code <- (js \ "sicNumber").asOpt[Int]
          description <- (js \ "description").asOpt[String]
        } yield (code, description)
      }
    } map (f => Map(f: _*))
  }

  implicit val quoteReads: Reads[Quote] = (
    (__ \ "symbol").read[String] and
      (__ \ "exchange").readNullable[String] and
      (__ \ "sector").readNullable[String] and
      (__ \ "industry").readNullable[String] and
      (__ \ "lastTrade").readNullable[Double])(Quote.apply _)

  case class Quote(symbol: String,
                   market: Option[String],
                   sector: Option[String],
                   industry: Option[String],
                   lastTrade: Option[Double]) {

    def exchange: Option[String] = {
      market map (_.toUpperCase) map {
        case s if s.startsWith("NASD") || s.startsWith("NCM") || s.startsWith("NMS") => "NASDAQ"
        case s if s.startsWith("NYS") || s.startsWith("NYQ") => "NYSE"
        case s if s.startsWith("OTC") => "OTCBB"
        case s if s.startsWith("OTHER") => "OTCBB"
        case s if s == "PNK" => "OTCBB"
        case other => other
      }
    }

  }

}