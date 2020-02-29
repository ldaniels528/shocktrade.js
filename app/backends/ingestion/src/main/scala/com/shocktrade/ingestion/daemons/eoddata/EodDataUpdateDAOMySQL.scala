package com.shocktrade.ingestion.daemons.eoddata

import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * EOD Data Update DAO (MySQL version)
 * @param options the given [[MySQLConnectionOptions]]
 */
class EodDataUpdateDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with EodDataUpdateDAO {

  override def update(quote: EodDataRecord)(implicit ec: ExecutionContext): Future[Int] = {
    import quote._
    conn.executeFuture(
      """|REPLACE INTO stocks_eoddata (symbol, exchange, name, high, low, `close`, volume, `change`, changePct)
         |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
         |""".stripMargin,
      params = js.Array(symbol, exchange, name, high, low, quote.close, volume, change, changePct)
    ) map (_.affectedRows)
  }

}
