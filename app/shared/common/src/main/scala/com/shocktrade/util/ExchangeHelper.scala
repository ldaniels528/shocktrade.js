package com.shocktrade.util

import scala.scalajs.js

/**
  * Exchange Utility
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ExchangeHelper {
  private val exchangeMapping = js.Dictionary(
    "ASE" -> "NYSE",
    "NCM" -> "NASDAQ",
    "NGM" -> "NASDAQ",
    "NMS" -> "NASDAQ",
    "NYQ" -> "NYSE",
    "OBB" -> "OTCBB",
    "PCX" -> "NYSE",
    "PNK" -> "OTCBB"
  )

  def lookupExchange(subExchange: String) = {
    exchangeMapping.get(subExchange)
  }

}
