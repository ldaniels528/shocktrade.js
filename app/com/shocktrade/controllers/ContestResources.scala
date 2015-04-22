package com.shocktrade.controllers

import java.util.Date

import akka.util.Timeout
import com.ldaniels528.commons.helpers.OptionHelper._
import com.shocktrade.actors.Contests
import com.shocktrade.actors.Contests._
import com.shocktrade.controllers.QuoteResources.Quote
import com.shocktrade.models.contest.{Contest, Participant}
import com.shocktrade.util.BSONHelper._
import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax._
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONObjectID}

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * Contest Resources
 * @author lawrence.daniels@gmail.com
 */
object ContestResources extends Controller with MongoController with MongoExtras {
  val mcQ = db.collection[JSONCollection]("Stocks")
  val mcC = db.collection[JSONCollection]("Contests")
  implicit val timeout: Timeout = 15.seconds

  implicit val orderReads: Reads[Order] = (
    (JsPath \ "symbol").read[String] and
      (JsPath \ "limitPrice").read[Double] and
      (JsPath \ "quantity").read[Int] and
      (JsPath \ "orderType").read[String] and
      (JsPath \ "priceType").read[String] and
      (JsPath \ "orderTerm").read[String] and
      (JsPath \ "emailNotify").read[Boolean])(Order.apply _)

  implicit val searchOptionReads: Reads[SearchOptions] = (
    (JsPath \ "activeOnly").read[Boolean] and
      (JsPath \ "available").read[Boolean] and
      (JsPath \ "perksAllowed").read[Boolean] and
      (JsPath \ "friendsOnly").read[Boolean] and
      (JsPath \ "acquaintances").read[Boolean] and
      (JsPath \ "levelCap").read[String] and
      (JsPath \ "levelCapAllowed").read[Boolean])(SearchOptions.apply _)

  /**
   * Creates a new contest
   */
  def createNewContest = Action { implicit request =>
    val results = for {
      json <- request.body.asJson
      title <- (json \ "name").asOpt[String] map (_.trim)
      playerName <- (json \ "player" \ "name").asOpt[String]
      facebookId <- (json \ "player" \ "facebookId").asOpt[String]
      invitationOnly = (json \ "invitationOnly").asOpt[Boolean].getOrElse(false)
      ranked = (json \ "ranked").asOpt[Boolean].getOrElse(false)
      perksAllowed = (json \ "perksAllowed").asOpt[Boolean].getOrElse(false)
      acquaintances = (json \ "acquaintances").asOpt[Boolean].getOrElse(false)
    } yield (title, playerName, facebookId, invitationOnly, ranked, perksAllowed, acquaintances)

    results match {
      case Some((title, name, facebookId, invitationOnly, ranked, perksAllowed, acquaintances)) =>
        val contest = Contest(
          name = title,
          creationTime = new Date(),
          startingBalance = BigDecimal(25000d),
          modifiers = Contest.Modifiers(acquaintances = acquaintances, invitationOnly = invitationOnly, ranked = ranked, perksAllowed = perksAllowed)
        )
        val creator = Participant(name, facebookId, fundsAvailable = contest.startingBalance)
        Logger.info(s"contest = ${contest.copy(participants = List(creator))}")
        Contests ! CreateContest(contest.copy(participants = List(creator)))

        Ok(JS("name" -> contest.name, "status" -> contest.status.name))
      case None =>
        BadRequest("One or more required property is missing")
    }
  }

  def getContestByID(id: String) = Action.async {
    findContestByID(id) map {
      case Some(contest) => Ok(contest)
      case None => BadRequest(s"Contest $id not found")
    }
  }

  def getContestRankings(id: String) = Action.async {
    (for {
      contest <- findContestByID(id) map (_ orDie s"Contest $id not found")
      rankings <- produceRankings(contest)
    } yield rankings) map (Ok(_))
  }

  def getContentParticipant(id: String, playerName: String) = Action.async {
    (for {
      contest <- findContestByID(id) map (_ orDie s"Contest $id not found")
      player = extractParticipantByName(playerName, contest \ "participants") orDie s"Player $playerName not found"
      enrichedPlayer <- enrichParticipant(player)
    } yield JsArray(Seq(enrichedPlayer))).map(Ok(_))
  }

  def contestSearch = Action.async { implicit request =>
    request.body.asJson map (_.as[SearchOptions]) match {
      case Some(searchOptions) =>
        (Contests ? FindContests(searchOptions)).mapTo[JsArray].map(Ok(_))
      case None =>
        Future.successful(BadRequest("Search options expected"))
    }
  }

  def cancelOrder(contestId: String, playerName: String, orderId: String) = Action.async {
    Logger.info(s"cancelOrder - contest Id: $contestId, playerName: $playerName, orderId: $orderId")
    val promisedContest = (Contests ? FindOrderByID(contestId.asBSID, orderId.asBSID,
      Seq("participants.name", "participants.orders"))).mapTo[Option[JsObject]]

    (for {
    // retrieve the contest skeleton with orders only
    // db.Contests.find({ "participants.orders" : { $elemMatch : { _id : ObjectId("532fb6a4bca7c0f8efc20e49") } } })
      contestWithOrders <- promisedContest.map(_.orDie(s"Contest $contestId not found"))

      // find the player by name
      player = extractParticipantByName(playerName, contestWithOrders \ "participants")
        .orDie(s"Player '$playerName' not found")

      // find the order to cancel
      order = extractOrderByID(orderId, player \ "orders").map(_.asInstanceOf[JsObject])
        .orDie(s"Order ID $orderId not found") ++ JS("message" -> "Canceled")

      // pull the order, add it to orderHistory, and return the participant
      updatedContest <- (Contests ? CloseOrder(contestId.asBSID, playerName, orderId.asBSID, order,
        Seq("participants.name", "participants.orders", "participants.orderHistory"))).mapTo[Option[JsObject]]
        .map(_.getOrElse(die(s"Order #$orderId not be canceled")))

    // extract just the updated participant
    } yield extractParticipantByName(playerName, updatedContest \ "participants").getOrElse(JsNull)) map (Ok(_))
  }

  private def extractParticipantByName(playerName: String, participants: JsValue): Option[JsValue] = {
    participants match {
      case JsArray(players) => players.find(p => (p \ "name").asOpt[String] == Some(playerName))
      case _ => None
    }
  }

  private def extractOrderByID(orderId: String, orders: JsValue): Option[JsValue] = {
    orders match {
      case JsArray(myOrders) => myOrders.find(o => (o \ "id").asOpt[BSONObjectID] == Some(orderId.asBSID))
      case _ => None
    }
  }

  def createOrder = Action.async { implicit request =>
    try {
      val result = for {
        js <- request.body.asJson
        contestId = (js \ "contestId").asOpt[String] getOrElse missing("contestId")
        playerName = (js \ "playerName").asOpt[String] getOrElse missing("playerName")
        order = JS(
          "id" -> BSONObjectID.generate,
          "creationTime" -> new BSONDateTime(System.currentTimeMillis()),
          "orderTime" -> new BSONDateTime(System.currentTimeMillis()),
          "symbol" -> (js \ "symbol").asOpt[String].getOrElse(missing("symbol")),
          "exchange" -> (js \ "exchange").asOpt[String].getOrElse(missing("exchange")),
          "limitPrice" -> asDecimal(js \ "limitPrice").getOrElse(missing("limitPrice")),
          "quantity" -> asDecimal(js \ "quantity").map(_.toInt).getOrElse(missing("quantity")),
          "orderType" -> (js \ "orderType").asOpt[String].getOrElse(missing("orderType")),
          "priceType" -> (js \ "priceType").asOpt[String].getOrElse(missing("priceType")),
          "commision1" -> 9.99,
          "volumeAtOrderTime" -> asDecimal(js \ "volumeAtOrderTime").map(_.toLong).getOrElse(missing("volume")),
          "orderTerm" -> (js \ "orderTerm").asOpt[String].getOrElse(missing("orderTerm")),
          "emailNotify" -> (js \ "emailNotify").asOpt[Boolean].getOrElse(missing("emailNotify")))
      } yield {
          (Contests ? CreateOrder(contestId.asBSID, playerName, order,
            Seq("participants.name", "participants.orders", "participants.orderHistory"))).mapTo[Option[JsValue]] map {
            case Some(contestJs) =>
              extractParticipantByName(playerName, contestJs \ "participants") match {
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

  private def asDecimal(js: JsValue): Option[Double] = {
    js.asOpt[Double] match {
      case Some(v) => Some(v)
      case None => js.asOpt[String] map (_.toDouble)
    }
  }

  def getHeldSecurities(playerId: String) = Action.async {
    val outcome = for {
    // lookup the contests
      contests <- mcC.find(
        JS("participants._id" -> playerId.asBSID),
        JS("participants.$" -> 1)).cursor[JsObject].collect[Seq]()

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
    (Contests ? FindContestsByPlayer(userName)).mapTo[JsArray].map(Ok(_))
  }

  def sendChatMessage(contestId: String, sender: String) = Action.async { implicit request =>
    request.body.asJson flatMap (_.asOpt[String]) match {
      case Some(text) =>
        (Contests ? SendMessage(BSONObjectID(contestId), sender, text)).mapTo[Option[JsObject]].map {
          case Some(messages) => Ok(JsArray(Seq(messages)))
          case None => Ok(JsArray())
        }
      case None =>
        Future.successful(Ok(JS("status" -> "error", "message" -> "No message sent")))
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

  private def getChartDataResults(contestId: String, playerName: String) = {
    for {
    // lookup the contest by ID
      contest_? <- mcC.findOneOpt(contestId)
      contestJs = contest_?.getOrElse(die("Game not found"))

      // get the contest participant details
      rankings <- produceNetWorths(contestJs)

    } yield rankings
  }

  private def extractParticipant(playerName: String, outcome: Future[Option[BSONDocument]]): Future[Result] = {
    (outcome map (_.flatMap { bs =>
      val js = Json.toJson(bs)
      Logger.info(s"js => $js")
      (js \\ "participants") find (p => (p \ "name").asOpt[String] == Some(playerName))
    })).transform({
      case Some(player) => Ok(player)
      case None => Ok(JsNull)
    },
    e => throw e)
  }

  private def enrichParticipant(playerJs: JsValue): Future[JsObject] = {
    // get the positions and associated symbols
    val positions = playerJs \ "positions"
    val symbols = ((positions \\ "symbol") flatMap (_.asOpt[String])).distinct

    for {
    // load the quotes for all position symbols
      quotesJs <- mcQ.find(JS("symbol" -> JS("$in" -> symbols)), JS("symbol" -> 1, "lastTrade" -> 1)).cursor[JsObject].collect[Seq]()

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

  private def asRanking(startingBalance: Option[Double], mapping: Map[String, Quote], p: JsValue): Option[Ranking] = {
    for {
      startingBal <- startingBalance
      name <- (p \ "name").asOpt[String]
      facebookID = (p \ "facebookID").asOpt[String].orNull
      score <- (p \ "score").asOpt[Int]
      symbols = ((p \ "positions") \\ "symbol") flatMap (_.asOpt[String])
      fundsAvailable <- (p \ "fundsAvailable").asOpt[Double]
      investment = (symbols flatMap (s => mapping.get(s) flatMap (_.lastTrade))).sum
      totalEquity = fundsAvailable + investment
      gainLoss_% = ((totalEquity - startingBal) / startingBal) * 100d
    } yield Ranking(name, facebookID, score, totalEquity, gainLoss_%)
  }

  private def placeName(place: Int): String = {
    place match {
      case 1 => "1st"
      case 2 => "2nd"
      case 3 => "3rd"
      case n => s"${n}th"
    }
  }

  private def findContestByID(id: String) = (Contests ? FindContestByID(id.asBSID)).mapTo[Option[JsObject]]

  case class Order(symbol: String,
                   limitPrice: Double,
                   quantity: Int,
                   orderType: String,
                   priceType: String,
                   orderTerm: String,
                   emailNotify: Boolean)

  case class Ranking(name: String, facebookID: String, score: Int, totalEquity: Double, gainLoss_% : Double)

}