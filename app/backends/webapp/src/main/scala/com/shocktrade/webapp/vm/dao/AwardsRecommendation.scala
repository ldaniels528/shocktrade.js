package com.shocktrade.webapp.vm.dao

import scala.scalajs.js

/**
 * Represents the Awards Recommendation
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class AwardsRecommendation(val portfolioID: String,
                           val userID: String,
                           val awardCodes: js.Array[String],
                           val awardedXP: Int) extends js.Object

/**
 * Awards Recommendation
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object AwardsRecommendation {

  /**
   * Recommendation Extension
   * @param recommendation the given [[AwardsRecommendation]]
   */
  final implicit class RecommendationExtension(val recommendation: AwardsRecommendation) extends AnyVal {

    def apply(code: String): Int = if (recommendation.awardCodes.contains(code)) 1 else 0

  }

}
