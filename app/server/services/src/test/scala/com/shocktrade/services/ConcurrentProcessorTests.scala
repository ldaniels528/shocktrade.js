package com.shocktrade.services

import java.util.UUID

import com.shocktrade.concurrent.{ConcurrentContext, ConcurrentProcessor, ConcurrentTaskHandler}
import com.shocktrade.serverside.LoggerFactory
import com.shocktrade.serverside.LoggerFactory.Logger
import org.scalajs.nodejs.console
import utest._

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

/**
  * Concurrent Processor Tests
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ConcurrentProcessorTests extends TestSuite {
  private implicit val logger = LoggerFactory.getLogger(getClass)
  private val processor = new ConcurrentProcessor()

  override val tests = this {
    "concurrent processes data" - {
      val dataSet = js.Array((1 to 100).map(_ => UUID.randomUUID()): _*)
      processor.start(queue = dataSet, ctx = ConcurrentContext(concurrency = 5), handler = new ConcurrentTaskHandler[UUID, String, Int] {

        override def onNext(ctx: ConcurrentContext, item: UUID) = {
          Future.successful(item.toString)
        }

        override def onSuccess(ctx: ConcurrentContext, result: String)(implicit logger: Logger) = {
          console.log(s"result: $result")
        }

        override def onFailure(ctx: ConcurrentContext, cause: Throwable) = {
          console.error(s"error: ${cause.getMessage}")
        }

        override def onComplete(ctx: ConcurrentContext) = {
          console.log("Done")
          100
        }
      })

      1
    }
  }

  tests.runAsync() map { results =>
    console.log(s"results: $results")
    results
  }

}
