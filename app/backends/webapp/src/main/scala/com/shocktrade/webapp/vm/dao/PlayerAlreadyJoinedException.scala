package com.shocktrade.webapp.vm.dao

case class PlayerAlreadyJoinedException(gs: PortfolioStatus)
  extends VirtualMachineException("You have already joined this game")