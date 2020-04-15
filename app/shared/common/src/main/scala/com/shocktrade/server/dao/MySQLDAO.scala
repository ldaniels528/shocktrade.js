package com.shocktrade.server.dao

import io.scalajs.npm.mysql.{MySQL, MySQLConnection, MySQLConnectionOptions}

import scala.scalajs.js

/**
 * MySQL Data Access Object
 * @param options the given [[MySQLConnectionOptions]]
 */
class MySQLDAO(options: MySQLConnectionOptions) {
  protected lazy val conn: MySQLConnection = MySQL.createConnection(options)

  def shutdown(): Unit = conn.destroy()

  protected def checkInsertCount: Int => Int = {
    case count if count < 1 => throw js.JavaScriptException(s"Record could not inserted (count = $count)")
    case count => count
  }

  protected def checkUpdateCount: Int => Int = {
    case count if count < 1 => throw js.JavaScriptException(s"Record could not updated (count = $count)")
    case count => count
  }

}
