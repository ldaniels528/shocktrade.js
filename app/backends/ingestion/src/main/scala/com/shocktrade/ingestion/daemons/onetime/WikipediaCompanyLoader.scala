package com.shocktrade.ingestion.daemons.onetime

import com.shocktrade.server.common.LoggerFactory
import io.scalajs.nodejs.fs.Fs

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Wikipedia Company One-time Loader
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class WikipediaCompanyLoader() {
  private val logger = LoggerFactory.getLogger(getClass)
  private val dao = WikipediaCompanyDAO()

  def run()(implicit ec: ExecutionContext): Unit = {
    Fs.readFileFuture("./temp/stockinfo.csv") onComplete {
      case Success(contents) =>
        val lines = contents.toString.split("[\n]")
        lines foreach { line =>
          logger.info(line)
          val values = line.split("[|]")
          dao.insert(new WikipediaCompanyData(
            symbol = extract(values, 0),
            name = extract(values, 1),
            sector = extract(values, 2),
            industry = extract(values, 3),
            cityState = extract(values, 4),
            initialReportingDate = extract(values, 5),
            cikNumber = extract(values, 6),
            yearFounded = extract(values, 7)
          ))
        }
      case Failure(e) =>
        logger.error(s"Wikipedia Company load failure: ${e.getMessage}")
    }
  }

  private def extract(values: Seq[String], index: Int): js.UndefOr[String] = if(values.size > index) values(index) else js.undefined

}
