package com.shocktrade.common.models.contest

import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Portfolio Ranking
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class PortfolioRanking(var _id: js.UndefOr[String] = js.undefined,
                       var facebookID: js.UndefOr[String] = js.undefined,
                       var name: js.UndefOr[String] = js.undefined,
                       var rank: js.UndefOr[String] = js.undefined,
                       var totalEquity: js.UndefOr[Double] = js.undefined,
                       var gainLoss: js.UndefOr[Double] = js.undefined) extends js.Object

/**
  * Portfolio Ranking
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object PortfolioRanking {

  /**
    * Portfolio Ranking Enrichment
    * @param ranking the given [[PortfolioRanking portfolio ranking]]
    */
  implicit class PortfolioRankingEnrichment(val ranking: PortfolioRanking) extends AnyVal {

    @inline
    def copy(_id: js.UndefOr[String] = js.undefined,
             facebookID: js.UndefOr[String] = js.undefined,
             name: js.UndefOr[String] = js.undefined,
             rank: js.UndefOr[String] = js.undefined,
             totalEquity: js.UndefOr[Double] = js.undefined,
             gainLoss: js.UndefOr[Double] = js.undefined) = {
      new PortfolioRanking(
        _id = _id ?? ranking._id,
        facebookID = facebookID ?? ranking.facebookID,
        name = name ?? ranking.name,
        rank = rank ?? ranking.rank,
        totalEquity = totalEquity ?? ranking.totalEquity,
        gainLoss = gainLoss ?? ranking.gainLoss
      )
    }

  }

}