package com.shocktrade.webapp.routes.discover

import com.shocktrade.common.models.rss.RSSChannel
import com.shocktrade.webapp.routes.discover.NewRSSFeedParser._

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
 * RSS Feed Parser
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class NewRSSFeedParser {

  /**
   * Parses the RSS feed represented by the given URL
   * @param url the given URL
   * @param ec  the implicit [[ExecutionContext execution context]]
   * @return a promise of a collection of RSS channels
   */
  def parse(url: String)(implicit ec: ExecutionContext): js.Promise[js.Array[RSSChannel]] = new Parser().parseURL(url)

}

/**
 * RSS Feed Parser Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object NewRSSFeedParser {

  @js.native
  @JSImport("rss-parser", JSImport.Namespace)
  class Parser extends js.Object {

    /**
     * @example {{{
     * let feed = await parser.parseURL('https://www.reddit.com/.rss');
     *   console.log(feed.title);
     *
     *   feed.items.forEach(item => {
     *     console.log(item.title + ':' + item.link)
     *   });
     * }}}
     * @return
     * @see [[https://www.npmjs.com/package/rss-parser]]
     */
    def parseURL(url: String): js.Promise[js.Array[RSSChannel]] = js.native

  }

}