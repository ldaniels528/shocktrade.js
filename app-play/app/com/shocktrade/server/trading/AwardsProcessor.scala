package com.shocktrade.server.trading

import com.shocktrade.models.profile.AwardCodes
import AwardCodes._
import com.shocktrade.models.contest.{Contest, Participant, PerkTypes}
import play.api.Logger

import scala.concurrent.ExecutionContext

/**
 * Awards Processor
 * @author lawrence.daniels@gmail.com
 */
object AwardsProcessor {
  private val conditions = Seq(CheckeredFlag, CrystalBall, GoldTrophy, MadMoney, PayDirt, PerkSet)
  private val MinParticipation = 4

  def qualifyAwards(c: Contest)(implicit ec: ExecutionContext) = {
    c.participants map { participant =>
      // get all qualified awards for the current player
      val awards = conditions.foldLeft[List[AwardCode]](Nil) { (awards, condition) =>
        condition.getAward(c, participant) map (_ :: awards) getOrElse awards
      }

      //
      Logger.info(s"awards = $awards")
      (participant, awards)
    }
  }

  trait AwardCondition {
    def getAward(c: Contest, p: Participant): Option[AwardCode]
  }

  object CheckeredFlag extends AwardCondition {
    override def getAward(c: Contest, p: Participant) = {
      if (c.participants.length >= MinParticipation) Option(CHKDFLAG) else None
    }
  }

  object CrystalBall extends AwardCondition {
    override def getAward(c: Contest, p: Participant) = {
      if (c.participants.length >= MinParticipation) Option(CRYSTBAL) else None
    }
  }

  object GoldTrophy extends AwardCondition {
    override def getAward(c: Contest, p: Participant) = {
      c.participants.sortBy(p => p.cashAccount.cashFunds + p.marginAccount.map(_.cashFunds).getOrElse(0d)).headOption flatMap { leader =>
        if (c.participants.length >= MinParticipation && p.id == leader.id) Option(GLDTRPHY) else None
      }
    }
  }

  object MadMoney extends AwardCondition {
    override def getAward(c: Contest, p: Participant) = {
      val totalCash = c.participants.map(p => p.cashAccount.cashFunds + p.marginAccount.map(_.cashFunds).getOrElse(0d)).sum
      if (totalCash / c.startingBalance >= 3.5) Option(MADMONEY) else None
    }
  }

  object PayDirt extends AwardCondition {
    override def getAward(c: Contest, p: Participant) = {
      val totalCash = c.participants.map(p => p.cashAccount.cashFunds + p.marginAccount.map(_.cashFunds).getOrElse(0d)).sum
      if (totalCash / c.startingBalance >= 2.0) Option(PAYDIRT) else None
    }
  }

  object PerkSet extends AwardCondition {
    override def getAward(c: Contest, p: Participant) = {
      if (c.participants.length >= MinParticipation && p.perks.containsSlice(PerkTypes.values.toSeq)) Option(PERKSET) else None
    }
  }

}
