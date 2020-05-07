package com.shocktrade.webapp.vm.dao

case class TickerNotFoundException(symbol: String, exchange: String)
  extends RuntimeException(s"Symbol $symbol ($exchange) was not found")