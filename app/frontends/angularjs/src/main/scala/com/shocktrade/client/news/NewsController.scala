package com.shocktrade.client.news

import com.shocktrade.client.GlobalLoading
import com.shocktrade.client.news.NewsController._
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.sanitize.Sce
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Scope, angular, injected}
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * News Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NewsController($scope: NewsScope, $cookies: Cookies, $sce: Sce, toaster: Toaster,
                     @injected("NewsService") newsService: NewsService)
  extends Controller with GlobalLoading {

  private var newsChannels = emptyArray[NewsChannel]
  private var newsSources = emptyArray[NewsSource]
  private var newsQuotes = emptyArray[NewsQuote]

  // define the scope variables
  // view: get the previously selected view from the cookie
  $scope.selection = new NewsFeedSelection(feed = "")
  $scope.view = $cookies.getOrElse(ViewTypeCookie, "list")

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.getChannels = () => newsChannels

  $scope.getNewsFeed = (aFeedId: js.UndefOr[String]) => aFeedId foreach findNewsFeed

  $scope.getNewsSources = () => {
    console.log("Loading news sources...")
    asyncLoading($scope)(newsService.getNewsSources) onComplete {
      case Success(response) =>
        val sources = response.data
        $scope.$apply { () =>
          this.newsSources = sources

          // select the ID of the first feed
          sources.headOption.orUndefined.flatMap(_._id) foreach { feed =>
            $scope.selection.feed = feed
            findNewsFeed(feed)
          }
        }
      case Failure(e) =>
        toaster.error("Failed to load news sources")
    }
  }

  /**
   * Return the appropriate class to create a diagonal grid
   */
  $scope.gridClass = (aIndex: js.UndefOr[Double]) => aIndex map (_.toInt) map { index =>
    val row = Math.floor(index / 2)
    val cell = if (row % 2 == 0) index % 2 else (index + 1) % 2
    s"news_tile$cell"
  }

  $scope.newsSources = () => newsSources

  $scope.trustMe = (aHtml: js.UndefOr[String]) => aHtml map ($sce.trustAsHtml(_))

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions
  /////////////////////////////////////////////////////////////////////////////

  private def findNewsFeed(feedId: String): Unit = {
    console.log("Getting news feeds...")
    asyncLoading($scope)(newsService.getNewsFeed(feedId)) onComplete {
      case Success(response) =>
        val feedChannels = response.data
        $scope.$apply { () =>
          //populateQuotes(feedChannels) TODO
          this.newsChannels = feedChannels; //getEnrichedChannels(feedChannels)
          removeImageTags(feedChannels)
        }
      case Failure(e) =>
        toaster.error(s"Failed to load news feed $feedId")
    }
  }

  private def removeImageTags(channels: js.Array[NewsChannel]): Unit = {
    for {
      channel <- channels
      item <- channel.items
    } {
      val start = item.description.indexOf("""<img src="http://feeds.feedburner.com""")
      val end = item.description.indexOf("/>", start)
      if (end > start && start != -1) {
        val block = item.description.substring(start, end + 2)
        item.description = item.description.replaceAllLiterally(block, "")
      }
    }
  }

  private def getEnrichedChannels(channels: js.Array[NewsChannel]): js.Array[NewsChannel] = {
    channels map { channel =>
      channel.copy(items = channel.items.map(item => item.copy(description = replaceSymbols(item.description, item.quotes))))
    }
  }

  private def replaceSymbols(description: String, quotes: js.Array[NewsQuote]): String = {
    val sb = new StringBuilder(description)
    quotes foreach { q =>
      val term = s"( ${q.symbol} )"
      description.indexOf(term) match {
        case -1 => description
        case start =>
          sb.replace(start, start + term.length,
            s"""(<a href="#/discover?symbol=${q.symbol}">
                    <span ${popup(q)} class="${q.exchange}">${q.symbol}</span>
                  </a>${changeArrow(q.changePct)})""".stripPrefix(" ").stripSuffix(" ")
          )
      }
    }
    sb.toString()
  }

  private def popup(q: NewsQuote): String = {
    s"""popover-title="${q.name} (${q.exchange})"
        popover="${q.sector} &#8212; ${q.industry}"
        popover-trigger="mouseenter"
        popover-placement="right"
        """.stripPrefix(" ").stripSuffix(" ")
  }

  private def changeArrow(aChangePct: js.UndefOr[Double]): js.UndefOr[String] = aChangePct map { changePct =>
    val isNeg = changePct < 0.00
    val iconClass = if (isNeg) "fa-arrow-down" else "fa-arrow-up"
    val colorClass = if (isNeg) "negative" else "positive"
    f"""<span class="fa $iconClass $colorClass">$changePct%.2f%%</span>"""
  }

  private def populateQuotes(channels: js.Array[NewsChannel]): Unit = {
    console.log(s"channels = ${angular.toJson(channels, pretty = true)}")
    newsQuotes = channels.flatMap(_.items.flatMap(_.quotes))
  }

}

/**
 * News Controller Singleton
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object NewsController {
  val ViewTypeCookie = "NewsController_view"

}

/**
 * News Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait NewsScope extends Scope {
  // variables
  var selection: NewsFeedSelection = js.native
  var view: String = js.native

  // functions
  var getChannels: js.Function0[js.Array[NewsChannel]] = js.native
  var getNewsFeed: js.Function1[js.UndefOr[String], Unit] = js.native
  var getNewsSources: js.Function0[Unit] = js.native
  var gridClass: js.Function1[js.UndefOr[Double], js.UndefOr[String]] = js.native
  var newsSources: js.Function0[js.Array[NewsSource]] = js.native
  var trustMe: js.Function1[js.UndefOr[String], js.UndefOr[Any]] = js.native

}

/**
 * News Feed Selection
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NewsFeedSelection(var feed: js.UndefOr[String]) extends js.Object

/**
 * News Source
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait NewsSource extends js.Object {
  var _id: js.UndefOr[String] = js.native
}
