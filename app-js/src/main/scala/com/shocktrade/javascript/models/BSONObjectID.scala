package com.shocktrade.javascript.models

import com.github.ldaniels528.meansjs.util.ScalaJsHelper._

import scala.language.implicitConversions
import scala.scalajs.js

/**
  * Represents a BSON Object ID (MongoDB)
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait BSONObjectID extends js.Object {
  var $oid: String
}

/**
  * BSONObjectID Companion
  * @author lawrence.daniels@gmail.com
  */
object BSONObjectID {

  /**
    * Creates a new BSON object ID instance (e.g. { _id : { $oid : "5637d9221b591f3f72d7efec" }} )
    * @param value the given value of the object identifier (e.g. "5637d9221b591f3f72d7efec")
    * @return a new BSON object ID instance
    */
  def apply(value: String) = {
    val bsid = New[BSONObjectID]
    bsid.$oid = value
    bsid
  }

  /**
    * Indicates whether the two unique identifiers are equal
    * @param idA the given [[BSONObjectID objectID A]]
    * @param idB the given [[BSONObjectID objectID B]]
    * @return true, if both the inner values ($oid) are equal
    */
  def isEqual(idA: js.UndefOr[BSONObjectID], idB: js.UndefOr[BSONObjectID]): Boolean = {
    (for {
      id0 <- idA
      id1 <- idB
    } yield id0.$oid == id1.$oid).exists(_ == true)
  }

}

