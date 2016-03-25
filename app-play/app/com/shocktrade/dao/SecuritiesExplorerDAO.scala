package com.shocktrade.dao

import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDocument => BS}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Securities Explorer DAO
  * @author lawrence.daniels@gmail.com
  */
trait SecuritiesExplorerDAO {

  def exploreSectors(userID: String)(implicit ec: ExecutionContext): Future[Seq[BS]]

  def exploreIndustries(userID: String, sector: String)(implicit ec: ExecutionContext): Future[Seq[BS]]

  def exploreSubIndustries(userID: String, sector: String, industry: String)(implicit ec: ExecutionContext): Future[Seq[BS]]

  def exploreQuotesBySubIndustry(userID: String, sector: String, industry: String, subIndustry: String)(implicit ec: ExecutionContext): Future[Seq[BS]]

  def exploreNAICSSectors(implicit ec: ExecutionContext): Future[Seq[BS]]

  def exploreSICSectors(implicit ec: ExecutionContext): Future[Seq[BS]]

}

/**
  * Securities Explorer DAO Companion Object
  * @author lawrence.daniels@gmail.com
  */
object SecuritiesExplorerDAO {

  def apply(reactiveMongoApi: ReactiveMongoApi) = {
    new SecuritiesExplorerDAOMongoDB(reactiveMongoApi)
  }

}