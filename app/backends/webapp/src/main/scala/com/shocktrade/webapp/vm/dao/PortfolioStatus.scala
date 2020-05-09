package com.shocktrade.webapp.vm.dao

import scala.scalajs.js

class PortfolioStatus(val contestID: String,
                      val userID: String,
                      val isParticipant: Int,
                      val wallet: Double,
                      val startingBalance: Double,
                      val playerCount: Int) extends js.Object
