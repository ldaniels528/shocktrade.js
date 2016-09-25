package com.shocktrade.services

import org.scalajs.nodejs.NodeRequire._
import org.scalajs.nodejs.console
import utest._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

/**
  * Yahoo Finance! Statistics Service Test
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class YahooFinanceStatisticsServiceTests extends TestSuite {
  private val service = new YahooFinanceKeyStatisticsService()(require)

  override val tests = this {
    "Yahoo! Finance Statistics Service should return stock quotes" - {
      service("AAPL") foreach { quote =>
        assert(quote.flatMap(_.symbol.toOption).contains("AAPL"))
        console.log("quote: %j", quote)
      }
    }
  }

  tests.runAsync() map { results =>
    console.log(s"results: $results")
    results
  }

}
