package com.shocktrade.util

import java.text.SimpleDateFormat
import java.util.Date

import play.api.Logger
import RSSReader._

import scala.beans.BeanProperty
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.xml.{Node, XML}

/**
 * RSS Reader
 * @author lawrence.daniels@gmail.com
 */
class RSSReader() {
  private val logger = Logger(getClass)

  /**
   * Retrieves an RSS feed
   */
  def getFeed(rssUrl: String)(implicit ec: ExecutionContext): Future[RSSChannel] = {
    import org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl

    for {
    // asynchronously retrieve the RSS feed
      rss <- Future {
        XML
          .withSAXParser(new SAXFactoryImpl().newSAXParser())
          .load(rssUrl)
      }

      // retrieve the channel
      results = for {
        node <- rss \\ "channel"

        channel = toChannel(node)
      } yield channel

    } yield results.head
  }

  private def toChannel(node: Node): RSSChannel = {
    import scala.collection.JavaConversions._

    val title = (node \ "title").text
    val link = (node \ "link").text
    val description = (node \ "description").text
    val language = (node \ "language").text
    val copyright = (node \ "copyright").text
    val pubDate = parseDate((node \ "pubDate").text)
    val ttl = parseInt((node \ "ttl").text)
    val items = (node \ "item") map toItem
    val image = ((node \ "image") map toChannelImage).headOption.orNull

    // create the RSS channel
    RSSChannel(title, description, pubDate, link, language, copyright, ttl, items, image)
  }

  private def toItem(node: Node): RSSItem = {
    val title = (node \ "title").text
    val description = (node \ "description").text
    val pubDate = parseDate((node \ "pubDate").text)
    val link = (node \ "link").text
    val guid = (node \ "guid").text
    val thumbNail = ((node \ "thumbnail") map toThumbNail).headOption.orNull

    // create the RSS item
    RSSItem(title, description, pubDate, link, guid, thumbNail)
  }

  private def toChannelImage(node: Node): RSSImage = {
    val title = (node \ "title").text
    val description = (node \ "description").text
    val link = (node \ "link").text
    val url = (node \ "url").text
    val width = parseInt((node \ "width").text)
    val height = parseInt((node \ "height").text)

    // create the RSS image
    RSSImage(title, description, url, link, width, height)
  }

  private def toThumbNail(node: Node): RSSThumbNail = {
    val url = (node \ "@url").text
    val width = parseInt((node \ "@width").text)
    val height = parseInt((node \ "@height").text)

    // create the RSS thumb-nail
    RSSThumbNail(url, width, height)
  }

  private def parseInt(value: String): Int = {
    Try(value.toInt) match {
      case Success(date) => date
      case Failure(e) =>
        logger.error(s"Error parsing date: ${e.getMessage}")
        0
    }
  }

  private def parseDate(dataString: String): Date = {
    // attempt format #1 (e.g. "Mon, 20 Jan 2014 22:11 GMT")
    parseDate(dataString, new SimpleDateFormat("E',' dd MMM yyyy HH:mm z")).getOrElse {
      // attempt format #2 (e.g. "Mon, 20 Jan 2014 16:02:58 EST")
      parseDate(dataString, new SimpleDateFormat("E',' dd MMM yyyy HH:mm:ss z")).orNull
    }
  }

  private def parseDate(dateString: String, fomatter: SimpleDateFormat): Option[Date] = {
    Try(fomatter.parse(dateString)) match {
      case Success(date) => Some(date)
      case Failure(e) => None
    }
  }
}

/**
 * RSS Reader Singleton
 * @author lawrence.daniels@gmail.com
 */
object RSSReader {

  case class RSSChannel(@BeanProperty title: String,
                        @BeanProperty description: String,
                        @BeanProperty pubDate: Date,
                        @BeanProperty link: String,
                        @BeanProperty language: String,
                        @BeanProperty copyright: String,
                        @BeanProperty ttl: Int,
                        @BeanProperty items: java.util.List[RSSItem],
                        @BeanProperty image: RSSImage)

  case class RSSImage(@BeanProperty title: String,
                      @BeanProperty description: String,
                      @BeanProperty url: String,
                      @BeanProperty link: String,
                      @BeanProperty width: Int,
                      @BeanProperty height: Int)

  case class RSSItem(@BeanProperty title: String,
                     @BeanProperty var description: String,
                     @BeanProperty pubDate: Date,
                     @BeanProperty link: String,
                     @BeanProperty guid: String,
                     @BeanProperty thumbNail: RSSThumbNail)

  case class RSSThumbNail(@BeanProperty url: String,
                          @BeanProperty width: Int,
                          @BeanProperty height: Int)

}