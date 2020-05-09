package com.shocktrade.webapp.vm.dao

case class MaxPlayersReachedException(gs: PortfolioStatus)
  extends VirtualMachineException(s"Maximum players reached (${gs.playerCount})")