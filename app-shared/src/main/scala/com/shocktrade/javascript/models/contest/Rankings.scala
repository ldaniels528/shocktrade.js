package com.shocktrade.javascript.models.contest

import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Contest Participant Rankings
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class Rankings(var participants: js.Array[ParticipantRanking] = emptyArray,
               var leader: js.UndefOr[ParticipantRanking] = js.undefined,
               var player: js.UndefOr[ParticipantRanking] = js.undefined) extends js.Object

