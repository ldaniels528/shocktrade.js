package com.shocktrade.controllers

import java.util.Date

import akka.util.Timeout
import com.ldaniels528.commons.helpers.OptionHelper._
import com.shocktrade.controllers.QuoteResources.Quote
import com.shocktrade.models.contest.AccessRestrictionType.AccessRestrictionType
import com.shocktrade.models.contest.OrderType.OrderType
import com.shocktrade.models.contest.PriceType.PriceType
import com.shocktrade.models.contest.SearchOptions._
import com.shocktrade.models.contest._
import com.shocktrade.models.quote.StockQuotes
import com.shocktrade.util.BSONHelper._
import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
 * Contest Resources
 * @author lawrence.daniels@gmail.com
 */
object ContestResources extends Controller with MongoExtras {
  val DisplayColumns = Seq(
    "name", "creator", "startTime", "expirationTime", "startingBalance", "status",
    "ranked", "playerCount", "levelCap", "perksAllowed", "maxParticipants",
    "participants._id", "participants.name", "participants.facebookID")

  implicit val timeout: Timeout = 20.seconds

  /**
   * Cancels the specified order
   * @param contestId the given contest ID
   * @param playerId the given player ID
   * @param orderId the given order ID
   */
  def cancelOrder(contestId: String, playerId: String, orderId: String) = Action.async {
    // pull the order, add it to orderHistory, and return the participant
    Contests.closeOrder(contestId.toBSID, playerId.toBSID, orderId.toBSID)("participants.name", "participants.orders", "participants.orderHistory")
      .map(_.orDie(s"Order $orderId could not be canceled"))
      .map(contest => Ok(Json.toJson(contest)))
  }

  /**
   * Performs a search for contests
   * @return a JSON array of [[Contest]] instances
   */
  def contestSearch = Action.async { implicit request =>
    Logger.info(s"SearchOptions = ${request.body.asJson.orNull}")
    Try(request.body.asJson map (_.as[SearchOptions])) match {
      case Success(Some(searchOptions)) =>
        Contests.findContests(searchOptions)() map (contests => Ok(Json.toJson(contests)))
      case Success(None) =>
        Future.successful(BadRequest("Search options were expected as JSON body"))
      case Failure(e) =>
        Logger.error(s"${e.getMessage}: json = ${request.body.asJson.orNull}")
        Future.successful(InternalServerError(e.getMessage))
    }
  }

  /**
   * Creates a new contest
   */
  def createContest = Action.async { implicit request =>
    Try(request.body.asJson.map(_.as[ContestForm])) match {
      case Success(Some(form)) =>
        Contests.createContest(makeContest(form)) map (lastError => Ok(JS("result" -> lastError.message)))
      case Success(None) =>
        Future.successful(BadRequest("Contest form was expected as JSON body"))
      case Failure(e) =>
        Logger.error(s"${e.getMessage}: json = ${request.body.asJson.orNull}")
        Future.successful(InternalServerError(e.getMessage))
    }
  }

  private def makeContest(js: ContestForm) = {
    // create a player instance
    val player = Player(id = js.playerId.toBSID, name = js.playerName, facebookId = js.facebookId)

    // create the contest
    Contest(
      name = js.name,
      creator = player,
      creationTime = new Date(),
      startingBalance = js.startingBalance,
      levelCap = js.levelCap.map(_.toInt),
      perksAllowed = js.perksAllowed,
      restriction = js.restriction,
      messages = List(Message(sentBy = player, text = s"Welcome to ${js.name}")),
      participants = List(Participant(js.playerName, js.facebookId, fundsAvailable = js.startingBalance, id = js.playerId.toBSID))
    )
  }

  /**
   * Creates a new order
   * @param contestId the given contest ID
   * @param playerId the given player ID
   * @return
   */
  def createOrder(contestId: String, playerId: String) = Action.async { implicit request =>
    Logger.info(s"json = ${request.body.asJson}")
    Try(request.body.asJson.map(_.as[OrderForm])) match {
      case Success(Some(form)) =>
        Contests.createOrder(contestId.toBSID, playerId.toBSID, makeOrder(form))("participants.name", "participants.orders", "participants.orderHistory") map {
          case Some(contest) => Ok(Json.toJson(contest))
          case None => BadRequest(s"Contest $contestId not found")
        }
      case Success(None) =>
        Future.successful(BadRequest("No order information"))
      case Failure(e) =>
        Logger.error(s"Error parsing JSON", e)
        Future.successful(BadRequest("Invalid JSON body"))
    }
  }

  private def makeOrder(form: OrderForm) = {
    Order(
      symbol = form.symbol,
      exchange = form.exchange,
      creationTime = new Date(),
      expirationTime = None, // TODO set once orderTerm is implemented
      orderType = form.orderType,
      price = form.limitPrice,
      priceType = form.priceType,
      processedTime = None,
      quantity = form.quantity,
      commission = 9.99,
      emailNotify = form.emailNotify,
      volumeAtOrderTime = form.volumeAtOrderTime
    )
  }

  def getAccessRestrictions = Action {
    Ok(JsArray(AccessRestrictionType.values.toSeq.map(v => JsString(v.toString))))
  }

  def getContestByID(id: String) = Action.async {
    Contests.findContestByID(id.toBSID)() map {
      case Some(contest) => Ok(Json.toJson(contest))
      case None => BadRequest(s"Contest $id not found")
    }
  }

  def getContestRankings(id: String) = Action.async {
    (for {
      contest <- Contests.findContestByID(id.toBSID)() map (_ orDie s"Contest $id not found")
      rankings <- produceRankings(contest)
    } yield rankings) map (Ok(_))
  }

  def getContestParticipant(id: String, playerId: String) = Action.async {
    (for {
      contest <- Contests.findContestByID(id.toBSID)() map (_ orDie s"Contest $id not found")
      player = contest.participants.find(_.id == playerId) orDie s"Player $playerId not found"
      enrichedPlayer <- enrichParticipant(player)
    } yield enrichedPlayer).map(p => Ok(JsArray(Seq(p))))
  }

  /**
   * Returns a trading clock state object
   */
  def getContestsByPlayerID(playerId: String) = Action.async {
    Contests.findContestsByPlayerID(playerId.toBSID)(DisplayColumns: _*) map (contests => Ok(Json.toJson(contests)))
  }

  def getHeldSecurities(playerId: String) = Action.async {
    Contests.findContestsByPlayerID(playerId.toBSID)("participants.$") map { contests =>
      contests.flatMap(_.participants.flatMap(_.positions.map(_.symbol)))
    } map (symbols => Ok(JsArray(symbols.distinct.map(JsString))))
  }

  def joinContest(id: String) = Action.async { implicit request =>
    Try(request.body.asJson map (_.as[JoinContestForm])) match {
      case Success(Some(js)) =>
        (for {
          startingBalance <- Contests.findContestByID(id.toBSID)() map (_ orDie "Contest not found") map (_.startingBalance)
          participant = Participant(js.playerName, js.facebookId, fundsAvailable = startingBalance, id = js.playerId.toBSID)
          contest <- Contests.joinContest(id.toBSID, participant)
        } yield contest) map (c => Ok(Json.toJson(c)))
      case Success(None) => Future.successful(Ok(JS("error" -> "Internal error")))
      case Failure(e) => Future.successful(Ok(JS("error" -> "Internal error")))
    }
  }

  def createChatMessage(contestId: String) = Action.async { implicit request =>
    request.body.asJson flatMap (_.asOpt[Message]) match {
      case Some(message) =>
        Contests.createMessage(contestId.toBSID, message)("messages") map {
          case Some(contest) => Ok(Json.toJson(contest.messages))
          case None => Ok(JsArray())
        }
      case None =>
        Future.successful(Ok(JS("status" -> "error", "message" -> "No message sent")))
    }
  }

  private def produceRankings(contest: Contest): Future[JsArray] = {
    for {
    // compute the total equity for each player
      rankings <- produceNetWorths(contest)

      // sort the participants by net-worth
      rankedPlayers = (1 to rankings.size).toSeq zip rankings.sortBy(-_.totalEquity)

    } yield JsArray(rankedPlayers map { case (place, p) =>
      import p._
      JS("name" -> name,
        "facebookID" -> facebookID,
        "score" -> score,
        "totalEquity" -> totalEquity,
        "gainLoss" -> gainLoss_%,
        "rank" -> placeName(place))
    })
  }

  private def produceNetWorths(contest: Contest): Future[Seq[Ranking]] = {
    // get the contest's values
    val startingBalance = contest.startingBalance
    val participants = contest.participants
    val allSymbols = participants.flatMap(_.positions.map(_.symbol))

    for {
    // query the quotes for all symbols
      quotes <- QuoteResources.findQuotesBySymbols(allSymbols)

      // create the mapping of symbols to quotes
      mapping = Map(quotes map (q => (q.symbol.getOrElse(""), q)): _*)

      // get the participants' net worth and P&L
      totalWorths = participants map (asRanking(startingBalance, mapping, _))

    // return the players' total worth
    } yield totalWorths
  }

  private def enrichParticipant(player: Participant): Future[JsObject] = {
    import player.positions

    // get the positions and associated symbols
    val symbols = positions.map(_.symbol).distinct

    for {
    // load the quotes for all position symbols
      quotesJs <- StockQuotes.findQuotes(symbols)("symbol", "lastTrade")

      // build a mapping of symbol to last trade
      quotes = Map(quotesJs flatMap { js =>
        for {symbol <- (js \ "symbol").asOpt[String]; lastTrade <- (js \ "lastTrade").asOpt[Double]} yield (symbol, lastTrade)
      }: _*)

      // enrich the positions
      enrichedPositions = positions flatMap { pos =>
        for {
          marketPrice <- quotes.get(pos.symbol)
          netValue = marketPrice * pos.quantity
          gainLoss = netValue - pos.cost
        } yield JS("lastTrade" -> marketPrice, "netValue" -> netValue, "gainLoss" -> gainLoss) ++ Json.toJson(pos).asInstanceOf[JsObject]
      }

      // re-insert into the participant object
      enrichedPlayer = Json.toJson(player).asInstanceOf[JsObject] ++ JS("positions" -> JsArray(enrichedPositions))
      _ = Logger.info(s"enrichedPositions = $enrichedPositions")
    } yield enrichedPlayer
  }

  private def asRanking(startingBalance: BigDecimal, mapping: Map[String, Quote], p: Participant) = {
    import p.{facebookId, fundsAvailable, name, positions, score}

    val symbols = positions.map(_.symbol).distinct
    val investment = (symbols flatMap (s => mapping.get(s) flatMap (_.lastTrade))).sum
    val totalEquity = fundsAvailable + investment
    val gainLoss_% = ((totalEquity - startingBalance) / startingBalance) * 100d
    Ranking(name, facebookId, score, totalEquity, gainLoss_%)
  }

  private def placeName(place: Int) = {
    place match {
      case 1 => "1st"
      case 2 => "2nd"
      case 3 => "3rd"
      case n => s"${n}th"
    }
  }

  case class ContestForm(name: String,
                         playerId: String,
                         playerName: String,
                         facebookId: String,
                         startingBalance: BigDecimal,
                         levelCapAllowed: Option[Boolean],
                         levelCap: Option[String],
                         perksAllowed: Option[Boolean],
                         restrictionEnabled: Option[Boolean],
                         restriction: Option[AccessRestrictionType])

  implicit val contestFormReads: Reads[ContestForm] = (
    (__ \ "name").read[String] and
      (__ \ "player" \ "id").read[String] and
      (__ \ "player" \ "name").read[String] and
      (__ \ "player" \ "facebookId").read[String] and
      (__ \ "startingBalance").read[BigDecimal] and
      (__ \ "levelCapAllowed").readNullable[Boolean] and
      (__ \ "levelCap").readNullable[String] and
      (__ \ "perksAllowed").readNullable[Boolean] and
      (__ \ "restrictionEnabled").readNullable[Boolean] and
      (__ \ "restriction").readNullable[AccessRestrictionType])(ContestForm.apply _)

  case class JoinContestForm(playerId: String,
                             playerName: String,
                             facebookId: String)

  implicit val joinContestFormReads: Reads[JoinContestForm] = (
    (__ \ "player" \ "id").read[String] and
      (__ \ "player" \ "name").read[String] and
      (__ \ "player" \ "facebookId").read[String])(JoinContestForm.apply _)

  /**
   * contestId = 553aa9f15dd0bcf00087f6ea, playerId = 51a308ac50c70a97d375a6b2,
   * form = {"emailNotify":true,"symbol":"AMD","limitPrice":2.3,"exchange":"NasdaqCM","volumeAtOrderTime":15001242,"orderType":"BUY",
   * "priceType":"MARKET","orderTerm":"GOOD_FOR_7_DAYS","quantity":"1000"}
   */
  case class OrderForm(symbol: String,
                       exchange: String,
                       limitPrice: BigDecimal,
                       orderType: OrderType,
                       priceType: PriceType,
                       //orderTerm: OrderTermType,
                       quantity: Int,
                       volumeAtOrderTime: Long,
                       emailNotify: Boolean)

  implicit val orderFormReads: Reads[OrderForm] = (
    (__ \ "symbol").read[String] and
      (__ \ "exchange").read[String] and
      (__ \ "limitPrice").read[BigDecimal] and
      (__ \ "orderType").read[OrderType] and
      (__ \ "priceType").read[PriceType] and
      (__ \ "quantity").read[String].map(_.toInt) and
      (__ \ "volumeAtOrderTime").read[Long] and
      (__ \ "emailNotify").read[Boolean])(OrderForm.apply _)

  case class Ranking(name: String, facebookID: String, score: Int, totalEquity: BigDecimal, gainLoss_% : BigDecimal)

}