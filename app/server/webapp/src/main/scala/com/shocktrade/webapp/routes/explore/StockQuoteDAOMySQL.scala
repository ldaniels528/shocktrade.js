package com.shocktrade.webapp.routes.explore

import com.shocktrade.common.models.quote.ResearchQuote
import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Stock Quote DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class StockQuoteDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with StockQuoteDAO  {

  override def findQuote(symbol: String)(implicit ec: ExecutionContext): Future[Option[ResearchQuote]] = {
    conn.queryFuture[ResearchQuote]("SELECT * FROM stocks WHERE symbol = ?", js.Array(symbol)) map { case (rows, _) => rows.headOption }
  }

  override def findQuotes(symbols: Seq[String])(implicit ec: ExecutionContext): Future[js.Array[ResearchQuote]] = {
    conn.queryFuture[ResearchQuote]("SELECT * FROM stocks WHERE symbol IN ( ? )", Seq(symbols)) map { case (rows, _) => rows }
  }

}
