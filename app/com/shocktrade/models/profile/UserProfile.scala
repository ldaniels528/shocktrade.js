package com.shocktrade.models.profile

import java.util.Date

import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, _}

import scala.util.{Failure, Success, Try}

/**
 * User Profile
 * @author lawrence.daniels@gmail.com
 */
case class UserProfile(id: BSONObjectID = BSONObjectID.generate,
                       name: String,
                       facebookID: String,
                       email: Option[String] = None,
                       netWorth: BigDecimal = 250000,
                       level: Int = 1,
                       admin: Option[Boolean] = None,
                       totalXP: Int = 0,
                       awards: List[String] = Nil,
                       favorites: List[String] = Nil,
                       friends: List[String] = Nil,
                       recentSymbols: List[String] = Nil,
                       country: Option[String] = None,
                       lastLoginTime: Option[Date] = None)

/**
 * User Profile Singleton
 * @author lawrence.daniels@gmail.com
 */
object UserProfile {

  implicit val userProfileReads: Reads[UserProfile] = (
    (__ \ "_id").read[BSONObjectID] and
      (__ \ "name").read[String] and
      (__ \ "facebookID").read[String] and
      (__ \ "email").readNullable[String] and
      (__ \ "netWorth").read[BigDecimal] and
      (__ \ "level").read[Int] and
      (__ \ "admin").readNullable[Boolean] and
      (__ \ "totalXP").read[Int] and
      (__ \ "awards").read[List[String]] and
      (__ \ "favorites").read[List[String]] and
      (__ \ "friends").read[List[String]] and
      (__ \ "recentSymbols").read[List[String]] and
      (__ \ "country").readNullable[String] and
      (__ \ "lastLoginTime").readNullable[Date])(UserProfile.apply _)

  implicit val userProfileWrites: Writes[UserProfile] = (
    (__ \ "_id").write[BSONObjectID] and
      (__ \ "name").write[String] and
      (__ \ "facebookID").write[String] and
      (__ \ "email").writeNullable[String] and
      (__ \ "netWorth").write[BigDecimal] and
      (__ \ "level").write[Int] and
      (__ \ "admin").writeNullable[Boolean] and
      (__ \ "totalXP").write[Int] and
      (__ \ "awards").write[List[String]] and
      (__ \ "favorites").write[List[String]] and
      (__ \ "friends").write[List[String]] and
      (__ \ "recentSymbols").write[List[String]] and
      (__ \ "country").writeNullable[String] and
      (__ \ "lastLoginTime").writeNullable[Date])(unlift(UserProfile.unapply))

  implicit object FilterReader extends BSONDocumentReader[UserProfile] {
    def read(doc: BSONDocument) = Try(UserProfile(
      doc.getAs[BSONObjectID]("_id").get,
      doc.getAs[String]("name").get,
      doc.getAs[String]("facebookID").get,
      doc.getAs[String]("email"),
      doc.getAs[BigDecimal]("netWorth").get,
      doc.getAs[Int]("level").getOrElse(1),
      doc.getAs[Boolean]("admin"),
      doc.getAs[Int]("totalXP").getOrElse(0),
      doc.getAs[List[String]]("awards").getOrElse(Nil),
      doc.getAs[List[String]]("favorites").getOrElse(Nil),
      doc.getAs[List[String]]("friends").getOrElse(Nil),
      doc.getAs[List[String]]("recentSymbols").getOrElse(Nil),
      doc.getAs[String]("country"),
      doc.getAs[Date]("lastLoginTime")
    )) match {
      case Success(v) => v
      case Failure(e) =>
        e.printStackTrace()
        throw new IllegalStateException(e)
    }
  }

  implicit object FilterWriter extends BSONDocumentWriter[UserProfile] {
    def write(userProfile: UserProfile) = BSONDocument(
      "_id" -> userProfile.id,
      "name" -> userProfile.name,
      "facebookID" -> userProfile.facebookID,
      "email" -> userProfile.email,
      "netWorth" -> userProfile.netWorth,
      "level" -> userProfile.level,
      "admin" -> userProfile.admin,
      "totalXP" -> userProfile.totalXP,
      "awards" -> userProfile.awards,
      "favorites" -> userProfile.favorites,
      "friends" -> userProfile.friends,
      "recentSymbols" -> userProfile.recentSymbols,
      "country" -> userProfile.country,
      "lastLoginTime" -> userProfile.lastLoginTime
    )
  }

}