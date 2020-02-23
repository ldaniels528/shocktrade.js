package com.shocktrade.server.dao
package securities

import com.shocktrade.common.models.quote._
import com.shocktrade.server.dao.securities.mongo.SecuritiesDAOMongoDB
import io.scalajs.npm.mongodb._
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Securities DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait SecuritiesDAO {

  def exploreIndustries(sector: String): Future[js.Array[ExploreQuote]]

  def exploreSectors(implicit ec: ExecutionContext): Future[js.Array[ExploreQuote]]

  def exploreSubIndustries(sector: String, industry: String): Future[js.Array[ExploreQuote]]

  def findCompleteQuote(symbol: String)(implicit ec: ExecutionContext): Future[Option[CompleteQuote]]

  def findQuotesByIndustry(sector: String, industry: String): Future[js.Array[ResearchQuote]]

  def findQuotesBySubIndustry(sector: String, industry: String, subIndustry: String): Future[js.Array[ResearchQuote]]

  def findQuotesBySymbols[T <: js.Any](symbols: Seq[String], fields: Seq[String]): Future[js.Array[T]]

  def search(searchTerm: String, maxResults: Int): Future[js.Array[AutoCompleteQuote]]

}

/**
 * Securities DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object SecuritiesDAO {

  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): SecuritiesDAO = ???

  def apply(db: Db): SecuritiesDAO = new SecuritiesDAOMongoDB(db.collection("Securities"))

}
