package com.shocktrade.common.models.contest

import scala.scalajs.js

/**
 * Represents a Contest-Like model
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait ContestLike extends js.Object {

  def contestID: js.UndefOr[String]

  def name: js.UndefOr[String]

  def hostUserID: js.UndefOr[String]

  def startTime: js.UndefOr[js.Date]

  def expirationTime: js.UndefOr[js.Date]

  def startingBalance: js.UndefOr[Double]

  def status: js.UndefOr[String]

}

/**
 * Contest-Like Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ContestLike {
  // status constants
  val StatusActive = "ACTIVE"
  val StatusClosed = "CLOSED"
  val StatusQueued = "QUEUED"

  /**
   * Contest Search Result Enriched
   * @param result the given [[ContestLike]]
   */
  final implicit class ContestLikeEnriched(val result: ContestLike) extends AnyVal {

    import io.scalajs.util.JsUnderOrHelper._

    ///////////////////////////////////////////////////////////////////////
    //  Contest Statuses
    ///////////////////////////////////////////////////////////////////////

    def isActive: Boolean = result.status.contains("ACTIVE")

    def isClosed: Boolean = result.status.contains("CLOSED")

    def isQQueued: Boolean = result.status.contains("QUEUED")

  }

}
