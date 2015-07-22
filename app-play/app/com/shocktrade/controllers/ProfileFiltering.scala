package com.shocktrade.controllers

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument => BS, BSONObjectID}

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
  def mcP: BSONCollection

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
          profile_? <- mcP.find(BS("_id" -> BSONObjectID(userID)), BS("exchanges" -> 1))
            .cursor[BS]()
            .headOption

          // attempt to extract the exchanges
          exchanges = (for {
            profile <- profile_?
            exchanges <- profile.getAs[Seq[String]]("exchanges")
          } yield exchanges) getOrElse Nil
        } yield exchanges
    }
  }

}