package com.shocktrade.webapp.vm.dao

case class PortfolioNotFoundException(portfolioID: String)
  extends VirtualMachineException(s"Portfolio $portfolioID was not found")