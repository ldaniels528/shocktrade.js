package com.shocktrade.serverside.persistence.eventsource

import com.shocktrade.eventsource.EventSource.generateUID

import scala.scalajs.js

/**
  * Represents the creation of a new position
  * @author lawrence.daniels@gmail.com
  */
class PositionCreationEvent(val name: js.UndefOr[String] = classOf[PositionCreationEvent].getSimpleName,
                            val uuid: js.UndefOr[String] = generateUID,
                            val userID: js.UndefOr[String],
                            val positionID: js.UndefOr[String] = generateUID,
                            val orderID: js.UndefOr[String],
                            val symbol: js.UndefOr[String],
                            val exchange: js.UndefOr[String],
                            val price: js.UndefOr[Double],
                            val quantity: js.UndefOr[Int],
                            val effectiveTime: js.UndefOr[js.Date] = new js.Date()) extends PositionEvent