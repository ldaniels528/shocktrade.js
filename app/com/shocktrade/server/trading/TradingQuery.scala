package com.shocktrade.server.trading

import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONDocument => BS}
import reactivemongo.core.commands.LastError

import scala.concurrent.{ExecutionContext, Future}
import scala.language.{implicitConversions, postfixOps}

/**
 * Trading Query
 * @author lawrence.daniels@gmail.com
 */
case class TradingQuery(comps: (BS, String)*) {

  def execute(update: BS)(implicit mc: BSONCollection, ec: ExecutionContext): Future[Int] = {
    for {
      result <- mc.update(getQuery, update, upsert = false, multi = false)
      outcome <- handlerResult(result)
    } yield outcome
  }

  def getQuery: BS = comps.foldLeft[BS](BS()) { case (obj, (q, msg)) => obj ++ q }

  private def findFailedComponent(implicit mc: BSONCollection, ec: ExecutionContext): Future[Int] = {
    Future.sequence {
      comps.map { case (comp, message) =>
        mc.find(comp, BS("_id" -> 1)).cursor[BS].headOption map {
          case Some(rec) => 1
          case None => throw new IllegalArgumentException(message)
        }
      }
    } map (_ sum)
  }

  private def handlerResult(result: LastError)(implicit mc: BSONCollection, ec: ExecutionContext): Future[Int] = {
    if (result.inError) {
      findFailedComponent
    } else {
      Future.successful(result.n)
    }
  }

}
