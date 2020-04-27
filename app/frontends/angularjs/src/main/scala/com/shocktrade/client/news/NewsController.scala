package com.shocktrade.client.news

import com.shocktrade.client.GlobalLoading
import com.shocktrade.client.news.NewsController._
import com.shocktrade.common.models.rss._
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.cookies.Cookies
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.sanitize.Sce
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Scope, angular, injected}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * News Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NewsController($scope: NewsScope, $cookies: Cookies, $sce: Sce, toaster: Toaster,
                     @injected("RSSFeedService") rssFeedService: RSSFeedService)
  extends Controller with GlobalLoading {

  // define the scope variables
  // view: get the previously selected view from the cookie
  $scope.selection = js.undefined
  $scope.view = $cookies.getOrElse(ViewTypeCookie, "list")

  $scope.test = """<i>This is it!</i>"""

  /////////////////////////////////////////////////////////////////////////////
  //			Initialization Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.init = () => {
    console.log("Loading news sources...")
    val outcome = (for {
      sources <- rssFeedService.getNewsSources
      feedId = sources.data.flatMap(_.rssFeedID.toOption).headOption.getOrElse(throw js.JavaScriptException("No feeds found"))
      channels <- rssFeedService.getNewsFeeds(feedId)
    } yield (sources.data, channels.data)).toJSPromise

    /*asyncLoading($scope)(outcome)*/ outcome onComplete {
      case Success((sources, channels)) =>
        $scope.$apply { () =>
          $scope.newsSources = sources
          $scope.newsChannels = channels
          $scope.selection = sources.headOption.orUndefined
          //$scope.test = generate(channels)
        }
      case Failure(e) =>
        toaster.error("Failed to load news sources")
        e.printStackTrace()
    }
    outcome
  }

  /////////////////////////////////////////////////////////////////////////////
  //			Public Functions
  /////////////////////////////////////////////////////////////////////////////

  $scope.getNewsFeed = (aFeedId: js.UndefOr[String]) => aFeedId.map(findNewsFeed)

  /**
   * Return the appropriate class to create a diagonal grid
   */
  $scope.gridClass = (aIndex: js.UndefOr[Double]) => aIndex map (_.toInt) map { index =>
    val row = Math.floor(index / 2)
    val cell = if (row % 2 == 0) index % 2 else (index + 1) % 2
    s"news_tile$cell"
  }

  $scope.trustMe = (aHtml: js.UndefOr[String]) => aHtml map ($sce.trustAsHtml(_))

  /////////////////////////////////////////////////////////////////////////////
  //			Private Functions
  /////////////////////////////////////////////////////////////////////////////

  def findNewsFeed(feedId: String): js.Promise[HttpResponse[js.Array[RSSChannel]]] = {
    console.log("Getting news feeds...")
    val outcome = rssFeedService.getNewsFeeds(feedId)
    /*asyncLoading($scope)(outcome)*/ outcome onComplete {
      case Success(response) =>
        val channels = response
        $scope.$apply { () =>
          $scope.newsChannels = channels.data

          for {channel <- channels.data; item <- channel.items} yield {
            console.info(s"channel => ${JSON.stringify(channel)}")
            console.info(s"item => ${JSON.stringify(item)}")
          }
          //$scope.test = generate(channels)
          //populateQuotes(feedChannels) TODO
          //this.newsChannels = feedChannels; //getEnrichedChannels(feedChannels)
          //removeImageTags(feedChannels)
        }
      case Failure(e) =>
        toaster.error(s"Failed to load news feed $feedId")
    }
    outcome
  }

  private def generate(channels: js.Array[RSSChannel]): String = {
    (for {channel <- channels; item <- channel.items} yield {
      console.info(s"channel => ${JSON.stringify(channel)}")
      s"""|<div>${channel.title.orNull}</div>
          |<div>${item.title}</div>
          |<div>${channel.description}</div>
          |""".stripMargin
    }) mkString "\n"
  }

  private def removeImageTags(channels: js.Array[RSSFeed]): Unit = {
    for {
      channel <- channels
      //(_, item) <- channel.items
    } {
      /*
      val start = item.description.indexOf("""<img src="http://feeds.feedburner.com""")
      val end = item.description.indexOf("/>", start)
      if (end > start && start != -1) {
        val block = item.description.substring(start, end + 2)
        item.description = item.description.replaceAllLiterally(block, "")
      }*/
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
    $scope.newsQuotes = channels.flatMap(_.items.flatMap(_.quotes))
  }

}

/**
 * News Controller Singleton
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object NewsController {
  val ViewTypeCookie = "NewsController_view"

  /**
   * News Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait NewsScope extends Scope {
    // variables
    var newsChannels: js.UndefOr[js.Array[RSSChannel]] = js.native
    var newsSources: js.UndefOr[js.Array[NewsSource]] = js.native
    var newsQuotes: js.UndefOr[js.Array[NewsQuote]] = js.native
    var selection: js.UndefOr[NewsSource] = js.native
    var view: js.UndefOr[String] = js.native
    var test: js.UndefOr[String] = js.native

    // functions
    var init: js.Function0[js.Promise[Any]] = js.native
    var getNewsFeed: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[HttpResponse[js.Array[RSSChannel]]]]] = js.native
    var gridClass: js.Function1[js.UndefOr[Double], js.UndefOr[String]] = js.native
    var trustMe: js.Function1[js.UndefOr[String], js.UndefOr[Any]] = js.native

  }

  /**
   * News Feed Selection
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  class NewsFeedSelection(var feed: js.UndefOr[String]) extends js.Object

}