package com.shocktrade.client.contest

import com.shocktrade.client.contest.ContestSearchOptions.ContestStatus
import com.shocktrade.common.forms.ContestCreationForm.{GameBalance, GameDuration}
import com.shocktrade.common.forms.ContestSearchRequest
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
 * Contest Search Options
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestSearchOptions(var userID: js.UndefOr[String] = js.undefined,
                           val buyIn: js.UndefOr[GameBalance] = js.undefined,
                           val continuousTrading: js.UndefOr[Boolean] = js.undefined,
                           val duration: js.UndefOr[GameDuration] = js.undefined,
                           val friendsOnly: js.UndefOr[Boolean] = js.undefined,
                           val invitationOnly: js.UndefOr[Boolean] = js.undefined,
                           val levelCap: js.UndefOr[Int] = js.undefined,
                           val levelCapAllowed: js.UndefOr[Boolean] = js.undefined,
                           val myGamesOnly: js.UndefOr[Boolean] = js.undefined,
                           val nameLike: js.UndefOr[String] = js.undefined,
                           val perksAllowed: js.UndefOr[Boolean] = js.undefined,
                           val robotsAllowed: js.UndefOr[Boolean] = js.undefined,
                           val status: js.UndefOr[ContestStatus] = js.undefined) extends js.Object


/**
 * Contest Search Options Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ContestSearchOptions {
  import ContestSearchRequest.{ACTIVE_AND_QUEUED, ACTIVE_ONLY, ALL, QUEUED_ONLY}

  val contestStatuses: js.Array[ContestStatus] = js.Array(
    new ContestStatus(statusID = ACTIVE_AND_QUEUED, description = "Active and Queued"),
    new ContestStatus(statusID = ACTIVE_ONLY, description = "Active Only"),
    new ContestStatus(statusID = QUEUED_ONLY, description = "Queued Only"),
    new ContestStatus(statusID = ALL, description = "All")
  )

  /**
   * Represents a contest status
   * @param statusID    the status ID
   * @param description the status description
   */
  class ContestStatus(val statusID: Int, val description: String) extends js.Object

  /**
   * Contest Search Options Enrichment
   * @param options the given [[ContestSearchOptions]]
   */
  final implicit class ContestSearchOptionsEnrichment(val options: ContestSearchOptions) extends AnyVal {

    def toRequest = new ContestSearchRequest(
      userID = options.userID,
      buyIn = options.buyIn.flatMap(_.value),
      continuousTrading = options.continuousTrading,
      duration = options.duration.flatMap(_.value),
      friendsOnly = options.friendsOnly,
      invitationOnly = options.invitationOnly,
      levelCap = options.levelCap,
      levelCapAllowed = options.levelCapAllowed,
      myGamesOnly = options.myGamesOnly,
      nameLike = options.nameLike,
      perksAllowed = options.perksAllowed,
      robotsAllowed = options.robotsAllowed,
      statusID = options.status.map(_.statusID)
    )

    def validate: js.Array[String] = {
      val messages = new js.Array[String]()
      if (options.levelCapAllowed.isTrue && options.levelCap.isEmpty) messages.push("Level cap must be specified")
      messages
    }

  }

}