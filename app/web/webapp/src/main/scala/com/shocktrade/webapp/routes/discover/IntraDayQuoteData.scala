package com.shocktrade.webapp.routes.discover

import io.scalajs.npm.mongodb.ObjectID

import scala.scalajs.js

/**
  * Intra-Day Quote Data
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class IntraDayQuoteData(val _id: js.UndefOr[ObjectID] = js.undefined,
                        val symbol: js.UndefOr[String],
                        val price: js.UndefOr[Double],
                        val time: js.UndefOr[String],
                        val volume: js.UndefOr[Double],
                        var aggregateVolume: js.UndefOr[Double],
                        val tradeDateTime: js.UndefOr[js.Date],
                        val creationTime: js.Date = new js.Date()) extends js.Object
