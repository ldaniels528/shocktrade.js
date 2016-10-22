package com.shocktrade.server.dao
package securities

import com.shocktrade.common.models.contest.OrderLike
import org.scalajs.nodejs.mongodb._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Intra-Day Quotes DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait IntraDayQuotesDAO extends Collection

/**
  * Intra-Day Quotes DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object IntraDayQuotesDAO {

  /**
    * Intra-Day Quotes DAO Extensions
    * @param dao the given [[IntraDayQuotesDAO Intra-Day Quotes DAO]]
    */
  implicit class IntraDayQuotesDAOExtensions(val dao: IntraDayQuotesDAO) {

    @inline
    def findMatch(order: OrderLike)(implicit ec: ExecutionContext) = {
      val query = doc(Seq(
        Option("symbol" $eq order.symbol),
        Option("tradeDateTime" between(order.creationTime, new js.Date())),
        Option("aggregateVolume" $gte order.quantity),
        // pricing
        if (order.isLimitOrder) Option(if (order.isBuyOrder) "price" $lte order.price else "price" $gte order.price)
        else None
      ).flatten: _*)

      dao.findOneFuture[IntraDayQuoteData](query)
    }

    @inline
    def findQuotes(symbol: String, startTime: js.Date, endTime: js.Date)(implicit ec: ExecutionContext) = {
      dao.find(doc("symbol" $eq symbol, "tradeDateTime" between(startTime, endTime))).toArrayFuture[IntraDayQuoteData]
    }

    @inline
    def saveQuotes(quotes: Seq[IntraDayQuoteData])(implicit ec: ExecutionContext) = {
      dao.bulkWrite(
        js.Array(quotes map { q =>
          insertOne(
            document = doc(
              "symbol" -> q.symbol,
              "price" -> q.price,
              "time" -> q.time,
              "volume" -> q.volume,
              "aggregateVolume" -> q.aggregateVolume,
              "tradeDateTime" -> q.tradeDateTime,
              "creationTime" -> q.creationTime))
        }: _*
        ))
    }

  }

  /**
    * Intra-Day Quotes DAO Constructor
    * @param db the given [[Db database]]
    */
  implicit class IntraDayQuotesDAOConstructor(val db: Db) extends AnyVal {

    @inline
    def getIntraDayQuotesDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("IntraDayQuotes").mapTo[IntraDayQuotesDAO]
    }
  }

}