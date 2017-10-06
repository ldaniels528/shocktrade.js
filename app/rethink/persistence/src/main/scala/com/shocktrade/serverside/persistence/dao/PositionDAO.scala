package com.shocktrade.serverside.persistence.dao

import com.shocktrade.serverside.persistence.dao.mysql.PositionDAOMySQL
import com.shocktrade.serverside.persistence.eventsource.{PositionCreationEvent, PositionUpdateEvent}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Position DAO
  * @author lawrence.daniels@gmail.com
  */
trait PositionDAO {

  def createPosition(event: PositionCreationEvent): Future[Boolean]

  def updatePosition(event: PositionUpdateEvent): Future[Boolean]

}

/**
  * Position DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object PositionDAO {

  def apply()(implicit ec: ExecutionContext): PositionDAO = new PositionDAOMySQL()

}