package com.shocktrade.webapp.routes.discover

import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Stock Quote DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class StockQuoteDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with StockQuoteDAO {

  override def findQuote[A <: js.Any](symbol: String)(implicit ec: ExecutionContext): Future[Option[A]] = {
    conn.queryFuture[A]("SELECT * FROM stocks WHERE symbol = ?", js.Array(symbol)) map { case (rows, _) => rows.headOption }
  }

  override def findQuotes[A <: js.Any](symbols: Seq[String])(implicit ec: ExecutionContext): Future[js.Array[A]] = {
    conn.queryFuture[A]("SELECT * FROM stocks WHERE symbol IN ( ? )", Seq(symbols)) map { case (rows, _) => rows }
  }

}
