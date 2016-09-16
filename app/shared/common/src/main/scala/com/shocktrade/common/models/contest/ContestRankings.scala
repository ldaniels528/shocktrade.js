package com.shocktrade.common.models.contest

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Contest Portfolio Rankings
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class ContestRankings(var participants: js.UndefOr[js.Array[PortfolioRanking]] = js.undefined,
                      var leader: js.UndefOr[PortfolioRanking] = js.undefined,
                      var player: js.UndefOr[PortfolioRanking] = js.undefined) extends js.Object

