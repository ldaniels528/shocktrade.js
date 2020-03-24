package com.shocktrade.webapp.routes.research.dao

import com.shocktrade.common.forms.ResearchOptions
import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions
import io.scalajs.util.JsUnderOrHelper._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Research DAO (MySQL)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ResearchDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with ResearchDAO {

  override def research(options: ResearchOptions)(implicit ec: ExecutionContext): Future[js.Array[ResearchQuote]] = {
    // build the query
    var tuples: List[(String, List[Any])] = Nil
    toRange("changePct", options.changeMin, options.changeMax) foreach (tuple => tuples = tuple :: tuples)
    toRange("lastTrade", options.priceMin, options.priceMax) foreach (tuple => tuples = tuple :: tuples)
    toRange("spread", options.spreadMin, options.spreadMax) foreach (tuple => tuples = tuple :: tuples)
    toRange("volume", options.volumeMin, options.volumeMax) foreach (tuple => tuples = tuple :: tuples)

    // determine the maximum number of results
    val maxResults = options.maxResults.flat.getOrElse(25)

    // build the query
    val (conditions, values) = (tuples.map(_._1), tuples.flatMap(_._2))
    val query =
      s"""|SELECT * FROM stocks
          |${if (conditions.nonEmpty) s"WHERE ${conditions.mkString(" AND ")}" else ""}
          |${options.sortBy.toOption.map(field => s"ORDER BY $field ${if (options.reverse.contains(true)) "DESC" else "ASC"}").getOrElse("")}
          |LIMIT $maxResults
          |""".stripMargin
    //LoggerFactory.getLogger(getClass).info(s"query: \n$query")

    // perform the query
    conn.queryFuture[ResearchQuote](query, values) map { case (rows, _) => rows }
  }

  private def toRange(field: String, minValue: js.UndefOr[Double], maxValue: js.UndefOr[Double]): Option[(String, List[Any])] = {
    (minValue.flat.toOption, maxValue.flat.toOption) match {
      case (Some(min), Some(max)) => Some(s"$field BETWEEN ? AND ?" -> List(min, max))
      case (Some(min), None) => Some(s"$field >= ?" -> List(min))
      case (None, Some(max)) => Some(s"$field <= ?" -> List(max))
      case (None, None) => None
    }
  }

}
