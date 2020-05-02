package com.shocktrade.webapp

import com.shocktrade.server.common.TradingClock
import com.shocktrade.webapp.routes.account.dao.UserDAO
import com.shocktrade.webapp.routes.contest.dao._
import com.shocktrade.webapp.routes.discover.dao._
import com.shocktrade.webapp.routes.research.dao.ResearchDAO
import com.shocktrade.webapp.routes.social.dao.PostDAO
import io.scalajs.npm.express.{Request, Response}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js

/**
 * Routes package object
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
package object routes {

  type NextFunction = js.Function0[Unit]

  object dao {

    implicit val autoCompleteDAO: AutoCompleteDAO = AutoCompleteDAO()
    implicit val contestDAO: ContestDAO = ContestDAO()
    implicit val exploreDAO: ExploreDAO = ExploreDAO()
    implicit val globalSearchDAO: GlobalSearchDAO = GlobalSearchDAO()
    implicit val newsDAO: RSSFeedDAO = RSSFeedDAO()
    implicit val portfolioDAO: PortfolioDAO = PortfolioDAO()
    implicit val postDAO: PostDAO = PostDAO()
    implicit val researchDAO: ResearchDAO = ResearchDAO()
    implicit val stockQuoteDAO: StockQuoteDAO = StockQuoteDAO()
    implicit val tradingClock: TradingClock = new TradingClock()
    implicit val userDAO: UserDAO = UserDAO()

  }

  /**
   * Request Extensions
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  implicit class RequestExtensions(val request: Request) extends AnyVal {

    def getMaxResults(default: Int = 20): Int = request.query.get("maxResults") map (_.toInt) getOrElse default

    def getSymbol: String = request.params("symbol").toUpperCase()

  }

  /**
   * Parameter Extensions
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  implicit class ParameterExtensions(val params: js.Dictionary[String]) extends AnyVal {

    @inline
    def extractParams(names: String*): Option[Seq[String]] = {
      val values = names.map(params.get)
      if (values.forall(_.isDefined)) Some(values.flatten) else None
    }
  }

  /**
   * Response Extensions
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  implicit class ResponseExtensions(val response: Response) extends AnyVal {

    def showException(e: Throwable): response.type = {
      e.printStackTrace()
      response
    }

    @inline
    def missingParams(params: String*): Unit = {
      val message = s"Bad Request: ${params.mkString(" and ")} ${if (params.length == 1) "is" else "are"} required"
      response.status(400).send(message)
    }

    private def asString(value: js.Any): String = value match {
      case v if v == null => ""
      case v if js.typeOf(v) == "string" => s""""${v.toString}""""
      case v => v.toString
    }

  }

}
