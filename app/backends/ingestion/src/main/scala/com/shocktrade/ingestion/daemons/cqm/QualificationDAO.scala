package com.shocktrade.ingestion.daemons.cqm

import com.shocktrade.server.dao.DataAccessObjectHelper
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}

/**
 * Qualification DAO
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait QualificationDAO {

  def doQualification()(implicit ec: ExecutionContext): Future[Int]

}

/**
 * Qualification DAO Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object QualificationDAO {

  /**
   * Creates a new Qualification DAO instance
   * @param options the given [[MySQLConnectionOptions]]
   * @return a new [[QualificationDAO Qualification DAO]]
   */
  def apply(options: MySQLConnectionOptions = DataAccessObjectHelper.getConnectionOptions): QualificationDAO = new QualificationDAOMySQL(options)

}
