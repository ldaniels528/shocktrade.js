package com.shocktrade.services.directory

import com.shocktrade.services.directory.ServiceDirectory._
import com.shocktrade.services.directory.ServiceDirectoryTests._
import com.github.ldaniels528.tabular.Tabular
import org.junit.{Assert, Test}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Service Directory Tests
 * @author lawrence.daniels@gmail.com
 */
class ServiceDirectoryTests {

  import WxServiceDirectory._

  @Test
  def testSingle() {
    val results =
      select[ComprehensiveQuote]("symbol", "lastTrade", "lastTrade", "chg") from ("Quotes") where ("symbol" -> "AMX") !!

    Assert.assertTrue(results.loadResults.size == 1 && results.pctLoaded == 1.0)
  }

  @Test
  def testMultiple() {
    val results =
      select[ComprehensiveQuote]("symbol", "name", "lastTrade", "chg", "chgPct") from ("Quotes") where ("symbol" -> "AMX") !!

    Assert.assertTrue(results.loadResults.size >= 2 && results.pctLoaded == 1.0)
  }

  @Test
  def testMissingFields() {
    val results =
      select[ComprehensiveQuote]("symbol", "lastTrade", "open", "close", "chg", "chgPct", "beta", "target1Y") from ("Quotes") where ("symbol" -> "AMX") !!

    Assert.assertTrue(results.loadResults.size >= 4 && results.pctLoaded < 1.0)
  }

}

/**
 * Service Directory Tests Singleton
 * @author lawrence.daniels@gmail.com
 */
object ServiceDirectoryTests {
  private val logger = org.slf4j.LoggerFactory.getLogger(getClass())
  private val tables = new Tabular()

  ///////////////////////////////  Mock Service & Data Objects ///////////////////////////////

  class YFStockQuoteService() {

    def getQuote(symbol: String, params: String)(implicit ec: ExecutionContext) = Future {
      YFStockQuote("AMX", 123.22, 1.45)
    }
  }

  class YFKeyStatisticsService() {

    def getKeyStatistics(symbol: String)(implicit ec: ExecutionContext) = Future {
      YFKeyStatistics("AMX", Some(1.0), Some(2.3), Some(7.6))
    }
  }

  class BCStockQuoteService() {

    def getQuote(symbol: String)(implicit ec: ExecutionContext) = Future {
      BBStockQuote("AMX", Some(123.22), Some(121.11), Some(123.22), Some(1.45))
    }
  }

  class BCProfileService() {

    def getProfile(symbol: String)(implicit ec: ExecutionContext) = Future {
      BCProfile("AMX", Some("AMX, Inc."), Some(123.22), Some(0.045))
    }
  }

  case class YFKeyStatistics(symbol: String, beta: Option[Double], eps: Option[Double], peRatio: Option[Double])

  case class YFStockQuote(symbol: String, lastTrade: java.lang.Double, chg: java.lang.Double)

  case class BBStockQuote(symbol: String, lastTrade: Option[Double], open: Option[Double], close: Option[Double], chg: Option[Double])

  case class BCProfile(symbol: String, name: Option[String], lastTrade: Option[Double], chgPct: Option[Double])

  ///////////////////////////////  Example Implementation ///////////////////////////////

  /**
   * The service directory provides a means for discovering the service (or services)
   * that are required to fulfill a data request
   * @author lawrence.daniels@gmail.com
   */
  object WxServiceDirectory extends ServiceDirectory {

    import scala.beans.BeanProperty

    // let's create a new service domain
    "Quotes" ~>
      (WxYFKeyStatisticsService() ::
        WxYFStockQuoteService() ::
        WxBCStockQuoteService() ::
        WxBCProfileService() ::
        Nil)

    /**
     * Represents the target data object
     */
    class ComprehensiveQuote() {

      import java.lang._

      @BeanProperty var symbol: String = _
      @BeanProperty var name: String = _
      @BeanProperty var lastTrade: Double = _
      @BeanProperty var open: Double = _
      @BeanProperty var close: Double = _
      @BeanProperty var chg: Double = _
      @BeanProperty var chgPct: Double = _
      @BeanProperty var beta: Double = _
      @BeanProperty var eps: Double = _
      @BeanProperty var peRatio: Double = _
    }

    /**
     * WebApp Yahoo! Finance: Key Statistics Service
     * @author lawrence.daniels@gmail.com
     */
    case class WxYFKeyStatisticsService() extends YFKeyStatisticsService with AssetDataSource[ComprehensiveQuote] {
      val provides = extractFields(classOf[YFKeyStatistics])
      private val myFields = extractFields(classOf[YFKeyStatistics])

      override def load(fields: Set[String], conditions: Seq[(String, Any)], cq: ComprehensiveQuote)(implicit ec: ExecutionContext) = {
        val symbol = getCondition[String]("symbol", conditions)
        for {
          ks <- getKeyStatistics(symbol)

          result = {
            cq.symbol = ks.symbol
            cq.beta = <~(ks.beta)
            cq.eps = <~(ks.eps)
            cq.peRatio = <~(ks.peRatio)
            Seq(SuccessResult(this, ks))
          }
        } yield result
      }

      def provides(fields: Set[String]): Set[String] = myFields
    }

    /**
     * WebApp Yahoo! Finance: Stock Quote Service
     * @author lawrence.daniels@gmail.com
     */
    case class WxYFStockQuoteService() extends YFStockQuoteService with AssetDataSource[ComprehensiveQuote] {
      val provides = extractFields(classOf[YFStockQuote])
      private val myFields = extractFields(classOf[YFStockQuote])

      override def load(fields: Set[String], conditions: Seq[(String, Any)], cq: ComprehensiveQuote)(implicit ec: ExecutionContext) = {
        val symbol = getCondition[String]("symbol", conditions)
        val params = generateParams(fields)

        for {
          q <- getQuote(symbol, params)

          result = {
            cq.symbol = q.symbol
            cq.lastTrade = q.lastTrade
            cq.chg = q.chg
            Seq(SuccessResult(this, q))
          }
        } yield result
      }

      def provides(fields: Set[String]): Set[String] = myFields & fields

      private def generateParams(fields: Set[String]) = {
        fields flatMap mapping.get mkString
      }

      private val mapping = Map(
        "symbol" -> "s",
        "name" -> "n",
        "lastTrade" -> "l1",
        "chg" -> "c1")
    }

    /**
     * WebApp BarChart Stock Quote Service
     * @author lawrence.daniels@gmail.com
     */
    case class WxBCStockQuoteService() extends BCStockQuoteService with AssetDataSource[ComprehensiveQuote] {
      val provides = extractFields(classOf[BBStockQuote])
      private val myFields = extractFields(classOf[BBStockQuote])

      override def load(fields: Set[String], conditions: Seq[(String, Any)], cq: ComprehensiveQuote)(implicit ec: ExecutionContext) = {
        val symbol = getCondition[String]("symbol", conditions)
        for {
          q <- getQuote(symbol)

          result = {
            cq.symbol = q.symbol
            cq.lastTrade = <~(q.lastTrade)
            cq.open = <~(q.open)
            cq.close = <~(q.close)
            cq.chg = <~(q.chg)
            Seq(SuccessResult(this, q))
          }
        } yield result
      }

      def provides(fields: Set[String]): Set[String] = myFields
    }

    /**
     * WebApp BarChart Profile Service
     * @author lawrence.daniels@gmail.com
     */
    case class WxBCProfileService() extends BCProfileService with AssetDataSource[ComprehensiveQuote] {
      val provides = extractFields(classOf[BCProfile])
      private val myFields = extractFields(classOf[BCProfile])

      override def load(fields: Set[String], conditions: Seq[(String, Any)], cq: ComprehensiveQuote)(implicit ec: ExecutionContext) = {
        val symbol = getCondition[String]("symbol", conditions)
        for {
          p <- getProfile(symbol)

          result = {
            cq.symbol = p.symbol
            cq.name = <~(p.name)
            cq.lastTrade = <~(p.lastTrade)
            cq.chgPct = <~(p.chgPct)
            Seq(SuccessResult(this, p))
          }
        } yield result
      }

      def provides(fields: Set[String]): Set[String] = myFields
    }

    /**
     * WebApp Service Output Logger
     * @author lawrence.daniels@gmail.com
     */
    implicit class WxServiceLogger[T](query: Future[Solution[T]]) {

      def !![S](): Solution[T] = {
        // find and invoke the qualified service(s)
        val solution = Await.result(query, 5.seconds)
        val loadResults = solution.loadResults
        val objects = loadResults flatMap {
          case SuccessResult(_, data, _) => Some(data)
          case _ => None
        }

        // display the data
        val pctLoaded = 100.0 * solution.pctLoaded
        logger.info(f"${loadResults.size} source(s) used: $pctLoaded%.01f%% loaded - $objects");
        tables.transform(Seq(solution.bean)) foreach logger.info
        println("")
        solution
      }
    }

  }

}