package com.shocktrade.common.models.contest

import java.util.UUID

import org.scalajs.sjs.JsUnderOrHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Participant Model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class Participant(var _id: js.UndefOr[String],
                  var name: js.UndefOr[String],
                  var facebookID: js.UndefOr[String],
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

    def is(id: js.UndefOr[String]) = participant._id ?== id

  }

}