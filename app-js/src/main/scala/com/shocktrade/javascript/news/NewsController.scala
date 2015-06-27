package com.shocktrade.javascript.news

import biz.enef.angulate.named
import com.ldaniels528.javascript.angularjs.core.Controller
import com.ldaniels528.javascript.angularjs.extensions.{CookieStore, Sce, Toaster}
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
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
  extends Controller {

  private var newsSymbols = js.Array[js.Dynamic]()
  private var channels = js.Array[js.Dynamic]()
  private var newsSources = js.Array[js.Dynamic]()

  // define the scope variables
  // view: get the previously selected view from the cookie
  $scope.selection = JS(feed = "")
  $scope.view = $cookieStore.get("NewsController_view").toOption.getOrElse("list")

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.getChannels = () => channels

  $scope.getNewsFeed = (feedId: String) => findNewsFeed(feedId)

  $scope.getNewsSources = () => loadNewsSources()

  $scope.gridClass = (index: Int) => getGridClass(index)

  $scope.newsSources = () => newsSources

  $scope.trustMe = (html: String) => $sce.trustAsHtml(html)

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions and Data
  /////////////////////////////////////////////////////////////////////////////

  private def loadNewsSources() {
    g.console.log("Loading news sources...")
    asyncLoading($scope)(newsService.getNewsSources) onComplete {
      case Success(sources) =>
        newsSources = sources

        // select the ID of the first feed
        sources.headOption.map(_.OID) foreach { feed =>
          $scope.selection.feed = feed
          findNewsFeed(feed)
        }
      case Failure(e) =>
        toaster.error("Failed to load news sources")
    }
  }

  private def findNewsFeed(feedId: String) = {
    g.console.log("Getting news feeds...")
    asyncLoading($scope)(newsService.getNewsFeed(feedId)) onComplete {
      case Success(feedChannels) =>
        populateQuotes(feedChannels)
        this.channels = feedChannels; //enrichTickers(feeds)
      case Failure(e) =>
        toaster.error(s"Failed to load news feed $feedId")
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
        if (description.last != '.') item.description += "..."
      }
    }
    channels
  }

  /**
   * Return the appropriate class to create a diagonal grid
   */
  private def getGridClass(index: Int) = {
    val row = Math.floor(index / 2)
    val cell = if (row % 2 == 0) index % 2 else (index + 1) % 2
    s"news_tile$cell"
  }

  private def replaceSymbols(description: String, quotes: js.Array[js.Dynamic]) = {
    val sb = new StringBuilder(description)
    quotes.foreach { q =>
      val term = s"( ${q.symbol} )"
      description.indexOf(term) match {
        case -1 => description
        case start =>
          sb.replace(start, start + term.length,
            s"""(<a href="#/discover?symbol=${q.symbol}">
                    <span ${popup(q)} class="${q.exchange}">${q.symbol}</span>
                  </a>${changeArrow(q)})""".stripPrefix(" ").stripSuffix(" ")
          )
      }
    }
    sb.toString()
  }

  private def popup(q: js.Dynamic) = {
    s"""popover-title="${q.name} (${q.exchange})"
        popover="${q.sector} &#8212; ${q.industry}"
        popover-trigger="mouseenter"
        popover-placement="right"
        """.stripPrefix(" ").stripSuffix(" ")
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

}
