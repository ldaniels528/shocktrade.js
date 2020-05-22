package com.shocktrade.ingestion.daemons.onetime

import com.shocktrade.server.common.LoggerFactory
import io.scalajs.nodejs.fs.Fs
import io.scalajs.nodejs.setImmediate

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Wikipedia Company One-time Loader
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class WikipediaCompanyLoader()(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val dao = WikipediaCompanyDAO()

  def run()(implicit ec: ExecutionContext): Unit = {
    val outcome = for {
      contents <- Fs.readFileFuture("./temp/stockinfo.csv")
      lines = contents.toString.split("[\n]").toList
      records = lines map toWikipediaCompanyData
      count <- insert(records)
    } yield (lines, count)

    outcome onComplete {
      case Success((lines, count)) =>
        logger.info(s"Inserted $count of ${lines.size} records.")
      case Failure(e) =>
        logger.error(s"Wikipedia Company load failure: ${e.getMessage}")
    }
  }

  private def insert(records: List[WikipediaCompanyData]): Future[Int] = {
    val promise = Promise[Int]()
    var list: List[Int] = Nil

    def recurse(myItems: List[WikipediaCompanyData]): Unit = {
      myItems match {
        case item :: items =>
          dao.insert(item) onComplete {
            case Success(count) =>
              list = count :: list
              recurse(items)
            case Failure(e) =>
              logger.error(e.getMessage)
              recurse(items)
          }
        case Nil => promise.success(list.sum)
      }
    }

    setImmediate(() => recurse(records))
    promise.future
  }

  private def extract(values: Seq[String], index: Int): js.UndefOr[String] = if (index < values.size) values(index) else js.undefined

  private def toWikipediaCompanyData(line: String): WikipediaCompanyData = {
    // WAB|Wabtec Corporation|reports|Industrials|Construction Machinery & Heavy Trucks|Wilmerding, Pennsylvania|2019-02-27|0000943452|1999 (1869)
    val values = line.split("[|]")
    new WikipediaCompanyData(
      symbol = extract(values, 0),
      name = extract(values, 1),
      sector = extract(values, 3),
      industry = extract(values, 4),
      cityState = extract(values, 5),
      initialReportingDate = extract(values, 6),
      cikNumber = extract(values, 7),
      yearFounded = extract(values, 8)
    )
  }

}
