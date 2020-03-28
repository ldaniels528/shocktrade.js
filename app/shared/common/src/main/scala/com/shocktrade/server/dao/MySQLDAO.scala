package com.shocktrade.server.dao

import io.scalajs.npm.mysql.{MySQL, MySQLConnection, MySQLConnectionOptions}

/**
 * MySQL Data Access Object
 * @param options the given [[MySQLConnectionOptions]]
 */
class MySQLDAO(options: MySQLConnectionOptions) {
  protected lazy val conn: MySQLConnection = MySQL.createConnection(options)

  def shutdown(): Unit = conn.destroy()

}
