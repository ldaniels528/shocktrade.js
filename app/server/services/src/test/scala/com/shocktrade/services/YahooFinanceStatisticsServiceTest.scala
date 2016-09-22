package com.shocktrade.services

import org.scalajs.nodejs.NodeRequire._
import org.scalajs.nodejs.console
import utest._

/**
  * Yahoo Finance! Statistics Service Test
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class YahooFinanceStatisticsServiceTest extends TestSuite {
  private val service = new YahooFinanceStatisticsService()(require)

  override val tests = this {
    "Yahoo! Finance Statistics Service should return stock quotes" - {
      service("AAPL") foreach { quote =>
        assert(quote.flatMap(_.symbol.toOption).contains("AAPL"))
        console.log("quote: %j", quote)
      }
    }
  }

}
