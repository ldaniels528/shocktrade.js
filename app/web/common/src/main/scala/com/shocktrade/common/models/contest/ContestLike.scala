package com.shocktrade.common.models.contest

import com.shocktrade.common.models.user.User
import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
  * Represents a Contest-like model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
trait ContestLike extends js.Object {

  def name: js.UndefOr[String]

  def creator: js.UndefOr[User]

  def startTime: js.UndefOr[js.Date]

  def startingBalance: js.UndefOr[Double]

  def status: js.UndefOr[String]

  def messages: js.UndefOr[js.Array[ChatMessage]]

  /////////////////////////////////////////////////////////////////////////
  //  Participants and Rankings
  /////////////////////////////////////////////////////////////////////////

  def participants: js.UndefOr[js.Array[Participant]]

  /////////////////////////////////////////////////////////////////////////
  //  Indicators
  /////////////////////////////////////////////////////////////////////////

  def friendsOnly: js.UndefOr[Boolean]

  def invitationOnly: js.UndefOr[Boolean]

  def levelCap: js.UndefOr[String]

  def perksAllowed: js.UndefOr[Boolean]

  def robotsAllowed: js.UndefOr[Boolean]

}

/**
  * Contest-like Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ContestLike {
  val MaxPlayers = 24

  // status constants
  val StatusActive = "ACTIVE"
  val StatusClosed = "CLOSED"

  /**
    * Contest Enrichment
    * @param contest the given [[ContestLike contest]]
    */
  implicit class ContestEnrichment(val contest: ContestLike) extends AnyVal {

    @inline
    def isActive = contest.status.contains(StatusActive)

    @inline
    def isClosed = contest.status.contains(StatusClosed)

    @inline
    def isEmpty = contest.participants.exists(_.isEmpty)

    @inline
    def isFull = contest.participants.exists(_.length >= MaxPlayers)

    @inline
    def isAlmostFull = contest.participants.exists(_.length + 1 >= MaxPlayers)

  }

}