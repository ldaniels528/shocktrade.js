package com.shocktrade.client.news

import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.js

/**
  * News Channel
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class NewsChannel(var items: js.Array[NewsItem] = emptyArray) extends js.Object

/**
  * News Channel Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object NewsChannel {

  /**
    * News Channel Enrichment
    * @param channel the given [[NewsChannel news channel]]
    */
  implicit class NewsChannelEnrichment(val channel: NewsChannel) extends AnyVal {

    @inline
    def copy(items: js.UndefOr[js.Array[NewsItem]] = js.undefined) = {
      new NewsChannel(items getOrElse channel.items)
    }

  }

}