package com.shocktrade.qualification

import com.shocktrade.qualification.QualificationDAOMySQL.QualificationResult
import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Qualification DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class QualificationDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with QualificationDAO {

  override def doQualification()(implicit ec: ExecutionContext): Future[Int] = {
    conn.queryFuture[QualificationResult]("CALL doQualification()")
      .map { case (rows, _) => rows.headOption.flatMap(_.total_count.toOption).getOrElse(0) }
  }

}

/**
 * QualificationDAOMySQL Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object QualificationDAOMySQL {

  class QualificationResult(val total_count: js.UndefOr[Int]) extends js.Object

}
