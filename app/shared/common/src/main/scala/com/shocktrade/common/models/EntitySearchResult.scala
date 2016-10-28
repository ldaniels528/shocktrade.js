package com.shocktrade.common.models

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents a Entity Search Result (e.g. Users, Groups, etc.)
  * @author lawrence.daniels@gmail.com
  */
@ScalaJSDefined
class EntitySearchResult(var _id: js.UndefOr[String] = js.undefined,
                         var name: js.UndefOr[String] = js.undefined,
                         var description: js.UndefOr[String] = js.undefined,
                         var `type`: js.UndefOr[String] = js.undefined) extends js.Object
