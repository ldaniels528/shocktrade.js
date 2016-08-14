package com.shocktrade.javascript.models.contest

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Player Reference
  * @param _id        the given unique identifier
  * @param name       the name of the user
  * @param facebookID the facebook ID of the user
  */
@ScalaJSDefined
class PlayerRef(var _id: js.UndefOr[String],
                var name: js.UndefOr[String],
                var facebookID: js.UndefOr[String]) extends js.Object