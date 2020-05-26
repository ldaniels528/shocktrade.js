package com.shocktrade.robots

import com.shocktrade.common.forms.ResearchOptions
import com.shocktrade.common.models.contest.{ContestRanking, Position}
import com.shocktrade.common.models.quote.ResearchQuote

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.util.Random

/**
 * Represents a synthetic personality; an artificial intelligence (AI)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait Personality {

  def computePurchasePrice(stock: ResearchQuote)(implicit state: RobotContext): js.UndefOr[Double]

  def computeSalePrice(position: Position)(implicit state: RobotContext): js.UndefOr[Double]

  def onFindStocksToBuy(implicit state: RobotContext): js.UndefOr[ResearchOptions]

  def onGoodbye(implicit state: RobotContext): js.UndefOr[String]

  def onHello(implicit state: RobotContext): js.UndefOr[String]

  def onRankChange(oldRank: Int, newRank: Int,
                   oldRankings: js.UndefOr[js.Array[ContestRanking]],
                   newRankings: js.Array[ContestRanking]): js.UndefOr[String]

}

/**
 * Personality Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object Personality {

  /**
   * Default Personality constructor
   * @return a new [[Personality]]
   */
  def apply(state: RobotContext): Personality = GenericPersonality

  /**
   * Generic Personality
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  object GenericPersonality extends Personality {
    private val random = new Random()

    override def computePurchasePrice(stock: ResearchQuote)(implicit state: RobotContext): js.UndefOr[Double] = {
      stock.low.map(_ * 1.10) // day's low + 10%
    }

    override def computeSalePrice(position: Position)(implicit state: RobotContext): js.UndefOr[Double] = {
      position.high.map(_ * 0.90) // day's high - 10%
    }

    override def onFindStocksToBuy(implicit state: RobotContext): js.UndefOr[ResearchOptions] = {
      val limit = 20
      state.robotName match {
        case "daisy" => new ResearchOptions(priceMax = 1.00, changeMax = 0.0, spreadMin = 25.0, volumeMin = 1e+6, maxResults = limit)
        case "gadget" => new ResearchOptions(priceMax = 5.00, maxResults = limit)
        case "teddy" => new ResearchOptions(priceMin = 5.00, priceMax = 25.00, spreadMin = 25.0, maxResults = limit)
        case "fugitive528" => new ResearchOptions(priceMin = 1.00, priceMax = 5.00, spreadMin = 50.0, maxResults = limit)
        case "joey" => new ResearchOptions(priceMin = 1.00, priceMax = 25.00, spreadMin = 25.0, maxResults = limit)
        case _ => new ResearchOptions(priceMax = random.nextDouble(), changeMax = 0.0, spreadMin = random.nextInt(75).toDouble, maxResults = limit)
      }
    }

    override def onGoodbye(implicit state: RobotContext): UndefOr[String] = {
      state.robotName match {
        case "daisy" => "Adios..."
        case "teddy" => "Goodbye."
        case _ => js.undefined
      }
    }

    override def onHello(implicit state: RobotContext): js.UndefOr[String] = {
      state.robotName match {
        case "daisy" => "Don't waste my time..."
        case "teddy" => "Hello everyone"
        case _ => js.undefined
      }
    }

    override def onRankChange(oldRank: Int, newRank: Int,
                              oldRankings: js.UndefOr[js.Array[ContestRanking]],
                              newRankings: js.Array[ContestRanking]): js.UndefOr[String] = {
      val contestantName = newRankings(newRank + 1).username.orNull
      newRank match {
        case 1 if oldRankings.nonEmpty => s"Wake up @$contestantName! I just took first... Haha!"
        case _ => js.undefined
      }
    }

  }

}