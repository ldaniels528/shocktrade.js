package com.shocktrade.webapp.routes.discover

import com.shocktrade.webapp.routes.discover.RSSFeedParser.{RSSChannel, XMLRSSRoot}
import io.scalajs.npm.request.Request
import io.scalajs.npm.xml2js.Xml2js
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
  * RSS Feed Parser
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class RSSFeedParser() {

  /**
    * Parses the RSS feed represented by the given URL
    * @param url the given URL
    * @param ec  the implicit [[ExecutionContext execution context]]
    * @return a promise of a collection of RSS channels
    */
  def parse(url: String)(implicit ec: ExecutionContext): Future[js.Array[RSSChannel]] = {
    for {
      (response, body) <- Request.getFuture(url)
      _ = if (response.statusCode != 200) die(s"HTTP/${response.statusCode}: ${response.statusMessage}")
      feedsXML <- Xml2js.parseStringFuture[XMLRSSRoot](body.toString)
    } yield feedsXML.toJson
  }

  def parseRaw(url: String)(implicit ec: ExecutionContext): Future[XMLRSSRoot] = {
    for {
      (response, body) <- Request.getFuture(url)
      _ = if (response.statusCode != 200) die(s"HTTP/${response.statusCode}: ${response.statusMessage}")
      feedsXML <- Xml2js.parseStringFuture[XMLRSSRoot](body.toString)
    } yield feedsXML
  }

}

/**
  * RSS Feed Parser Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object RSSFeedParser {

  ////////////////////////////////////////////////////////////
  //    JSON RSS Model
  ////////////////////////////////////////////////////////////

  class RSSChannel(val title: js.UndefOr[String],
                   val description: js.UndefOr[String],
                   val link: js.UndefOr[String],
                   val pubDate: js.UndefOr[Double],
                   val language: js.UndefOr[String],
                   val copyright: js.UndefOr[String],
                   val ttl: js.UndefOr[Double],
                   val images: js.Array[RSSImage],
                   val items: js.Array[RSSItem]) extends js.Object

  class RSSImage(val title: js.UndefOr[String],
                 val description: js.UndefOr[String],
                 val link: js.UndefOr[String],
                 val url: js.UndefOr[String],
                 val width: js.UndefOr[Double],
                 val height: js.UndefOr[Double]) extends js.Object

  class RSSItem(val title: js.UndefOr[String],
                val description: js.UndefOr[String],
                val link: js.UndefOr[String],
                val pubDate: js.UndefOr[Double],
                val guid: js.UndefOr[String],
                val thumbNail: js.UndefOr[RSSMediaThumbNail]) extends js.Object

  class RSSMediaThumbNail(val url: js.UndefOr[String],
                          val width: js.UndefOr[Double],
                          val height: js.UndefOr[Double]) extends js.Object

  ////////////////////////////////////////////////////////////
  //    XML RSS Model
  ////////////////////////////////////////////////////////////

  @js.native
  trait XMLRSSRoot extends js.Object {
    var rss: XMLRSSFeed = js.native
  }

  @js.native
  trait XMLRSSFeed extends js.Object {
    var channel: js.Array[XMLRSSChannel] = js.native
  }

  @js.native
  trait XMLRSSChannel extends js.Object {
    var title: js.Array[String] = js.native
    var description: js.Array[String] = js.native
    var link: js.Array[String] = js.native
    var language: js.Array[String] = js.native
    var copyright: js.Array[String] = js.native
    var pubDate: js.Array[String] = js.native
    var ttl: js.Array[String] = js.native
    var image: js.Array[XMLRSSImage] = js.native
    var item: js.Array[XMLRSSItem] = js.native
  }

  @js.native
  trait XMLRSSImage extends js.Object {
    var title: js.Array[String] = js.native
    var description: js.Array[String] = js.native
    var link: js.Array[String] = js.native
    var url: js.Array[String] = js.native
    var width: js.Array[String] = js.native
    var height: js.Array[String] = js.native
  }

  @js.native
  trait XMLRSSItem extends js.Object {
    var title: js.Array[String] = js.native
    var description: js.Array[String] = js.native
    var guid: js.Array[String] = js.native
    var link: js.Array[String] = js.native
    var pubDate: js.Array[String] = js.native
  }

  @js.native
  trait XMLRSSMediaThumbNail extends js.Object {
    var url: js.UndefOr[String] = js.native
    var width: js.UndefOr[String] = js.native
    var height: js.UndefOr[String] = js.native
  }

  ////////////////////////////////////////////////////////////
  //    Conversion Logic
  ////////////////////////////////////////////////////////////

  /**
    * RSS root conversions
    * @param root the given [[XMLRSSRoot XML RSS root]]
    */
  final implicit class RSSRootConversions(val root: XMLRSSRoot) extends AnyVal {

    @inline
    def toJson: js.Array[RSSChannel] = root.rss.channel.map(_.toJson)
  }

  /**
    * XML RSS channel conversions
    * @param channel the given [[XMLRSSChannel XML RSS channel]]
    */
  final implicit class RSSChannelConversions(val channel: XMLRSSChannel) extends AnyVal {

    @inline
    def toJson = new RSSChannel(
      title = channel.title.headOption.orUndefined,
      description = channel.description.headOption.orUndefined,
      link = channel.link.headOption.orUndefined,
      language = channel.language.headOption.orUndefined,
      copyright = channel.copyright.headOption.orUndefined,
      pubDate = channel.pubDate.headOption.map(js.Date.parse).orUndefined,
      ttl = channel.ttl.headOption.map(_.toDouble).orUndefined,
      images = channel.image.map(_.toJson),
      items = channel.item.map(_.toJson)
    )
  }

  /**
    * XML RSS image conversions
    * @param image the given [[XMLRSSImage XML RSS image]]
    */
  final implicit class RSSImageConversions(val image: XMLRSSImage) extends AnyVal {

    @inline
    def toJson = new RSSImage(
      title = image.title.headOption.orUndefined,
      description = image.description.headOption.orUndefined,
      link = image.link.headOption.orUndefined,
      url = image.url.headOption.orUndefined,
      width = image.width.headOption.map(_.toDouble).orUndefined,
      height = image.height.headOption.map(_.toDouble).orUndefined
    )
  }

  /**
    * XML RSS item conversions
    * @param item the given [[XMLRSSItem XML RSS item]]
    */
  final implicit class RSSItemConversions(val item: XMLRSSItem) extends AnyVal {

    @inline
    def toJson = new RSSItem(
      title = item.title.headOption.orUndefined,
      description = item.description.headOption.orUndefined,
      link = item.link.headOption.orUndefined,
      guid = item.guid.headOption.orUndefined,
      thumbNail = getThumbNail,
      pubDate = item.pubDate.headOption.map(js.Date.parse).orUndefined
    )

    /**
      * {{{
      * "media:thumbnail": [{
      *   "$": {
      *     "url": "http:\/\/i2.cdn.turner.com\/money\/dam\/assets\/160816101117-neuromama-stock-120x90.jpg",
      *     "height": "90",
      *     "width": "120"
      *   }
      * }]
      * }}}
      */
    @inline
    def getThumbNail: js.UndefOr[RSSMediaThumbNail] = {
      val outerDict = item.asInstanceOf[js.Dictionary[js.Array[js.Dictionary[XMLRSSMediaThumbNail]]]]
      (for {
        innerDict <- outerDict.get("media:thumbnail").flatMap(_.headOption)
        thumbNail <- innerDict.get("$")
      } yield new RSSMediaThumbNail(
        url = thumbNail.url,
        width = thumbNail.width.map(_.toDouble),
        height = thumbNail.height.map(_.toDouble))).orUndefined
    }

  }

}