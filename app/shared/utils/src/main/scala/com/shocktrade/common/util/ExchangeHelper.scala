package com.shocktrade.common.util

/**
 * Exchange Utility
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ExchangeHelper {
  private val exchangeMapping = Map(
    "ASE" -> "NYSE",
    "NCM" -> "NASDAQ",
    "NGM" -> "NASDAQ",
    "NMS" -> "NASDAQ",
    "NYQ" -> "NYSE",
    "OBB" -> "OTCBB",
    "PCX" -> "NYSE",
    "PNK" -> "OTCBB"
  )

  def lookupExchange(exchange: String) = exchangeMapping.get(exchange)

}
