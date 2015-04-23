package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.models.contest.Contest.MaxPlayers
import com.shocktrade.models.contest.ContestStatus.ContestStatus
import com.shocktrade.util.BSONHelper._
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{Reads, Writes, __}
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, _}

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
    (__ \ "name").read[String] and
      (__ \ "creationTime").read[Date] and
      (__ \ "expirationTime").readNullable[Date] and
      (__ \ "startingBalance").read[BigDecimal] and
      (__ \ "maxParticipants").read[Int] and
      (__ \ "messages").read[List[Message]] and
      (__ \ "participants").read[List[Participant]] and
      (__ \ "status").read[ContestStatus] and
      (__ \ "acquaintances").read[Boolean] and
      (__ \ "friendsOnly").read[Boolean] and
      (__ \ "levelCap").readNullable[Int] and
      (__ \ "invitationOnly").read[Boolean] and
      (__ \ "perksAllowed").read[Boolean] and
      (__ \ "ranked").read[Boolean] and
      (__ \ "_id").read[BSONObjectID])(Contest.apply _)

  implicit val contestWrites: Writes[Contest] = (
    (__ \ "name").write[String] and
      (__ \ "creationTime").write[Date] and
      (__ \ "expirationTime").writeNullable[Date] and
      (__ \ "startingBalance").write[BigDecimal] and
      (__ \ "maxParticipants").write[Int] and
      (__ \ "messages").write[List[Message]] and
      (__ \ "participants").write[List[Participant]] and
      (__ \ "status").write[ContestStatus] and
      (__ \ "acquaintances").write[Boolean] and
      (__ \ "friendsOnly").write[Boolean] and
      (__ \ "levelCap").writeNullable[Int] and
      (__ \ "invitationOnly").write[Boolean] and
      (__ \ "perksAllowed").write[Boolean] and
      (__ \ "ranked").write[Boolean] and
      (__ \ "_id").write[BSONObjectID])(unlift(Contest.unapply))

  implicit object ContestReader extends BSONDocumentReader[Contest] {
    def read(doc: BSONDocument) = Contest(
      doc.getAs[String]("name").get,
      doc.getAs[Date]("creationTime").get,
      doc.getAs[Date]("expirationTime"),
      doc.getAs[BigDecimal]("startingBalance").get,
      doc.getAs[Int]("maxParticipants").get,
      doc.getAs[List[Message]]("messages").get,
      doc.getAs[List[Participant]]("participants").get,
      doc.getAs[ContestStatus]("status").get,
      doc.getAs[Boolean]("acquaintances").get,
      doc.getAs[Boolean]("friendsOnly").get,
      doc.getAs[Int]("levelCap"),
      doc.getAs[Boolean]("invitationOnly").get,
      doc.getAs[Boolean]("perksAllowed").get,
      doc.getAs[Boolean]("ranked").get,
      doc.getAs[BSONObjectID]("_id").get
    )
  }

  implicit object ContestWriter extends BSONDocumentWriter[Contest] {
    def write(contest: Contest) = BSONDocument(
      "_id" -> contest.id,
      "name" -> contest.name,
      "creationTime" -> contest.creationTime,
      "expirationTime" -> contest.expirationTime,
      "startingBalance" -> contest.startingBalance,
      "maxParticipants" -> contest.maxParticipants,
      "messages" -> contest.messages,
      "participants" -> contest.participants,
      "status" -> contest.status,
      "acquaintances" -> contest.acquaintances,
      "friendsOnly" -> contest.friendsOnly,
      "levelCap" -> contest.levelCap,
      "invitationOnly" -> contest.invitationOnly,
      "perksAllowed" -> contest.perksAllowed,
      "ranked" -> contest.ranked
    )
  }

}