package com.shocktrade.webapp.routes.contest

import com.shocktrade.server.dao.MySQLDAO
import io.scalajs.npm.mysql.MySQLConnectionOptions

/**
 * Position DAO (MySQL implementation)
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PositionDAOMySQL(options: MySQLConnectionOptions) extends MySQLDAO(options) with PositionDAO {

}
