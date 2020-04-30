package com.shocktrade.common

import com.shocktrade.common.models.quote._
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Securities Helper
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object SecuritiesHelper {

  def getAssetCode(q: js.UndefOr[ClassifiedQuote]): String = {
    q.flatMap(_.assetType) map {
      case "Crypto-Currency" => "&#xf15a" // fa-bitcoin
      case "Currency" => "&#xf155" // fa-dollar
      case "ETF" => "&#xf18d" // fa-stack-exchange
      case _ => "&#xf0ac" // fa-globe
    } getOrElse ""
  }

  def getAssetIcon(q: js.UndefOr[ClassifiedQuote]): String = {
    q.flatMap(_.assetType) map {
      case "Crypto-Currency" => "fa fa-bitcoin st_blue"
      case "Currency" => "fa fa-dollar st_blue"
      case "ETF" => "fa fa-stack-exchange st_blue"
      case _ => "fa fa-globe st_blue"
    } getOrElse "fa fa-globe st_blue"
  }

  def normalizeExchange(aMarket: js.UndefOr[String]): String = {
    aMarket.flat map (_.toUpperCase) map {
      //case s if s.contains("ASE") => s
      //case s if s.contains("CCY") => s
      case s if s.contains("NAS") => "NASDAQ"
      case s if s.contains("NCM") => "NASDAQ"
      case s if s.contains("NGM") => "NASDAQ"
      case s if s.contains("NMS") => "NASDAQ"
      case s if s.contains("NYQ") => "NYSE"
      case s if s.contains("NYS") => "NYSE"
      case s if s.contains("OBB") => "OTCBB"
      case s if s.contains("OTC") => "OTCBB"
      case s if s.contains("OTHER") => "OTHER_OTC"
      //case s if s.contains("PCX") => s
      case s if s.contains("PNK") => "OTCBB"
      case s => s
    } getOrElse ""
  }
}
