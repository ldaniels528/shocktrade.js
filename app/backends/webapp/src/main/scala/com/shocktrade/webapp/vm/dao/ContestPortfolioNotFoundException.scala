package com.shocktrade.webapp.vm.dao

case class ContestPortfolioNotFoundException(contestID: String, userID: String)
  extends VirtualMachineException(s"Portfolio was not found [contest $contestID, user $userID]")