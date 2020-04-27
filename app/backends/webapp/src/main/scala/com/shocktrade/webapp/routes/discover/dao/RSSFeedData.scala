package com.shocktrade.webapp.routes.discover.dao

import scala.scalajs.js

/**
 * News Source Data Object
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class RSSFeedData(val rss_feeds_id: js.UndefOr[String] = js.undefined,
                  val name: js.UndefOr[String] = js.undefined,
                  val url: js.UndefOr[String] = js.undefined,
                  val priority: js.UndefOr[Int] = js.undefined) extends js.Object
