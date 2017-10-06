package com.shocktrade.serverside.persistence.dao.mysql

import com.shocktrade.serverside.persistence.dao.PositionDAO
import com.shocktrade.serverside.persistence.eventsource.{PositionCreationEvent, PositionUpdateEvent}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * MySQL implementation of the Position DAO
  * @author lawrence.daniels@gmail.com
  */
class PositionDAOMySQL()(implicit ec: ExecutionContext) extends PositionDAO {
  private val datePattern = "YYYY-MM-DD HH:mm:ss"

  override def createPosition(event: PositionCreationEvent): Future[Boolean] = {
    Future.failed(js.JavaScriptException("Create position is not yet implemented"))
  }

  override def updatePosition(event: PositionUpdateEvent): Future[Boolean] = {
    Future.failed(js.JavaScriptException("Update position is not yet implemented"))
  }

}
