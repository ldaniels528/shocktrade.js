package com.shocktrade.client.news

import scala.scalajs.js

/**
 * News Channel Item
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NewsItem(var description: String, var quotes: js.Array[NewsQuote]) extends js.Object

/**
 * News Channel Item Companion Object
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object NewsItem {

  implicit class NewsItemEnrichment(val item: NewsItem) extends AnyVal {

    @inline
    def copy(description: js.UndefOr[String] = js.undefined,
             quotes: js.UndefOr[js.Array[NewsQuote]] = js.undefined) = new NewsItem(
      description = description getOrElse item.description,
      quotes = quotes getOrElse item.quotes
    )
  }

}
