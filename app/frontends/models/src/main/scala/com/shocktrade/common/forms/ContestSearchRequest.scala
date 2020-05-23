package com.shocktrade.common.forms

import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * Contest Search Request
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestSearchRequest(val userID: js.UndefOr[String] = js.undefined,
                           val buyIn: js.UndefOr[Double] = js.undefined,
                           val continuousTrading: js.UndefOr[Boolean] = js.undefined,
                           val duration: js.UndefOr[Int] = js.undefined,
                           val friendsOnly: js.UndefOr[Boolean] = js.undefined,
                           val invitationOnly: js.UndefOr[Boolean] = js.undefined,
                           val levelCap: js.UndefOr[Int] = js.undefined,
                           val levelCapAllowed: js.UndefOr[Boolean] = js.undefined,
                           val myGamesOnly: js.UndefOr[Boolean] = js.undefined,
                           val nameLike: js.UndefOr[String] = js.undefined,
                           val perksAllowed: js.UndefOr[Boolean] = js.undefined,
                           val robotsAllowed: js.UndefOr[Boolean] = js.undefined,
                           val statusID: js.UndefOr[Int] = js.undefined) extends js.Object


/**
 * Contest Search Request Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ContestSearchRequest {

  val ACTIVE_AND_QUEUED = 1
  val ACTIVE_ONLY = 2
  val QUEUED_ONLY = 3
  val ALL = 4

  /**
   * Contest Search Request Enrichment
   * @param request the given [[ContestSearchRequest]]
   */
  final implicit class ContestSearchRequestEnrichment(val request: ContestSearchRequest) extends AnyVal {

    def toQueryString: String = {
      val values = request.asInstanceOf[js.Dictionary[js.UndefOr[Any]]].toJSArray
      (for ((name, value) <- values if value.nonEmpty) yield s"$name=$value").mkString("&")
    }

    def validate: js.Array[String] = {
      val messages = new js.Array[String]()
      if (request.levelCapAllowed.isTrue && request.levelCap.isEmpty) messages.push("Level cap must be specified")
      messages
    }

  }

}