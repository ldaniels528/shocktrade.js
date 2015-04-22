package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.models.contest.Contest.MaxPlayers
import com.shocktrade.models.contest.ContestStatus.ContestStatus
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsPath, Reads, Writes}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID

/**
 * Represents a contest
 * @author lawrence.daniels@gmail.com
 */
case class Contest(name: String,
                   creationTime: Date,
                   expirationTime: Option[Date] = None,
                   startingBalance: BigDecimal,
                   maxParticipants: Int = MaxPlayers,
                   messages: List[Message] = Nil,
                   participants: List[Participant] = Nil,
                   status: ContestStatus = ContestStatus.ACTIVE,
                   acquaintances: Boolean = false,
                   friendsOnly: Boolean = false,
                   levelCap: Option[Int] = None,
                   invitationOnly: Boolean = false,
                   perksAllowed: Boolean = false,
                   ranked: Boolean = false,
                   id: BSONObjectID = BSONObjectID.generate)

/**
 * Contest Singleton
 * @author lawrence.daniels@gmail.com
 */
object Contest {
  val MaxPlayers = 14

  implicit val contestReads: Reads[Contest] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "creationTime").read[Date] and
      (JsPath \ "expirationTime").read[Option[Date]] and
      (JsPath \ "startingBalance").read[BigDecimal] and
      (JsPath \ "maxParticipants").read[Int] and
      (JsPath \ "messages").read[List[Message]] and
      (JsPath \ "participants").read[List[Participant]] and
      (JsPath \ "status").read[ContestStatus] and
      (JsPath \ "acquaintances").read[Boolean] and
      (JsPath \ "friendsOnly").read[Boolean] and
      (JsPath \ "levelCap").read[Option[Int]] and
      (JsPath \ "invitationOnly").read[Boolean] and
      (JsPath \ "perksAllowed").read[Boolean] and
      (JsPath \ "ranked").read[Boolean] and
      (JsPath \ "_id").read[BSONObjectID])(Contest.apply _)

  implicit val contestWrites: Writes[Contest] = (
    (JsPath \ "name").write[String] and
      (JsPath \ "creationTime").write[Date] and
      (JsPath \ "expirationTime").write[Option[Date]] and
      (JsPath \ "startingBalance").write[BigDecimal] and
      (JsPath \ "maxParticipants").write[Int] and
      (JsPath \ "messages").write[List[Message]] and
      (JsPath \ "participants").write[List[Participant]] and
      (JsPath \ "status").write[ContestStatus] and
      (JsPath \ "acquaintances").write[Boolean] and
      (JsPath \ "friendsOnly").write[Boolean] and
      (JsPath \ "levelCap").write[Option[Int]] and
      (JsPath \ "invitationOnly").write[Boolean] and
      (JsPath \ "perksAllowed").write[Boolean] and
      (JsPath \ "ranked").write[Boolean] and
      (JsPath \ "_id").write[BSONObjectID])(unlift(Contest.unapply))

}