package com.shocktrade.webapp.routes.discover.dao

import com.shocktrade.common.models.EntitySearchResult
import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Global Search DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class GlobalSearchDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with GlobalSearchDAO {

  override def search(searchTerm: String, maxResults: Int)(implicit ec: ExecutionContext): Future[js.Array[EntitySearchResult]] = {
    val term = s"$searchTerm%".toUpperCase
    conn.queryFuture[EntitySearchResult](
      s"""|SELECT symbol AS _id, symbol AS name, name AS description, 'STOCK' AS type
          |FROM stocks
          |WHERE name LIKE '$term' OR symbol LIKE '$term'
          |         UNION
          |SELECT userID AS _id, username AS name, 'Investor/Trader' AS description, 'USER' AS type
          |FROM users
          |WHERE UPPER(username) LIKE '$term'
          |         UNION
          |SELECT contestID AS _id, name, 'Simulation' AS description, 'SIMULATION' AS type
          |FROM contests
          |WHERE UPPER(name) LIKE '$term'
          |LIMIT $maxResults
          |""".stripMargin) map { case (rows, _) => rows }
  }

}
