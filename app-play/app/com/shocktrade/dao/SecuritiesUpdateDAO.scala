package com.shocktrade.dao

import com.shocktrade.processors.actors.FinraRegShoUpdateActor.RegSHO
import com.shocktrade.processors.actors.MissingCik
import com.shocktrade.services.CikCompanySearchService.CikInfo
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.bson.{BSONDocument => BS}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Securities Update DAO
  * @author lawrence.daniels@gmail.com
  */
trait SecuritiesUpdateDAO {

  def findMissingCiks(implicit ec: ExecutionContext): Future[Seq[MissingCik]]

  def getSymbolsForCsvUpdate(implicit ec: ExecutionContext): Cursor[BS]

  def getSymbolsForKeyStatisticsUpdate(implicit ec: ExecutionContext): Cursor[BS]

  def updateQuote(symbol: String, doc: BS)(implicit ec: ExecutionContext): Future[Int]

  def updateCik(symbol: String, name: String, cik: CikInfo)(implicit ec: ExecutionContext): Future[Int]

  def updateRegSHO(reg: RegSHO)(implicit ec: ExecutionContext): Future[Int]

}

/**
  * Securities Update DAO Companion Object
  * @author lawrence.daniels@gmail.com
  */
object SecuritiesUpdateDAO {

  def apply(reactiveMongoApi: ReactiveMongoApi) = {
    new SecuritiesUpdateDAOMongoDB(reactiveMongoApi)
  }

}