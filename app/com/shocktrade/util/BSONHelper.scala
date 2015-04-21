package com.shocktrade.util

import reactivemongo.bson.BSONObjectID

/**
 * BSON Helper
 * @author lawrence.daniels@gmail.com
 */
object BSONHelper {

  implicit class BSONObjectIDExtensionsA(val id: Option[String]) extends AnyVal {

    def toBSID = id.map(s => BSONObjectID(s)).getOrElse(BSONObjectID.generate)

  }

  implicit class BSONObjectIDExtensionsB(val id: Option[BSONObjectID]) extends AnyVal {

    def toBSID = id.getOrElse(BSONObjectID.generate)

  }

}
