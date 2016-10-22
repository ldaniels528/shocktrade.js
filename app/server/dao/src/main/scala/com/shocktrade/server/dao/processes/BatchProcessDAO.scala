package com.shocktrade.server.dao.processes

import org.scalajs.nodejs.mongodb._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Batch Process DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait BatchProcessDAO extends Collection

/**
  * Batch Process DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object BatchProcessDAO {

  /**
    * Batch Process DAO Enrichment
    * @param dao the given [[BatchProcessDAO process DAO]]
    */
  implicit class BatchProcessDAOEnrichment(val dao: BatchProcessDAO) extends AnyVal {

    @inline
    def findProcess(name: String)(implicit ec: ExecutionContext) = {
      dao.findOneFuture[BatchProcessData]("name" $eq name)
    }

  }

  /**
    * Batch Process DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class BatchProcessDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getBatchProcessDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("BatchProcesses").mapTo[BatchProcessDAO]
    }
  }

}
