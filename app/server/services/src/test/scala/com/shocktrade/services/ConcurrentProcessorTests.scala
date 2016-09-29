package com.shocktrade.services

import java.util.UUID

import com.shocktrade.concurrent.ConcurrentProcessor
import com.shocktrade.concurrent.ConcurrentProcessor.ConcurrentTaskHandler
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
  private val processor = new ConcurrentProcessor()

  override val tests = this {
    "concurrent processes data" - {
      val dataSet = js.Array((1 to 100).map(_ => UUID.randomUUID()): _*)
      processor.start(dataSet, new ConcurrentTaskHandler[UUID, String, Int] {

        override def onNext(item: UUID): Future[String] = {
          Future.successful(item.toString)
        }

        override def onSuccess(result: String): Any = {
          console.log(s"result: $result")
        }

        override def onFailure(cause: Throwable): Any = {
          console.error(s"error: ${cause.getMessage}")
        }

        override def onComplete(): Int = {
          console.log("Done")
          100
        }
      }, concurrency = 10)
    }
  }

  tests.runAsync() map { results =>
    console.log(s"results: $results")
    results
  }

}
