package com.shocktrade.javascript.news

import biz.enef.angulate.{ScopeController, named}
import com.ldaniels528.angularjs.{CookieStore, Sce, Toaster}

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
 * News Controller
 * @author lawrence.daniels@gmail.com
 */
class NewsController($scope: js.Dynamic, $cookieStore: CookieStore, $sce: Sce, toaster: Toaster,
                     @named("NewsService") newsService: NewsService)
  extends ScopeController {

  private var newsSymbols = js.Array[js.Dynamic]()
  private var loading = false
  private var channels = js.Array[js.Dynamic]()
  private val selection = JS(feed = "")
  private var newsSources = js.Array[js.Dynamic]()

  $scope.selection = selection

  // get the previously selected view from the cookie
  $scope.view = $cookieStore.get("NewsController_view").toOption.getOrElse("list")

  $scope.getChannels = () => channels

  $scope.newsSources = () => newsSources

  $scope.getNewsFeed = (feedId: String) => findNewsFeed(feedId)

  $scope.getNewsSources = () => findNewsSources

  /**
   * Return the appropriate class to create a diagonal grid
   */
  $scope.gridClass = { (index: Int) => val row = Math.floor(index / 2)
    val cell = if (row % 2 == 0) index % 2 else (index + 1) % 2
    s"news_tile$cell"
  }

  $scope.trustMe = (html: String) => $sce.trustAsHtml(html)

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions and Data
  /////////////////////////////////////////////////////////////////////////////

  private def findNewsSources = {
    g.console.log("Loading news sources...")
    startLoading()
    newsService.getNewsSources() onComplete {
      case Success(sources) =>
        newsSources = sources
        if (newsSources.nonEmpty) {
          val feed = newsSources.headOption.flatMap(obj => Option(obj._id).map(_.$oid)).orNull.asInstanceOf[String]
          selection.feed = feed
          findNewsFeed(feed)
        }
        stopLoading()
      case Failure(e) =>
        toaster.error("Failed to load news sources")
        stopLoading()
    }
  }

  private def findNewsFeed(feedId: String) = {
    g.console.log("Getting news feeds...")
    startLoading()
    newsService.getNewsFeed(feedId) onComplete {
      case Success(feedChannels) =>
        populateQuotes(feedChannels)
        this.channels = feedChannels; //enrichTickers(feeds)
        stopLoading()
      case Failure(e) =>
        toaster.error(s"Failed to load news feed $feedId")
        stopLoading()
    }
  }

  private def enrichTickers(channels: js.Array[js.Dynamic]) = {
    channels.foreach { channel =>
      val items = channel.asInstanceOf[js.Array[js.Dynamic]]
      items.foreach { item =>
        g.console.log(s"description = ${JSON.stringify(item.description)}")
        val description = item.description.asInstanceOf[String]
        val quotes = item.quotes.asInstanceOf[js.Array[js.Dynamic]]
        if (quotes.nonEmpty) {
          item.description = replaceSymbols(description, quotes)
        }

        // add ... to the end of incomplete sentences
        if (description.last != '.') {
          item.description += "..."
        }
      }
    }
    channels
  }

  private def replaceSymbols(description: String, quotes: js.Array[js.Dynamic]) = {
    val sb = new StringBuilder(description)
    quotes.foreach { q =>
      val term = s"( ${q.symbol} )"
      description.indexOf(term) match {
        case -1 => description
        case start =>
          sb.replace(start, start + term.length,
            s"""|(<a href="#/discover?symbol=${q.symbol}">
                |<span ${popup(q)} class="${q.exchange}">${q.symbol}</span>
                |</a>${changeArrow(q)})
                |""".stripMargin)
      }
    }
    sb.toString()
  }

  private def popup(q: js.Dynamic) = {
    s"""|popover-title="${q.name} (${q.exchange})"
        |popover="${q.sector} &#8212; ${q.industry}"
        |popover-trigger="mouseenter"
        |popover-placement="right"
        |""".stripMargin
  }

  private def populateQuotes(channels: js.Array[js.Dynamic]) = {
    // gather the quotes
    val myQuotes = channels.flatMap { channel =>
      val items = channel.items.asInstanceOf[js.Array[js.Dynamic]]
      items.flatMap(_.quotes.asInstanceOf[js.Array[js.Dynamic]])
    }

    // set the quotes
    newsSymbols = myQuotes
  }

  private def changeArrow(q: js.Dynamic) = {
    val changePct = q.changePct.asInstanceOf[Double]
    val isNeg = changePct < 0.00
    val iconClass = if (isNeg) "fa-arrow-down" else "fa-arrow-up"
    val colorClass = if (isNeg) "negative" else "positive"
    f"""<span class="fa $iconClass $colorClass">$changePct%.2f%%</span>"""
  }

  private def startLoading() = loading = true

  private def stopLoading() = loading = false

}
