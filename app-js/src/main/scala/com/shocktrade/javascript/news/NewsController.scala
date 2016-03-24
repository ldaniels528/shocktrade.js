package com.shocktrade.javascript.news

import com.github.ldaniels528.scalascript.extensions.{Cookies, Sce, Toaster}
import com.github.ldaniels528.scalascript.{Controller, injected}
import com.shocktrade.javascript.GlobalLoading
import com.github.ldaniels528.scalascript.util.ScalaJsHelper._
import com.shocktrade.javascript.models.{BSONObjectID, NewsFeed, NewsQuote, NewsSource}
import com.shocktrade.javascript.news.NewsController._
import org.scalajs.dom.console

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => JS}
import scala.util.{Failure, Success}

/**
 * News Controller
 * @author lawrence.daniels@gmail.com
 */
class NewsController($scope: js.Dynamic, $cookies: Cookies, $sce: Sce, toaster: Toaster,
                     @injected("NewsService") newsService: NewsService)
  extends Controller with GlobalLoading {

  private var newsSymbols = emptyArray[NewsQuote]
  private var channels = emptyArray[NewsFeed]
  private var newsSources = emptyArray[NewsSource]

  // define the scope variables
  // view: get the previously selected view from the cookie
  $scope.selection = JS(feed = "")
  $scope.view = $cookies.getOrElse(ViewTypeCookie, "list")

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.getChannels = () => channels

  $scope.getNewsFeed = (feedId: String) => findNewsFeed(BSONObjectID(feedId))

  $scope.getNewsSources = () => loadNewsSources()

  $scope.gridClass = (index: Int) => getGridClass(index)

  $scope.newsSources = () => newsSources

  $scope.trustMe = (html: String) => $sce.trustAsHtml(html)

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions
  /////////////////////////////////////////////////////////////////////////////

  private def loadNewsSources() {
    console.log("Loading news sources...")
    asyncLoading($scope)(newsService.getNewsSources) onComplete {
      case Success(sources) =>
        newsSources = sources

        // select the ID of the first feed
        sources.headOption.flatMap(_._id.toOption) foreach { feed =>
          $scope.selection.feed = feed
          findNewsFeed(feed)
        }
      case Failure(e) =>
        toaster.error("Failed to load news sources")
    }
  }

  private def findNewsFeed(feedId: BSONObjectID) = {
    console.log("Getting news feeds...")
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
      val items = channel.asArray[js.Dynamic]
      items.foreach { item =>
        val description = item.description.as[String]
        val quotes = item.quotes.asArray[js.Dynamic]
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

  private def populateQuotes(channels: js.Array[NewsFeed]) = {
    // gather the quotes
    val myQuotes = channels.flatMap { channel =>
      val items = channel.items
      items.flatMap(_.quotes)
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

/**
 * News Controller Singleton
 * @author lawrence.daniels@gmail.com
 */
object NewsController {
  val ViewTypeCookie = "NewsController_view"

}
