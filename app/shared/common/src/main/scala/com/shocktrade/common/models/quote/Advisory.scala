package com.shocktrade.common.models.quote

import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents OTC advisory information
  * @param description the given advisory description
  * @param type        the given advisory type
  */
@ScalaJSDefined
class Advisory(val description: String, val `type`: js.UndefOr[String]) extends js.Object

/**
  * Advisory Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object Advisory {

  /**
    * Optionally returns an advisory for the given symbol
    * @param symbol   the given symbol
    * @param exchange the given exchange
    * @return an optional [[Advisory advisory]]
    */
  def apply(symbol: String, exchange: js.UndefOr[String] = js.undefined): js.UndefOr[Advisory] = {
    if (symbol.length != 5) js.undefined
    else {
      // get the last character of the symbol
      val lastChar = symbol.lastOption.orUndefined

      // get the advisory description
      val description = lastChar flatMap {
        case 'A' => "Class A asset"
        case 'B' => "Class B asset"
        case 'C' if exchange.contains("NASDAQ") => "Exception"
        case 'C' => "Continuance"
        case 'D' => "New issue or reverse split"
        case 'E' => "Delinquent in required SEC filings"
        case 'F' => "Foreign security"
        case 'G' => "First convertible bond"
        case 'H' => "Second convertible bond"
        case 'I' if exchange.exists(_.startsWith("OTC")) => "Additional warrants or preferreds"
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
      val `type` = lastChar map {
        case 'A' | 'B' | 'N' | 'O' | 'P' | 'R' | 'X' => "INFO"
        case _ => "WARNING"
      }

      description map (new Advisory(_, `type`))
    }
  }

  /**
    * Advisory Enrichment
    * @param advisory the given [[Advisory advisory]]
    */
  implicit class AdvisoryEnrichment(val advisory: Advisory) extends AnyVal {

    @inline
    def isInformational = advisory.`type`.contains("INFO")

    @inline
    def isWarning = advisory.`type`.contains("WARNING")

  }

}