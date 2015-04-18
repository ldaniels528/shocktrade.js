package com.shocktrade.controllers

import play.api.libs.json.Json.{obj => JS}
import play.api.libs.json._
import play.modules.reactivemongo.json.BSONFormats._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID

import scala.concurrent.{ExecutionContext, Future}

/**
 * Profile Filtering Capability
 * @author lawrence.daniels@gmail.com
 */
trait ProfileFiltering {
  val EXCHANGES = Seq("AMEX", "NASDAQ", "NYSE", "OTCBB", "OTHER_OTC")

  /**
   * Players collection
   */
  def mcP: JSONCollection

  /**
   * Retrieves the exchanges for the given player
   * @param playerID the user ID that represents the player
   * @return a [[Future]] of a sequence of exchanges (strings)
   */
  def findStockExchanges(playerID: Option[String])(implicit ec: ExecutionContext): Future[Seq[String]] = {
    playerID match {
      case None => Future.successful(EXCHANGES)
      case Some(userID) =>
        for {
        // attempt to retrieve the user's profile
          profile_? <- mcP.find(JS("_id" -> BSONObjectID(userID)), JS("exchanges" -> 1))
            .cursor[JsObject]
            .headOption

          // attempt to extract the exchanges
          exchanges = (for {
            profile <- profile_?
            exchanges = profile \ "exchanges" match {
              case JsArray(seq) => seq flatMap (_.asOpt[String])
              case js => EXCHANGES
            }
          } yield exchanges) getOrElse Seq.empty
        } yield exchanges
    }
  }

}