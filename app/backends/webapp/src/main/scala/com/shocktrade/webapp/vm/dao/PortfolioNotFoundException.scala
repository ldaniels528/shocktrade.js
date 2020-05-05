package com.shocktrade.webapp.vm.dao

case class PortfolioNotFoundException(portfolioID: String)
  extends RuntimeException(s"Portfolio $portfolioID was not found")