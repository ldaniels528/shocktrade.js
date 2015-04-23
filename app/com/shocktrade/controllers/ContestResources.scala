package com.shocktrade.controllers

import java.util.Date

import akka.util.Timeout
import com.ldaniels528.commons.helpers.OptionHelper._
import com.shocktrade.controllers.QuoteResources.Quote
import com.shocktrade.models.contest.SearchOptions._
import com.shocktrade.models.contest._
import com.shocktrade.models.quote.StockQuotes
import com.shocktrade.util.BSONHelper._
import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, __, _}
import play.api.mvc._
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID

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
    "participants.name", "participants.facebookID")

  implicit val timeout: Timeout = 20.seconds

  /**
   * Cancels the given order
   * @param contestId the given contest ID
   * @param playerId the given player ID
   * @param orderId the given order ID
   */
  def cancelOrder(contestId: String, playerId: String, orderId: String) = Action.async {
    Logger.info(s"cancelOrder - contest Id: $contestId, playerName: $playerId, orderId: $orderId")
    Try {
      for {
      // retrieve the contest skeleton with orders only
      // db.Contests.find({ "participants.orders" : { $elemMatch : { _id : ObjectId("532fb6a4bca7c0f8efc20e49") } } })
        contestWithOrders <- Contests.findOrderByID(contestId.asBSID, orderId.asBSID)("participants.name", "participants.orders")
          .map(_.orDie(s"Contest $contestId not found"))

        // find the player by name
        player = extractParticipantByID(playerId.asBSID, contestWithOrders \ "participants")
          .orDie(s"Player ID '$playerId' not found")

        // find the order to cancel
        order = extractOrderByID(orderId.asBSID, player \ "orders").map(_.asInstanceOf[JsObject])
          .orDie(s"Order ID $orderId not found") ++ JS("message" -> "Canceled")

        // pull the order, add it to orderHistory, and return the participant
        updatedContest <- Contests.closeOrder(contestId.asBSID, playerId.asBSID, orderId.asBSID, order)("participants.name", "participants.orders", "participants.orderHistory")
          .map(_.orDie(s"Order $orderId could not be canceled"))

      // extract just the updated participant
      } yield extractParticipantByID(playerId.asBSID, updatedContest \ "participants")
    } match {
      case Success(result) => result map {
        case Some(player) => Ok(player)
        case None => Ok(JS())
      }
      case Failure(e) => Future.successful(Ok(JS("error" -> e.getMessage)))
    }
  }

  def contestSearch = Action.async { implicit request =>
    Try(request.body.asJson map (_.as[SearchOptions])) match {
      case Success(Some(searchOptions)) =>
        Contests.findContests(searchOptions)() map (list => Ok(JsArray(list)))
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
  def createNewContest = Action.async { implicit request =>
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
    // create the contest skeleton
    val contest = Contest(
      name = js.name,
      creationTime = new Date(),
      startingBalance = BigDecimal(25000d),
      friendsOnly = js.friendsOnly,
      acquaintances = js.acquaintances,
      invitationOnly = js.invitationOnly,
      perksAllowed = js.perksAllowed,
      ranked = js.ranked
    )

    // create a welcome message
    val welcomeMessage = Message(sentBy = Addressee(js.playerName, js.facebookId), text = s"Welcome to ${js.name}")

    // create the first participant (the contest creator)
    val creator = Participant(js.playerName, js.facebookId, fundsAvailable = contest.startingBalance, id = js.playerId.asBSID)

    // create the contest with its participant
    contest.copy(participants = List(creator), messages = List(welcomeMessage))
  }

  def createOrder = Action.async { implicit request =>
    try {
      val result = for {
        js <- request.body.asJson
        contestId = (js \ "contestId").asOpt[String] getOrElse missing("contestId")
        playerId = (js \ "playerId").asOpt[String] getOrElse missing("playerId")
        order = js.as[Order]
      } yield {
          Contests.createOrder(contestId.asBSID, playerId, order)("participants.name", "participants.orders", "participants.orderHistory") map {
            case Some(contestJs) =>
              extractParticipantByID(playerId.asBSID, contestJs \ "participants") match {
                case Some(player) => Ok(player)
                case None => BadRequest("No player found")
              }
            case None => BadRequest(s"Contest $contestId not found")
          }
        }

      result.orDie("Invalid order")
    } catch {
      case e: Exception => Future.successful(BadRequest(e.getMessage))
    }
  }

  def getContestByID(id: String) = Action.async {
    Contests.findContestByID(id.asBSID)() map {
      case Some(contest) => Ok(contest)
      case None => BadRequest(s"Contest $id not found")
    }
  }

  def getContestRankings(id: String) = Action.async {
    (for {
      contest <- Contests.findContestByID(id.asBSID)() map (_ orDie s"Contest $id not found")
      rankings <- produceRankings(contest)
    } yield rankings) map (Ok(_))
  }

  def getContentParticipant(id: String, playerId: String) = Action.async {
    (for {
      contest <- Contests.findContestByID(id.asBSID)() map (_ orDie s"Contest $id not found")
      player = extractParticipantByID(playerId.asBSID, contest \ "participants") orDie s"Player $playerId not found"
      enrichedPlayer <- enrichParticipant(player)
    } yield enrichedPlayer).map(p => Ok(JsArray(Seq(p))))
  }

  def getHeldSecurities(playerId: String) = Action.async {
    val outcome = for {
    // lookup the contests
      contests <- Contests.findContestsByPlayerID(playerId.asBSID)("participants.$")

      // get just the first contest
      symbols = JsArray(value = contests flatMap { contest =>

        // get the first participant
        val participants = (contest \ "participants" match {
          case JsArray(players) => players.headOption
          case js => Some(js)
        }) getOrElse JS()

        // get the symbol for each position
        val positions = participants \ "positions"

        // extract the sequence of symbols
        positions \\ "symbol"
      })

    } yield symbols

    outcome map (Ok(_))
  }

  /**
   * Returns a trading clock state object
   */
  def getMyContests(userName: String) = Action.async {
    Contests.findContestsByPlayerName(userName)(DisplayColumns: _*) map (list => Ok(JsArray(list)))
  }

  def sendChatMessage(contestId: String, sender: String) = Action.async { implicit request =>
    request.body.asJson flatMap (_.asOpt[String]) match {
      case Some(text) =>
        Contests.sendMessage(BSONObjectID(contestId), sender, text) map {
          case Some(messages) => Ok(JsArray(Seq(messages)))
          case None => Ok(JsArray())
        }
      case None =>
        Future.successful(Ok(JS("status" -> "error", "message" -> "No message sent")))
    }
  }

  private def extractParticipantByID(playerId: BSONObjectID, participants: JsValue): Option[JsValue] = {
    participants match {
      case JsArray(players) => players.find(p => (p \ "_id").asOpt[BSONObjectID] == Some(playerId))
      case _ => None
    }
  }

  private def extractOrderByID(orderId: BSONObjectID, orders: JsValue): Option[JsValue] = {
    orders match {
      case JsArray(myOrders) => myOrders.find(o => (o \ "_id").asOpt[BSONObjectID] == Some(orderId))
      case _ => None
    }
  }

  private def produceRankings(contestJson: JsObject): Future[JsArray] = {
    for {
    // compute the total equity for each player
      rankings <- produceNetWorths(contestJson)

      // sort the participants by net-worth
      rankedPlayers = (1 to rankings.size).toSeq zip rankings.sortBy(-_.totalEquity)

    } yield JsArray(rankedPlayers map {
      case (place, p) =>
        import p._
        JS("name" -> name,
          "facebookID" -> facebookID,
          "score" -> score,
          "totalEquity" -> totalEquity,
          "gainLoss" -> gainLoss_%,
          "rank" -> placeName(place))
    })
  }

  private def produceNetWorths(contestJson: JsObject): Future[Seq[Ranking]] = {
    // get the contest's values
    val startingBalance = (contestJson \ "startingBalance").asOpt[Double]
    val participants = (contestJson \ "participants").asInstanceOf[JsArray]
    val allSymbols = (participants \\ "symbol") flatMap (_.asOpt[String])

    for {
    // query the quotes for all symbols
      quotes <- QuoteResources.findQuotesBySymbols(allSymbols)

      // create the mapping of symbols to quotes
      mapping = Map(quotes map (q => (q.symbol.getOrElse(""), q)): _*)

      // get the participants' net worth and P&L
      totalWorths = participants.value flatMap (asRanking(startingBalance, mapping, _))

    // return the players' total worth
    } yield totalWorths
  }

  private def enrichParticipant(playerJs: JsValue): Future[JsObject] = {
    // get the positions and associated symbols
    val positions = playerJs \ "positions"
    val symbols = ((positions \\ "symbol") flatMap (_.asOpt[String])).distinct

    for {
    // load the quotes for all position symbols
      quotesJs <- StockQuotes.findQuotes(symbols)("symbol", "lastTrade")

      // build a mapping of symbol to last trade
      quotes = Map(quotesJs flatMap { js =>
        for {symbol <- (js \ "symbol").asOpt[String]; lastTrade <- (js \ "lastTrade").asOpt[Double]} yield (symbol, lastTrade)
      }: _*)

      // enrich the positions
      enrichedPositions = positions match {
        case JsArray(psa) =>
          val prices = psa flatMap { ps =>
            for {
              symbol <- (ps \ "symbol").asOpt[String]
              market <- quotes.get(symbol)
              qty <- (ps \ "quantity").asOpt[Double]
              cost <- (ps \ "cost").asOpt[Double]
              netValue = market * qty
              gainLoss = netValue - cost
            } yield JS("lastTrade" -> market, "netValue" -> netValue, "gainLoss" -> gainLoss) ++ ps.asInstanceOf[JsObject]
          }
          prices
        case _ => Seq.empty
      }

      // re-insert into the participant object
      enrichedPlayer = playerJs.asInstanceOf[JsObject] ++ JS("positions" -> enrichedPositions)

      _ = Logger.info(s"enrichedPositions = $enrichedPositions")

    } yield enrichedPlayer
  }

  private def asRanking(startingBalance: Option[Double], mapping: Map[String, Quote], p: JsValue) = {
    for {
      startingBal <- startingBalance
      name <- (p \ "name").asOpt[String]
      facebookID = (p \ "facebookID").asOpt[String]
      score <- (p \ "score").asOpt[Int]
      symbols = ((p \ "positions") \\ "symbol") flatMap (_.asOpt[String])
      fundsAvailable <- (p \ "fundsAvailable").asOpt[Double]
      investment = (symbols flatMap (s => mapping.get(s) flatMap (_.lastTrade))).sum
      totalEquity = fundsAvailable + investment
      gainLoss_% = ((totalEquity - startingBal) / startingBal) * 100d
    } yield Ranking(name, facebookID, score, totalEquity, gainLoss_%)
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
                         acquaintances: Option[Boolean],
                         friendsOnly: Option[Boolean],
                         invitationOnly: Option[Boolean],
                         perksAllowed: Option[Boolean],
                         ranked: Option[Boolean])

  implicit val contestFormReads: Reads[ContestForm] = (
    (__ \ "name").read[String] and
      (__ \ "player" \ "id").read[String] and
      (__ \ "player" \ "name").read[String] and
      (__ \ "player" \ "facebookId").read[String] and
      (__ \ "acquaintances").readNullable[Boolean] and
      (__ \ "friendsOnly").readNullable[Boolean] and
      (__ \ "invitationOnly").readNullable[Boolean] and
      (__ \ "perksAllowed").readNullable[Boolean] and
      (__ \ "ranked").readNullable[Boolean])(ContestForm.apply _)

  case class Ranking(name: String, facebookID: Option[String], score: Int, totalEquity: Double, gainLoss_% : Double)

}