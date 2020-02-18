package com.shocktrade.services

import com.shocktrade.server.common.LoggerFactory
import com.shocktrade.server.concurrent.ConcurrentProcessor
//import utest._

/**
  * Concurrent Processor Tests
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ConcurrentProcessorTests /*extends TestSuite*/ {
  private implicit val logger = LoggerFactory.getLogger(getClass)
  private val processor = new ConcurrentProcessor()
/*
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
  }*/

}
