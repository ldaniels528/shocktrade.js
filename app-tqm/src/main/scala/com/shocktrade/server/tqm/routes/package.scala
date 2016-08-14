package com.shocktrade.server.tqm

import org.scalajs.nodejs.mongodb.{Collection, MongoDB, _}

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Routes package object
  * @author lawrence.daniels@gmail.com
  */
package object routes {

  type NextFunction = js.Function0[Unit]

  /**
    * Data Access Object Extensions
    * @author lawrence.daniels@gmail.com
    */
  implicit class DAOExtensions(val coll: Collection) extends AnyVal {

    @inline
    def findById[T <: js.Any](id: String)(implicit mongo: MongoDB, ec: ExecutionContext) = {
      coll.findOneFuture[T]("_id" $eq id.$oid)
    }

    @inline
    def findById[T <: js.Any](id: String, fields: js.Array[String])(implicit mongo: MongoDB, ec: ExecutionContext) = {
      coll.findOneFuture[T]("_id" $eq id.$oid, fields)
    }

  }

}
