package com.shocktrade.common.models

import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Player Reference model
  * @param _id        the given unique identifier
  * @param name       the name of the user
  * @param facebookID the facebook ID of the user
  */
@ScalaJSDefined
class PlayerRef(var _id: js.UndefOr[String],
                var name: js.UndefOr[String],
                var facebookID: js.UndefOr[String]) extends js.Object

/**
  * PlayerRef Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object PlayerRef {

  /**
    * PlayerRef Enrichment
    * @param playerRef the given [[PlayerRef playerRef]]
    */
  implicit class PlayerRefEnrichment(val playerRef: PlayerRef) extends AnyVal {

    @inline
    def is(playerId: js.UndefOr[String]) = playerRef._id ?== playerId

  }

}