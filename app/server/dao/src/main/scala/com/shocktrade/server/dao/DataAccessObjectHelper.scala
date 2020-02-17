package com.shocktrade.server.dao

import io.scalajs.nodejs.process
import io.scalajs.npm.mysql.ConnectionOptions

/**
  * Data Access Object Helper
  * @author lawrence.daniels@gmail.com
  */
object DataAccessObjectHelper {

  def getConnectionOptions: ConnectionOptions = {
    val name = "db_connection"
    process.env.get(name) flatMap { connectionString =>
      connectionString.split("[|]").toList match {
        case host :: database :: user :: password :: Nil =>
          Option(new ConnectionOptions(
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
