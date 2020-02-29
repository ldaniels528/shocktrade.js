package com.shocktrade.common.models.contest

import io.scalajs.util.JsUnderOrHelper._

import scala.scalajs.js

/**
  * Participant Model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class Participant(var _id: js.UndefOr[String],
                  var name: js.UndefOr[String],
                  var rank: js.UndefOr[String] = js.undefined,
                  var totalEquity: js.UndefOr[Double] = js.undefined,
                  var gainLoss: js.UndefOr[Double] = js.undefined,
                  var joinedTime: js.UndefOr[js.Date] = new js.Date()) extends js.Object

/**
  * Participant Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object Participant {

  /**
    * Participant Enrichment
    * @param participant the given [[Participant participant]]
    */
  implicit class ParticipantEnrichment(val participant: Participant) extends AnyVal {

    @inline
    def is(id: js.UndefOr[String]): Boolean = participant._id ?== id

  }

}