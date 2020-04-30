package com.shocktrade.common.forms

import com.shocktrade.common.forms.ContestCreationForm.{GameBalance, GameDuration}
import com.shocktrade.common.forms.ContestSearchOptions.ContestStatus
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * Contest Search Options
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestSearchOptions(var userID: js.UndefOr[String] = js.undefined,
                           var buyIn: js.UndefOr[GameBalance] = js.undefined,
                           var continuousTrading: js.UndefOr[Boolean] = js.undefined,
                           var duration: js.UndefOr[GameDuration] = js.undefined,
                           var friendsOnly: js.UndefOr[Boolean] = js.undefined,
                           var invitationOnly: js.UndefOr[Boolean] = js.undefined,
                           var levelCap: js.UndefOr[Int] = js.undefined,
                           var levelCapAllowed: js.UndefOr[Boolean] = js.undefined,
                           var myGamesOnly: js.UndefOr[Boolean] = js.undefined,
                           var nameLike: js.UndefOr[String] = js.undefined,
                           var perksAllowed: js.UndefOr[Boolean] = js.undefined,
                           var robotsAllowed: js.UndefOr[Boolean] = js.undefined,
                           var status: js.UndefOr[ContestStatus] = js.undefined) extends js.Object


/**
 * Contest Search Options Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ContestSearchOptions {

  val ACTIVE_AND_QUEUED = 1
  val ACTIVE_ONLY = 2
  val QUEUED_ONLY = 3
  val ALL = 4

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

    def toQueryString: String = {
      val values = options.asInstanceOf[js.Dictionary[js.UndefOr[Any]]].toJSArray
      (for ((name, value) <- values if value.nonEmpty) yield s"$name=$value").mkString("&")
    }

    def validate: js.Array[String] = {
      val messages = new js.Array[String]()
      if (options.levelCapAllowed.isTrue && options.levelCap.isEmpty) messages.push("Level cap must be specified")
      messages
    }

  }

}