package com.shocktrade.services

import com.shocktrade.services.USTreasuryBillRatesService._
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Node, NodeSeq}

/**
 * United States Treasury Bill Rates Service
 * @author lawrence.daniels@gmail.com
 */
class USTreasuryBillRatesService {

  /**
   * Retrieves all U.S. treasury bill rates
   * @return a { @link Future future} of a { @link BCBusinessProfile business profile}
   */
  def getBillRates()(implicit ec: ExecutionContext): Future[Seq[USTBillRate]] = {
    import dispatch.{Http, as, url}

    for {
    // retrieve the document
      doc <- Http.configure(_ setFollowRedirects true)(url(s"http://www.treasury.gov/resource-center/data-chart-center/interest-rates/Datasets/daily_treas_bill_rates.xml") OK as.xml.Elem)

    // transform the document into a business profile
    } yield parseDocument(doc)
  }

  private def parseDocument(doc: NodeSeq): Seq[USTBillRate] = {
    (doc \\ "G_CS_4WK_CLOSE_AVG") flatMap { table =>
      // TODO finish this
      table map toUSTBillRate
    }
  }

  private def toUSTBillRate(node: Node): USTBillRate = USTBillRate()

}

/**
 * United States Treasury Bill Rates Service Singleton
 * @author lawrence.daniels@gmail.com
 */
object USTreasuryBillRatesService {

  case class USTBillRate()

}