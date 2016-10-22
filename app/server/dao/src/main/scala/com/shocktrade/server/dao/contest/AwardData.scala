package com.shocktrade.server.dao.contest

import org.scalajs.nodejs.mongodb.ObjectID

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  * Represents an Award Data model
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@ScalaJSDefined
class AwardData(val _id: js.UndefOr[ObjectID] = js.undefined,
                val name: js.UndefOr[String] = js.undefined,
                val code: js.UndefOr[String] = js.undefined,
                val icon: js.UndefOr[String] = js.undefined,
                val description: js.UndefOr[String] = js.undefined) extends js.Object