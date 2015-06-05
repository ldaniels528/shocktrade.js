import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import scala.xml.{Node, XML}

object RSSParserTest {

  def main(args: Array[String]) {
    parseRSS(XML.loadFile(new File("./testscript/cnbc.xml"))) foreach (println)
  }

  def parseRSS(rss: Node): Seq[RSSChannel] = {
    // create the date parser (e.g. "Thu, 13 Mar 2014 03:23 GMT")
    implicit val sdf = new SimpleDateFormat("EEE',' dd MMM yyyy HH:mm z")

    // convert the XML into RSS channel
    (rss \ "channel") map { channel =>
      RSSChannel(
        (channel \ "title").headOption map (_.text),
        (channel \ "description").headOption map (_.text),
        (channel \ "link").headOption map (_.text),
        (channel \ "pubDate").headOption map (_.text) map (parseDate(_)),
        (channel \ "lastBuildDate").headOption map (_.text) map (parseDate(_)),
        (channel \ "language").headOption map (_.text),
        (channel \ "ttl").headOption map (_.text) map (_.toInt),
        (channel \ "item") map { item =>
          RSSItem(
            (item \ "title").headOption map (_.text),
            (item \ "description").headOption map (_.text),
            (item \ "link").headOption map (_.text),
            (item \ "pubDate").headOption map (_.text) map (parseDate(_)))
        })
    }
  }

  def parseDate(s: String)(implicit sdf: SimpleDateFormat): Date = {
    sdf.parse(s)
  }

  case class RSSChannel(
                         title: Option[String],
                         description: Option[String],
                         link: Option[String],
                         pubDate: Option[Date],
                         lastBuildDate: Option[Date],
                         language: Option[String],
                         ttl: Option[Int],
                         items: Seq[RSSItem])

  case class RSSItem(
                      title: Option[String],
                      description: Option[String],
                      link: Option[String],
                      pubDate: Option[Date])

}