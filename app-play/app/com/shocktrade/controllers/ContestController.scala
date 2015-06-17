package com.shocktrade.controllers

import java.util.Date

import akka.util.Timeout
import com.ldaniels528.commons.helpers.OptionHelper._
import com.ldaniels528.tabular.Tabular
import com.shocktrade.actors.WebSockets
import com.shocktrade.actors.WebSockets.UserProfileUpdated
import com.shocktrade.controllers.ContestControllerForms._
import com.shocktrade.controllers.QuotesController.Quote
import com.shocktrade.models.contest.{PlayerRef, _}
import com.shocktrade.models.profile.UserProfiles
import com.shocktrade.models.quote.StockQuotes
import com.shocktrade.server.trading.{Contests, OrderProcessor}
import com.shocktrade.util.BSONHelper._
import org.joda.time.DateTime
import play.api._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.json.BSONFormats._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Success, Try}

/**
 * Contest Controller
 * @author lawrence.daniels@gmail.com
 */
object ContestController extends Controller with ErrorHandler {
  private val tabular = new Tabular()
  private val DisplayColumns = Seq(
    "name", "creator", "startTime", "expirationTime", "startingBalance", "status",
    "ranked", "playerCount", "levelCap", "perksAllowed", "maxParticipants",
    "participants._id", "participants.name", "participants.facebookID")

  implicit val timeout: Timeout = 20.seconds

  ////////////////////////////////////////////////////////////////////////////
  //      API functions
  ////////////////////////////////////////////////////////////////////////////

  /**
   * Cancels the specified order
   * @param contestId the given contest ID
   * @param playerId the given player ID
   * @param orderId the given order ID
   */
  def cancelOrder(contestId: String, playerId: String, orderId: String) = Action.async {
    // pull the order, add it to closedOrders, and return the participant
    Contests.closeOrder(contestId.toBSID, playerId.toBSID, orderId.toBSID)("participants.name", "participants.orders", "participants.closedOrders")
      .map(_.orDie(s"Order $orderId could not be canceled"))
      .map(contest => Ok(Json.toJson(contest)))
      .recover { case e: Exception => Ok(createError(e)) }
  }

  def closeContest(contestId: String) = Action.async {
    val outcome = for {
      contest <- Contests.findContestByID(contestId.toBSID)() map (_ orDie "Contest not found")
      result <- OrderProcessor.closeContest(contest, contest.asOfDate.getOrElse(new Date()))
    } yield result

    outcome map { case (liquidations, contest) =>
      Ok(JS("name" -> contest.name, "count" -> liquidations.size))
    }
  }

  /**
   * Performs a search for contests
   * @return a JSON array of [[Contest]] instances
   */
  def contestSearch = Action.async { implicit request =>
    Try(request.body.asJson map (_.as[ContestSearchForm])) match {
      case Success(Some(form)) =>
        val searchOptions = SearchOptions(
          activeOnly = form.activeOnly,
          available = form.available,
          friendsOnly = form.friendsOnly,
          levelCap = for {allowed <- form.levelCapAllowed; cap <- form.levelCap if allowed} yield cap,
          perksAllowed = form.perksAllowed,
          robotsAllowed = form.robotsAllowed)
        Contests.findContests(searchOptions)() map (contests => Ok(Json.toJson(contests))) recover {
          case e: Exception => Ok(createError(e))
        }
      case Success(None) =>
        Future.successful(BadRequest("Search options were expected as JSON body"))
      case Failure(e) =>
        Logger.error(s"${e.getMessage}: json = ${request.body.asJson.orNull}")
        Future.successful(InternalServerError(e.getMessage))
    }
  }

  def createChatMessage(contestId: String) = Action.async { implicit request =>
    request.body.asJson map (_.as[MessageForm]) match {
      case Some(form) =>
        val message = Message(sender = form.sender, recipient = form.recipient, text = form.text)
        Contests.createMessage(contestId.toBSID, message)("messages") map {
          case Some(contest) => Ok(Json.toJson(contest.messages))
          case None => Ok(JsArray())
        } recover {
          case e: Exception =>
            Logger.error(s"${e.getMessage}: json = ${request.body.asJson.orNull}")
            Ok(createError(e))
        }
      case None =>
        Future.successful(Ok(createError("No message sent")))
    }
  }

  /**
   * Creates a new contest
   */
  def createContest = Action.async { implicit request =>
    Try(request.body.asJson.map(_.as[ContestCreateForm])) match {
      case Success(Some(form)) =>
        val outcome = for {
        // deduct the buy-in cost from the profile
          profile <- UserProfiles.deductFunds(form.playerId.toBSID, form.startingBalance) map (_ orDie "Insufficient funds")
          newContest = makeContest(form)

          // create the contest
          lastError <- Contests.createContest(newContest)
        } yield (newContest, lastError)

        outcome map { case (newContest, lastError) =>
          Ok(Json.toJson(newContest))
        } recover {
          case e: Exception => Ok(createError(e))
        }
      case Success(None) =>
        Future.successful(BadRequest("Contest form was expected as JSON body"))
      case Failure(e) =>
        Logger.error(s"${e.getMessage}: json = ${request.body.asJson.orNull}")
        Future.successful(InternalServerError(e.getMessage))
    }
  }

  def deleteContestByID(contestId: String) = Action.async {
    val outcome = for {
    // retrieve the contest
      contest <- Contests.findContestByID(contestId.toBSID)() map (_ orDie "Contest not found")

      // update each participant
      updatedParticipants <- Future.sequence(contest.participants map { participant =>
        UserProfiles.deductFunds(participant.id, -participant.cashAccount.cashFunds) map (_ orDie "Failed to refund game cash")
      })

      // delete the contest
      lastError <- Contests.deleteContestByID(contestId.toBSID)
    } yield lastError

    outcome map { lastError =>
      Ok(if (lastError.inError) JS("error" -> lastError.message) else JS())
    } recover {
      case e: Exception => Ok(createError(e))
    }
  }

  def createMarginAccount(contestId: String, playerId: String) = Action.async {
    Contests.createMarginAccount(contestId.toBSID, playerId.toBSID, MarginAccount()) map {
      case Some(contest) => Ok(Json.toJson(contest))
      case None => Ok(JS("error" -> "Game or player not found"))
    } recover {
      case e: Exception => Ok(createError(e))
    }
  }

  def transferFundsBetweenAccounts(contestId: String, playerId: String) = Action.async { implicit request =>
    Try(request.body.asJson.map(_.as[TransferFundsForm])) match {
      case Success(Some(form)) =>
        // perform the atomic update
        Contests.transferFundsBetweenAccounts(contestId.toBSID, playerId.toBSID, form.source, form.amount) map {
          case Some(contest) => Ok(Json.toJson(contest))
          case None => Ok(JS("error" -> "Game or player not found"))
        } recover {
          case e: Exception => Ok(createError(e))
        }
      case Success(None) =>
        Logger.error(s"adjustMarginAccountFunds: Bad request -> json = ${request.body.asJson.orNull}")
        Future.successful(BadRequest("JSON body expected"))
      case Failure(e) =>
        Logger.error(s"adjustMarginAccountFunds: Internal server error -> json = ${request.body.asJson.orNull}", e)
        Future.successful(InternalServerError(e.getMessage))
    }
  }

  def getMarginMarketValue(contestId: String, playerId: String) = Action.async {
    val outcome = for {
      contest <- Contests.findContestByID(contestId.toBSID)() map (_ orDie "Game not found")
      participant = contest.participants.find(_.id == playerId.toBSID) orDie "Player not found"
      positions = participant.positions.filter(_.accountType == AccountTypes.MARGIN)
      marketValue <- computeMarketValue(positions)
    } yield (contest, participant, marketValue)

    outcome map { case (contest, participant, marketValue) =>
      Ok(JS("name" -> contest.name, "_id" -> contest.id, "marginMarketValue" -> marketValue))
    }
  }

  private def computeMarketValue(positions: Seq[Position]): Future[Double] = {
    val symbols = positions.map(_.symbol).distinct
    StockQuotes.findQuotes(symbols)("name", "symbol", "lastTrade", "close") map (_ flatMap (_.asOpt[MarketQuote])) map { quotes =>
      val mapping = Map(quotes.map(q => (q.symbol, q)): _*)
      (positions flatMap { pos =>
        for {
          quote <- mapping.get(pos.symbol)
          value <- quote.lastTrade ?? quote.close ?? Option(pos.pricePaid.toDouble)
        } yield value * pos.quantity
      }).sum
    }
  }

  private def makeContest(js: ContestCreateForm) = {
    // create a player instance
    val player = PlayerRef(id = js.playerId.toBSID, name = js.playerName, facebookId = js.facebookId)
    val startTime = if (js.startAutomatically.contains(true)) Some(new Date()) else None

    // create the contest
    Contest(
      name = js.name,
      creator = player,
      creationTime = new Date(),
      startingBalance = js.startingBalance,
      startTime = startTime,
      expirationTime = startTime.map(t => new DateTime(t).plusDays(js.duration).toDate),
      friendsOnly = js.friendsOnly,
      invitationOnly = js.invitationOnly,
      levelCap = (for {allowed <- js.levelCapAllowed; cap <- js.levelCap if allowed} yield cap) map (_.toInt),
      perksAllowed = js.perksAllowed,
      robotsAllowed = js.robotsAllowed,
      messages = List(Message(sender = player, text = s"Welcome to ${js.name}")),
      participants = List(Participant(id = js.playerId.toBSID, js.playerName, js.facebookId, cashAccount = CashAccount(cashFunds = js.startingBalance)))
    )
  }

  /**
   * Creates a new order
   * @param contestId the given contest ID
   * @param playerId the given player ID
   * @return a [[Contest]] in JSON format
   */
  def createOrder(contestId: String, playerId: String) = Action.async { implicit request =>
    Try(request.body.asJson.map(_.as[OrderForm])) match {
      case Success(Some(form)) =>
        Contests.createOrder(contestId.toBSID, playerId.toBSID, makeOrder(playerId, form)) map {
          case Some(contest) => Ok(Json.toJson(contest))
          case None => Ok(createError(s"Contest $contestId not found"))
        } recover {
          case e: Exception => Ok(createError(e))
        }
      case Success(None) =>
        Future.successful(BadRequest("No order information"))
      case Failure(e) =>
        Logger.error(s"Error parsing JSON: json = ${request.body.asJson.orNull}", e)
        Future.successful(BadRequest("Invalid JSON body"))
    }
  }

  private def makeOrder(playerId: String, form: OrderForm) = {
    Order(
      accountType = form.accountType,
      symbol = form.symbol,
      exchange = form.exchange,
      creationTime = if (playerId == "51a308ac50c70a97d375a6b2") new DateTime().minusDays(4).toDate else new Date(), // TODO for testing only
      orderTerm = form.orderTerm,
      orderType = form.orderType,
      price = form.limitPrice,
      priceType = form.priceType,
      quantity = form.quantity,
      commission = Commissions.getCommission(form.priceType, form.perks.getOrElse(Nil)),
      emailNotify = form.emailNotify,
      partialFulfillment = form.partialFulfillment
    )
  }

  def getContestByID(contestId: String) = Action.async {
    Contests.findContestByID(contestId.toBSID)() map {
      case Some(contest) => Ok(Json.toJson(contest))
      case None => Ok(createError(s"Contest $contestId not found"))
    } recover {
      case e: Exception => Ok(createError(e))
    }
  }

  def getContestRankings(contestId: String) = Action.async {
    (for {
      contest <- Contests.findContestByID(contestId.toBSID)() map (_ orDie s"Contest $contestId not found")
      rankings <- produceRankings(contest)
    } yield rankings) map (Ok(_)) recover {
      case e: Exception => Ok(createError(e))
    }
  }

  def getContestParticipant(contestId: String, playerId: String) = Action.async {
    (for {
      contest <- Contests.findContestByID(contestId.toBSID)() map (_ orDie s"Contest $contestId not found")
      player = contest.participants.find(_.id == playerId) orDie s"Player $playerId not found"
      enrichedPlayer <- enrichPositions(player)
    } yield enrichedPlayer).map(p => Ok(JsArray(Seq(p)))) recover {
      case e: Exception => Ok(createError(e))
    }
  }

  /**
   * Returns a trading clock state object
   */
  def getContestsByPlayerID(playerId: String) = Action.async {
    Contests.findContestsByPlayerID(playerId.toBSID)(DisplayColumns: _*) map (contests => Ok(Json.toJson(contests)))
  }

  def getEnrichedOrders(contestId: String, playerId: String) = Action.async {
    val outcome = for {
      contest <- Contests.findContestByID(contestId.toBSID)() map (_ orDie "Contest not found")
      player = contest.participants.find(_.id == playerId.toBSID) orDie "Player not found"
      enriched <- enrichOrders(player)
    } yield enriched \ "orders"

    outcome map (js => Ok(js)) recover {
      case e: Exception => Ok(createError(e))
    }
  }

  def getEnrichedPositions(contestId: String, playerId: String) = Action.async {
    val outcome = for {
      contest <- Contests.findContestByID(contestId.toBSID)() map (_ orDie "Contest not found")
      player = contest.participants.find(_.id == playerId.toBSID) orDie "Player not found"
      enriched <- enrichPositions(player)
    } yield enriched \ "positions"

    outcome map (js => Ok(js)) recover {
      case e: Exception => Ok(createError(e))
    }
  }

  def getHeldSecurities(playerId: String) = Action.async {
    Contests.findContestsByPlayerID(playerId.toBSID)("participants.$") map {
      _.flatMap(_.participants.flatMap(_.positions.map(_.symbol)))
    } map (symbols => Ok(JsArray(symbols.distinct.map(JsString)))) recover {
      case e: Exception => Ok(createError(e))
    }
  }

  def getTotalInvestment(playerId: String) = Action.async {
    val outcome = for {
    // calculate the symbol-quantity tuples
      quantities <- Contests.findContestsByPlayerID(playerId.toBSID)("participants.$") map (
        _.flatMap(_.participants.flatMap(_.positions.map(p => (p.symbol, p.quantity)))))

      // load the quotes for all order symbols
      symbols = quantities.map(_._1)
      quotesJs <- StockQuotes.findQuotes(symbols)("name", "symbol", "lastTrade")

      // build a mapping of symbol to last trade
      quotes = Map(quotesJs map (_.as[QuoteSnapshot]) map (q => (q.symbol, q)): _*)

      // compute the total net worth
      netWorth = (quantities flatMap { case (symbol, quantity) => quotes.get(symbol).map(_.lastTrade * quantity) }).sum

    } yield netWorth

    outcome map { netWorth =>
      Ok(JS("netWorth" -> netWorth))
    } recover {
      case e: Exception => Ok(createError(e))
    }
  }

  def joinContest(contestId: String) = Action.async { implicit request =>
    Try(request.body.asJson map (_.as[JoinContestForm])) match {
      case Success(Some(js)) =>
        (for {
          startingBalance <- Contests.findContestByID(contestId.toBSID)() map (_ orDie "Contest not found") map (_.startingBalance)
          participant = Participant(id = js.playerId.toBSID, js.playerName, js.facebookId, CashAccount(cashFunds = startingBalance))
          userProfile <- UserProfiles.deductFunds(participant.id, startingBalance) map (_ orDie "Insufficient funds")
          contest <- Contests.joinContest(contestId.toBSID, participant)
        } yield (userProfile, contest)) map { case (userProfile, contest_?) =>
          WebSockets ! UserProfileUpdated(userProfile)
          Ok(Json.toJson(contest_?))

        } recover {
          case e: Exception => Ok(createError(e))
        }
      case Success(None) => Future.successful(Ok(JS("error" -> "Internal error")))
      case Failure(e) =>
        Logger.error("Contest Join JSON parsing failed", e)
        Future.successful(Ok(createError("Internal error")))
    }
  }

  def quitContest(contestId: String, playerId: String) = Action.async { implicit request =>
    (for {
      c <- Contests.findContestByID(contestId.toBSID)() map (_ orDie "Contest not found")
      p = c.participants.find(_.id.stringify == playerId) orDie "Player not found"
      u <- UserProfiles.deductFunds(playerId.toBSID, -p.cashAccount.cashFunds)
      updatedContest <- Contests.quitContest(contestId.toBSID, playerId.toBSID)
    } yield (u, updatedContest)) map { case (profile_?, contest_?) =>
      profile_?.foreach(WebSockets ! UserProfileUpdated(_))
      contest_? match {
        case Some(contest) => Ok(Json.toJson(contest))
        case None => Ok(createError("Contest not found"))
      }
    } recover {
      case e: Exception => Ok(createError(e))
    }
  }

  def startContest(contestId: String) = Action.async {
    Contests.startContest(contestId.toBSID, startTime = new Date()) map {
      case Some(contest) => Ok(Json.toJson(contest))
      case None => Ok(createError("No qualifying contest found"))
    } recover {
      case e: Exception => Ok(createError(e))
    }
  }

  def updateProcessingHost(contestId: String) = Action.async { implicit request =>
    val host = request.body.asJson flatMap (js => (js \ "host").asOpt[String])
    Contests.updateProcessingHost(contestId.toBSID, host) map (_ => Ok(JS()))
  }

  def getAvailablePerks(contestId: String) = Action.async {
    Contests.findAvailablePerks(contestId.toBSID) map (perks => Ok(Json.toJson(perks)))
  }

  def getPlayerPerks(id: String, playerId: String) = Action.async {
    // retrieve the participant
    val result = for {
      contest_? <- Contests.findContestByID(id.toBSID)()
      participant_? = for {
        contest <- contest_?
        participant <- contest.participants.find(_.id.stringify == playerId)
      } yield participant
    } yield participant_?

    result map {
      case Some(participant) =>
        Ok(JS("perks" -> participant.perks, "fundsAvailable" -> participant.cashAccount.cashFunds))
      case None =>
        Ok(JS("error" -> "Perks could not be retrieved"))
    } recover {
      case e =>
        Logger.error("Perks could not be retrieved", e)
        Ok(JS("error" -> "Perks could not be retrieved"))
    }
  }

  /**
   * Facilitates the purchase of perks
   * Returns the updated perks (e.g. ['CREATOR', 'PRCHEMNT'])
   */
  def purchasePerks(contestId: String, playerId: String) = Action.async { request =>
    // get the perks from the request body
    request.body.asJson map (_.as[Seq[String]]) match {
      case Some(perkCodeIDs) =>
        val perkCodes = perkCodeIDs.map(PerkTypes.withName)
        val result = for {
        // retrieve the mapping of perk codes to perk costs
          perkCostsByCode <- Contests.findAvailablePerks(contestId.toBSID) map (perks => Map(perks map (p => (p.code, p.cost)): _*))

          // compute the total cost of the perks
          totalCost = (perkCodes flatMap perkCostsByCode.get).sum

          // perform the purchase
          perks_? <- Contests.purchasePerks(contestId.toBSID, playerId.toBSID, perkCodes, totalCost)

          // was a margin account purchased?
          margin_? <- {
            if (perkCodes.contains(PerkTypes.MARGIN))
              Contests.createMarginAccount(contestId.toBSID, playerId.toBSID, MarginAccount())
            else
              Future.successful(None)
          }

        } yield margin_? ?? perks_?

        result.map {
          case Some(contest) =>
            Ok(Json.toJson(contest))
          case None =>
            Ok(JS("error" -> "Perks could not be purchased"))
        } recover {
          case e => Ok(JS("error" -> "Perks could not be purchased"))
        }
      case _ =>
        Future.successful(BadRequest("JSON array of Perk codes expected"))
    }
  }

  private def produceRankings(contest: Contest): Future[JsArray] = {
    for {
    // compute the total equity for each player
      rankings <- produceNetWorths(contest)

      // sort the participants by net-worth
      rankedPlayers = (1 to rankings.size).toSeq zip rankings.sortBy(-_.totalEquity)

    } yield JsArray(rankedPlayers map { case (place, p) => JS("rank" -> placeName(place)) ++ Json.toJson(p).asInstanceOf[JsObject] })
  }

  private def produceNetWorths(contest: Contest): Future[Seq[Ranking]] = {
    // get the contest's values
    val startingBalance = contest.startingBalance
    val participants = contest.participants
    val allSymbols = participants.flatMap(_.positions.map(_.symbol))

    for {
    // query the quotes for all symbols
      quotes <- QuotesController.findQuotesBySymbols(allSymbols)
      //_ = tabular.transform(quotes) foreach (s => Logger.info(s))

      // create the mapping of symbols to quotes
      mapping = Map(quotes map (q => (q.symbol, q)): _*)

      // get the participants' net worth and P&L
      totalWorths = participants map (asRanking(startingBalance, mapping, _))

      _ = tabular.transform(totalWorths) foreach (s => Logger.info(s))

    // return the players' total worth
    } yield totalWorths
  }

  private def enrichOrders(player: Participant): Future[JsObject] = {
    // get the orders and associated symbols
    val symbols = player.orders.map(_.symbol).distinct

    for {
    // load the quotes for all order symbols
      quotesJs <- StockQuotes.findQuotes(symbols)("name", "symbol", "lastTrade")

      // build a mapping of symbol to last trade
      quotes = Map(quotesJs map (_.as[QuoteSnapshot]) map (q => (q.symbol, q)): _*)

      // enrich the orders
      enrichedOrders = player.orders flatMap { order =>
        for {
          quote <- quotes.get(order.symbol)
        } yield Json.toJson(order).asInstanceOf[JsObject] ++ JS(
          "companyName" -> quote.name,
          "lastTrade" -> quote.lastTrade)
      }

    // re-insert into the participant object
    } yield Json.toJson(player).asInstanceOf[JsObject] ++ JS("orders" -> JsArray(enrichedOrders))
  }

  private def enrichPositions(player: Participant): Future[JsObject] = {
    // get the positions and associated symbols
    val symbols = player.positions.map(_.symbol).distinct

    for {
    // load the quotes for all position symbols
      quotesJs <- StockQuotes.findQuotes(symbols)("name", "symbol", "lastTrade")

      // build a mapping of symbol to last trade
      quotes = Map(quotesJs map (_.as[QuoteSnapshot]) map (q => (q.symbol, q)): _*)

      // enrich the positions
      enrichedPositions = player.positions flatMap { pos =>
        for {
          quote <- quotes.get(pos.symbol)
          netValue = quote.lastTrade * pos.quantity
          gainLoss = netValue - pos.cost
          gainLossPct = 100d * (gainLoss / pos.cost)
        } yield Json.toJson(pos).asInstanceOf[JsObject] ++ JS(
          "companyName" -> quote.name,
          "cost" -> pos.cost,
          "lastTrade" -> quote.lastTrade,
          "netValue" -> netValue,
          "gainLoss" -> gainLoss,
          "gainLossPct" -> gainLossPct)
      }

    // re-insert into the participant object
    } yield Json.toJson(player).asInstanceOf[JsObject] ++ JS("positions" -> JsArray(enrichedPositions))
  }

  private def asRanking(startingBalance: BigDecimal, mapping: Map[String, Quote], p: Participant) = {

    def computeInvestment(positions: Seq[Position]) = {
      positions flatMap { p =>
        for {
          quote <- mapping.get(p.symbol)
          lastTrade <- quote.lastTrade ?? Some(p.pricePaid.toDouble)
        } yield lastTrade * p.quantity
      } sum
    }

    // compute the investment for all positions
    val (cashPositions, marginPositions) = p.positions.partition(_.accountType == AccountTypes.CASH)
    val investment = computeInvestment(cashPositions) + (computeInvestment(marginPositions) * MarginAccount.InitialMargin)

    // add it all up and generate the ranking
    val marginFunds = p.marginAccount.map(_.cashFunds).getOrElse(BigDecimal(0.0d))
    val totalEquity = p.cashAccount.cashFunds + investment + marginFunds
    val gainLoss_% = ((totalEquity - startingBalance) / startingBalance) * 100d
    Ranking(p.id, p.name, p.facebookId, totalEquity, gainLoss_%)
  }

  private def placeName(place: Int) = {
    place match {
      case 1 => "1st"
      case 2 => "2nd"
      case 3 => "3rd"
      case n => s"${n}th"
    }
  }

  implicit def Option2Boolean[T](option: Option[T]): Boolean = option.isDefined

}