package com.shocktrade.server.dao

import io.scalajs.nodejs.process
import io.scalajs.npm.mysql.MySQLConnectionOptions

/**
  * Data Access Object Helper
  * @author lawrence.daniels@gmail.com
  */
object DataAccessObjectHelper {

  /**
    * Retrieves the connection options for the database connection from an environment variable called "db_connection"
    * @return the [[MySQLConnectionOptions]]
    */
  def getConnectionOptions: MySQLConnectionOptions = {
    val name = "db_connection"
    process.env.get(name) flatMap { connectionString =>
      connectionString.split("[|]").toList match {
        case host :: database :: user :: password :: Nil =>
          Option(new MySQLConnectionOptions(
            host = host,
            database = database,
            user = user,
            password = password
          ))
        case _ => None
      }
    } getOrElse(throw new IllegalStateException(s"Environment variable '$name' is required. Invoke '. ~/shocktrade-env.sh' and try again."))
  }

}
