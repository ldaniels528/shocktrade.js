package com.shocktrade.javascript.data

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Basic Quote
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait BasicQuote extends Quote {
  var symbol: String = js.native
  var baseSymbol: js.UndefOr[String] = js.native
  var exchange: String = js.native
  var name: js.UndefOr[String] = js.native
  var active: js.UndefOr[Boolean] = js.native

  // basics
  var lastTrade: js.UndefOr[Double] = js.native
  var tradeDateTime: js.UndefOr[js.Date] = js.native
  var changePct: js.UndefOr[Double] = js.native
  var prevClose: js.UndefOr[Double] = js.native
  var open: js.UndefOr[Double] = js.native
  var close: js.UndefOr[Double] = js.native
  var low: js.UndefOr[Double] = js.native
  var high: js.UndefOr[Double] = js.native
  var spread: js.UndefOr[Double] = js.native
  var low52Week: js.UndefOr[Double] = js.native
  var high52Week: js.UndefOr[Double] = js.native
  var volume: js.UndefOr[Long] = js.native
}

/**
  * Basic Quote Companion
  * @author lawrence.daniels@gmail.com
  */
object BasicQuote {

  val Fields = js.Array(
    "symbol", "exchange", "name", "active",
    "lastTrade", "tradeDateTime", "changePct", "prevClose",
    "open", "close", "low", "high", "spread", "low52Week", "high52Week", "volume"
  )

  /**
    * Represents OTC advisory information
    * @param description the given advisory description
    * @param type        the given advisory type
    */
  @ScalaJSDefined
  class Advisory(val description: String, val `type`: String) extends js.Object

  /**
    * Basic Quote Extensions
    * @param quote the given [[BasicQuote basic quote]]
    */
  implicit class BasicQuoteExtensions(val quote: BasicQuote) extends AnyVal {

    /**
      * Returns the OTC advisory for the given symbol
      * @return the OTC advisory for the given symbol
      */
    @inline
    def getAdvisory = {
      if(quote.symbol.length != 5) js.undefined
      else {
        // get the advisory description
        val description: js.UndefOr[String] = quote.symbol.last match {
          case 'A' => "Class A asset"
          case 'B' => "Class BS asset"
          case 'C' if quote.exchange == "NASDAQ" => "Exception"
          case 'C' => "Continuance"
          case 'D' => "New issue or reverse split"
          case 'E' => "Delinquent in required SEC filings"
          case 'F' => "Foreign security"
          case 'G' => "First convertible bond"
          case 'H' => "Second convertible bond"
          case 'I' if quote.exchange.contains("OTC") => "Additional warrants or preferreds"
          case 'I' => "Third convertible bond"
          case 'J' => "Voting share - special"
          case 'K' => "Nonvoting (common)"
          case 'L' => "Miscellaneous"
          case 'M' => "Fourth class - preferred shares"
          case 'N' => "Third class - preferred shares"
          case 'O' => "Second class - preferred shares"
          case 'P' => "First class - Preferred shares"
          case 'Q' => "Involved in bankruptcy proceedings"
          case 'R' => "Rights"
          case 'S' => "Shares of beneficial interest"
          case 'T' => "With warrants or rights"
          case 'U' => "Units"
          case 'V' => "Pending issue and distribution"
          case 'W' => "Warrants"
          case 'X' => "Mutual fund"
          case 'Y' => "ADR (American Depositary Receipts)"
          case 'Z' => "Miscellaneous situations, such as stubs, depositary receipts, limited partnership units, or additional warrants or units"
          case _ => js.undefined
        }

        // get the advisory type
        val advisoryType = quote.symbol.last match {
          case 'A' | 'B' | 'N' | 'O' | 'P' | 'R' | 'X' => "INFO"
          case _ => "WARNING"
        }

        description map (new Advisory(_, advisoryType))
      }
    }

  }

}