package com.shocktrade.javascript.models.contest

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Contest-like Model
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
trait ContestLike extends js.Object {

  def name: js.UndefOr[String]

  def creator: js.UndefOr[String]

  def startTime: js.UndefOr[js.Date]

  def status: js.UndefOr[String]

  def participants: js.UndefOr[js.Array[Participant]]

  // indicators

  def friendsOnly: js.UndefOr[Boolean]

  def invitationOnly: js.UndefOr[Boolean]

  def levelCap: js.UndefOr[String]

  def perksAllowed: js.UndefOr[Boolean]

  def robotsAllowed: js.UndefOr[Boolean]

}
