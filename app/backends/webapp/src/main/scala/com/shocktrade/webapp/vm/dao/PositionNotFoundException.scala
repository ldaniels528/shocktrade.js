package com.shocktrade.webapp.vm.dao

case class PositionNotFoundException(portfolioID: String, symbol: String, exchange: String)
  extends VirtualMachineException(s"No position for $exchange.$symbol was found")