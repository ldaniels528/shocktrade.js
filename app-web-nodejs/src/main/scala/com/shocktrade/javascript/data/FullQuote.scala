package com.shocktrade.javascript.data

import com.shocktrade.javascript.data.FullQuote.Executive

import scala.scalajs.js

/**
  * Full Quote
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait FullQuote extends BasicQuote with KeyStatistics {
  // details
  var ask: js.UndefOr[Double] = js.native
  var askSize: js.UndefOr[Int] = js.native
  var bid: js.UndefOr[Double] = js.native
  var bidSize: js.UndefOr[Int] = js.native
  var change: js.UndefOr[Double] = js.native
  var target1Yr: js.UndefOr[Double] = js.native

  // financials
  var beta: js.UndefOr[Double] = js.native
  var bookValuePerShare: js.UndefOr[Double] = js.native
  var EBITDA: js.UndefOr[Double] = js.native

  // classification
  var assetClass: js.UndefOr[String] = js.native
  var assetType: js.UndefOr[String] = js.native
  var legalType: js.UndefOr[String] = js.native

  // standard codes
  var naicsNumber: js.UndefOr[Int] = js.native
  var sicNumber: js.UndefOr[Int] = js.native

  // summary
  var businessSummary: js.UndefOr[String] = js.native
  var executives: js.Array[Executive] = js.native

}

/**
  * Full Quote Companion
  * @author lawrence.daniels@gmail.com
  */
object FullQuote {

  /**
    * Company Executive
    * @author lawrence.daniels@gmail.com
    */
  @js.native
  trait Executive extends js.Object

  /**
    * Full Quote Extensions
    * @param quote the given [[FullQuote full quote]]
    */
  implicit class FullQuoteExtensions(val quote: FullQuote) extends AnyVal {

    @inline
    def getRiskLevel = {
      quote.beta map {
        case b if b >= 0 && b <= 1.25 => "Low"
        case b if b > 1.25 && b <= 1.9 => "Medium"
        case _ => "High"
      } getOrElse "Unknown"
    }

  }

}

