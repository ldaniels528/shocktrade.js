package com.shocktrade.server

import io.scalajs.npm.mongodb._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * dao package object
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
package object dao {

  /**
    * Data Access Object Extensions
    * @author Lawrence Daniels <lawrence.daniels@gmail.com>
    */
  implicit class DAOExtensions(val coll: Collection) extends AnyVal {

    @inline
    def findById[T <: js.Any](id: String)(implicit ec: ExecutionContext): Future[Option[T]] = {
      coll.findOneAsync[T]("_id" $eq id.$oid)
    }

    @inline
    def findById[T <: js.Any](id: String, fields: js.Array[String])(implicit ec: ExecutionContext): Future[Option[T]] = {
      coll.findOneAsync[T]("_id" $eq id.$oid, fields)
    }

    @inline
    def follow(entityID: String, userID: String): Future[FindAndModifyWriteOpResult] = {
      link(entityID, userID, entitySetName = "followers", entityQtyName = "totalFollowers")
    }

    @inline
    def unfollow(entityID: String, userID: String): Future[FindAndModifyWriteOpResult] = {
      unlink(entityID, userID, entitySetName = "followers", entityQtyName = "totalFollowers")
    }

    @inline
    def like(entityID: String, userID: String): Future[FindAndModifyWriteOpResult] = {
      link(entityID, userID, entitySetName = "likedBy", entityQtyName = "likes")
    }

    @inline
    def unlike(entityID: String, userID: String): Future[FindAndModifyWriteOpResult] = {
      unlink(entityID, userID, entitySetName = "likedBy", entityQtyName = "likes")
    }

    private def link(entityID: String, userID: String, entitySetName: String, entityQtyName: String) = {
      coll.findOneAndUpdate(
        filter = doc("_id" $eq new ObjectID(entityID), $or(entitySetName $nin js.Array(userID), entitySetName $exists false)),
        update = doc(entitySetName $addToSet userID, entityQtyName $inc 1),
        options = new FindAndUpdateOptions(upsert = false, returnOriginal = false)
      ).toFuture
    }

    private def unlink(entityID: String, userID: String, entitySetName: String, entityQtyName: String) = {
      coll.findOneAndUpdate(
        filter = doc("_id" $eq new ObjectID(entityID), entitySetName $in js.Array(userID)),
        update = doc(entitySetName $pull userID, entityQtyName $inc -1),
        options = new FindAndUpdateOptions(upsert = false, returnOriginal = false)
      ).toFuture
    }

  }

  /**
    * Future Extensions
    * @param futureA the given host [[Future future]]
    */
  implicit class FutureExtensions[A](val futureA: Future[A]) extends AnyVal {

    @inline
    def ++[B](futureB: Future[B])(implicit ec: ExecutionContext): Future[(A, B)] = for {
      a <- futureA
      b <- futureB
    } yield (a, b)

  }

}
