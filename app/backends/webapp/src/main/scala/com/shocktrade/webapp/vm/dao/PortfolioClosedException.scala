package com.shocktrade.webapp.vm.dao

import scala.scalajs.js

case class PortfolioClosedException(portfolioID: String, closedTime: js.UndefOr[js.Date])
  extends VirtualMachineException(s"Portfolio $portfolioID is closed ${closedTime.map(t => s"at $t").getOrElse("")}")