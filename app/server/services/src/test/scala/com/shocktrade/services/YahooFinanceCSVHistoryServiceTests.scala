package com.shocktrade.services

import com.shocktrade.server.services.yahoo.YahooFinanceCSVHistoryService
import org.scalajs.nodejs.NodeRequire.require
import org.scalajs.nodejs.console
import org.scalajs.sjs.DateHelper._
import utest._

import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

/**
  * Yahoo Finance! CSV History Service Tests
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class YahooFinanceCSVHistoryServiceTests extends TestSuite {
  private val service = new YahooFinanceCSVHistoryService()(require)

  override val tests = this {
    "Yahoo! Finance CSV History Service should return historical quotes" - {
      service("AAPL", from = new js.Date() - 30.days, to = new js.Date()) foreach { quote =>
        console.log("quote: %j", quote)
      }
      1
    }
  }

  tests.runAsync() map { results =>
    console.log(s"results: $results")
    results
  }

}
