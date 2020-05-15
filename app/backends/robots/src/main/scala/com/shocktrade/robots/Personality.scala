package com.shocktrade.robots

import com.shocktrade.common.forms.ResearchOptions
import com.shocktrade.common.models.contest.ContestRanking

import scala.scalajs.js
import scala.scalajs.js.UndefOr
import scala.util.Random

/**
 * Represents a synthetic personality
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait Personality {

  def onBuying(): ResearchOptions

  def onGoodbye(): js.UndefOr[String]

  def onOvertake(oldRank: Int, newRank: Int, me: js.UndefOr[ContestRanking], him: js.UndefOr[ContestRanking]): js.UndefOr[String]

  def onWelcome(): js.UndefOr[String]

}

/**
 * Personality Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object Personality {

  /**
   * Default Personality constructor
   * @param state the given [[RobotState]]
   * @return a new [[Personality]]
   */
  def apply(state: RobotState): Personality = new GenericPersonality(state)

  /**
   * Generic Personality
   * @param state the given [[RobotState]]
   */
  class GenericPersonality(state: RobotState) extends Personality {
    private val random = new Random()

    override def onBuying(): ResearchOptions = {
      val limit = 50
      state.robotName match {
        case "daisy" => new ResearchOptions(priceMax = 1.00, changeMax = 0.0, spreadMin = 25.0, volumeMin = 1e+6, maxResults = limit)
        case "gadget" => new ResearchOptions(priceMax = 5.00, maxResults = limit)
        case "teddy" => new ResearchOptions(priceMin = 5.00, priceMax = 25.00, spreadMin = 25.0, maxResults = limit)
        case "fugitive528" => new ResearchOptions(priceMin = 1.00, priceMax = 5.00, spreadMin = 50.0, maxResults = limit)
        case "joey" => new ResearchOptions(priceMin = 1.00, priceMax = 25.00, spreadMin = 25.0, maxResults = limit)
        case _ => new ResearchOptions(priceMax = random.nextDouble(), changeMax = 0.0, spreadMin = random.nextInt(75).toDouble, maxResults = limit)
      }
    }

    override def onGoodbye(): UndefOr[String] = {
      state.robotName match {
        case "daisy" => "Adios..."
        case "teddy" =>  "Goodbye."
        case _ => js.undefined
      }
    }

    override def onOvertake(oldRank: Int, newRank: Int, me: js.UndefOr[ContestRanking], him: js.UndefOr[ContestRanking]): js.UndefOr[String] = {
      val contestantName = him.flatMap(_.username).orNull
      newRank match {
        case 1 => s"Wake up @$contestantName! I just took first... Haha"
        case 2 => s"Sorry @$contestantName, you were in my way..."
        case _ => js.undefined
      }
    }

    override def onWelcome(): js.UndefOr[String] = {
      state.robotName match {
        case "daisy" => "Don't waste my time..."
        case "teddy" => "Hello everyone"
        case _ => js.undefined
      }
    }
  }

}