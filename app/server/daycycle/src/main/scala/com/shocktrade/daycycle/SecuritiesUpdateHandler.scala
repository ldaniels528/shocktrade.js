package com.shocktrade.daycycle

import com.shocktrade.common.dao.securities.SecurityRef
import com.shocktrade.concurrent.ConcurrentProcessor.{ConcurrentContext, TaskHandler}
import com.shocktrade.daycycle.SecuritiesUpdateHandler.Outcome
import com.shocktrade.services.LoggerFactory.Logger
import org.scalajs.nodejs.mongodb.UpdateWriteOpResultObject

import scala.concurrent.Future

/**
  * Securities Update Handler
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait SecuritiesUpdateHandler extends TaskHandler[SecurityRef, UpdateWriteOpResultObject, Outcome] {
  var successes = 0
  var failures = 0

  def requested: Int

  def logger: Logger

  @inline def processed = successes + failures

  override def onNext(ctx: ConcurrentContext[SecurityRef], security: SecurityRef) = updateSecurity(security)

  override def onSuccess(ctx: ConcurrentContext[SecurityRef], result: UpdateWriteOpResultObject) = {
    successes += 1
    val count = processed
    if (count % 100 == 0 || count == requested) {
      val completion = (100.0 * processed / requested).toInt
      logger.info(s"Processed $count securities (completion: $completion%, successes: $successes, failures: $failures)")
    }
  }

  override def onFailure(ctx: ConcurrentContext[SecurityRef], cause: Throwable) = failures += 1

  override def onComplete(ctx: ConcurrentContext[SecurityRef]) = Outcome(processed, successes, failures)

  def updateSecurity(security: SecurityRef): Future[UpdateWriteOpResultObject]

}

/**
  * Securities Update Handler Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object SecuritiesUpdateHandler {

  /**
    * Processing outcome
    */
  case class Outcome(processed: Int, successes: Int, failures: Int)

}