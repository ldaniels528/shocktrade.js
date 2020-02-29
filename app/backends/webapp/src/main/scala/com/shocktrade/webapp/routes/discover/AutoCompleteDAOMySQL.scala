package com.shocktrade.webapp.routes.discover

import com.shocktrade.common.models.quote.AutoCompleteQuote
import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Auto-Complete DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class AutoCompleteDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with AutoCompleteDAO {

  override def search(searchTerm: String, maxResults: Int)(implicit ec: ExecutionContext): Future[js.Array[AutoCompleteQuote]] = {
    conn.queryFuture[AutoCompleteQuote](
      s"""|SELECT symbol, name, exchange, assetType
         |FROM stocks
         |WHERE symbol LIKE '$searchTerm%'
         |OR name LIKE '$searchTerm%'
         |""".stripMargin
    ) map { case (rows, _ ) => rows }
  }

}
