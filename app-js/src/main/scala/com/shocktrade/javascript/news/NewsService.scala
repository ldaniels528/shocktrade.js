package com.shocktrade.javascript.news

import com.github.ldaniels528.scalascript.Service
import com.github.ldaniels528.scalascript.core.Http
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._
import com.shocktrade.javascript.models.BSONObjectID

import scala.scalajs.js

/**
  * News Service
  * @author lawrence.daniels@gmail.com
  */
class NewsService($http: Http) extends Service {

  def getNewsSources = $http.get[js.Array[NewsSource]]("/api/news/sources")

  def getNewsFeed(feedId: BSONObjectID) = {
    $http.get[js.Array[NewsChannel]](s"/api/news/feed/${feedId.$oid}")
  }

}

/**
  * News Channel
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait NewsChannel extends js.Object {
  var items: js.Array[NewsItem]
}

/**
  * News Channel Companion Object
  * @author lawrence.daniels@gmail.com
  */
object NewsChannel {

  implicit class NewsChannelEnrichment(val channel: NewsChannel) extends AnyVal {

    def copy(items: js.UndefOr[js.Array[NewsItem]] = js.undefined) = {
      val newChannel = makeNew[NewsChannel]
      newChannel.items = items getOrElse channel.items
      newChannel
    }
  }

}

/**
  * News Channel Item
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait NewsItem extends js.Object {
  var description: String
  var quotes: js.Array[NewsQuote]
}

/**
  * News Channel Item Companion Object
  * @author lawrence.daniels@gmail.com
  */
object NewsItem {

  implicit class NewsItemEnrichment(val item: NewsItem) extends AnyVal {

    def copy(description: js.UndefOr[String] = js.undefined,
             quotes: js.UndefOr[js.Array[NewsQuote]] = js.undefined) = {
      val newItem = makeNew[NewsItem]
      newItem.description = description getOrElse item.description
      newItem.quotes = quotes getOrElse item.quotes
      newItem
    }
  }

}

/**
  * News Quote
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait NewsQuote extends js.Object {
  var name: String
  var symbol: String
  var exchange: String
  var sector: String
  var industry: String
  var changePct: Double
}
