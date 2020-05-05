package com.shocktrade.webapp.vm.dao

case class ContestPortfolioNotFoundException(contestID: String, userID: String)
  extends RuntimeException(s"Portfolio was not found [contest $contestID, user $userID]")