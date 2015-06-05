package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.models.contest.ContestStatuses.ContestStatus
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
case class Contest(id: BSONObjectID = BSONObjectID.generate,
                   name: String,
                   creator: PlayerRef,
                   creationTime: Date,
                   startTime: Option[Date] = None,
                   expirationTime: Option[Date] = None,
                   startingBalance: BigDecimal,
                   messages: List[Message] = Nil,
                   participants: List[Participant] = Nil,
                   status: ContestStatus = ContestStatuses.ACTIVE,
                   levelCap: Option[Int] = None,
                   friendsOnly: Boolean = false,
                   invitationOnly: Boolean = false,
                   perksAllowed: Boolean = false,
                   robotsAllowed: Boolean = false,
                   asOfDate: Option[Date] = None)

/**
 * Contest Singleton
 * @author lawrence.daniels@gmail.com
 */
object Contest {
  val MaxPlayers = 24

  implicit val contestReads: Reads[Contest] = (
    (__ \ "_id").read[BSONObjectID] and
      (__ \ "name").read[String] and
      (__ \ "creator").read[PlayerRef] and
      (__ \ "creationTime").read[Date] and
      (__ \ "startTime").readNullable[Date] and
      (__ \ "expirationTime").readNullable[Date] and
      (__ \ "startingBalance").read[BigDecimal] and
      (__ \ "messages").readNullable[List[Message]].map(_.getOrElse(Nil)) and
      (__ \ "participants").readNullable[List[Participant]].map(_.getOrElse(Nil)) and
      (__ \ "status").read[ContestStatus] and
      (__ \ "levelCap").readNullable[Int] and
      (__ \ "friendsOnly").read[Boolean] and
      (__ \ "invitationOnly").read[Boolean] and
      (__ \ "perksAllowed").read[Boolean] and
      (__ \ "robotsAllowed").read[Boolean] and
      (__ \ "asOfDate").readNullable[Date])(Contest.apply _)

  implicit val contestWrites: Writes[Contest] = (
    (__ \ "_id").write[BSONObjectID] and
      (__ \ "name").write[String] and
      (__ \ "creator").write[PlayerRef] and
      (__ \ "creationTime").write[Date] and
      (__ \ "startTime").writeNullable[Date] and
      (__ \ "expirationTime").writeNullable[Date] and
      (__ \ "startingBalance").write[BigDecimal] and
      (__ \ "messages").write[List[Message]] and
      (__ \ "participants").write[List[Participant]] and
      (__ \ "status").write[ContestStatus] and
      (__ \ "levelCap").writeNullable[Int] and
      (__ \ "friendsOnly").write[Boolean] and
      (__ \ "invitationOnly").write[Boolean] and
      (__ \ "perksAllowed").write[Boolean] and
      (__ \ "robotsAllowed").write[Boolean] and
      (__ \ "asOfDate").writeNullable[Date])(unlift(Contest.unapply))

  implicit object ContestReader extends BSONDocumentReader[Contest] {
    def read(doc: BSONDocument) = Contest(
      doc.getAs[BSONObjectID]("_id").get,
      doc.getAs[String]("name").get,
      doc.getAs[PlayerRef]("creator").get,
      doc.getAs[Date]("creationTime").getOrElse(new Date()),
      doc.getAs[Date]("startTime"),
      doc.getAs[Date]("expirationTime"),
      doc.getAs[BigDecimal]("startingBalance").get,
      doc.getAs[List[Message]]("messages").getOrElse(Nil),
      doc.getAs[List[Participant]]("participants").getOrElse(Nil),
      doc.getAs[ContestStatus]("status").get,
      doc.getAs[Int]("levelCap"),
      doc.getAs[Boolean]("friendsOnly").contains(true),
      doc.getAs[Boolean]("invitationOnly").contains(true),
      doc.getAs[Boolean]("perksAllowed").contains(true),
      doc.getAs[Boolean]("robotsAllowed").contains(true),
      doc.getAs[Date]("asOfDate")
    )
  }

  implicit object ContestWriter extends BSONDocumentWriter[Contest] {
    def write(contest: Contest) = BSONDocument(
      "_id" -> contest.id,
      "name" -> contest.name,
      "creator" -> contest.creator,
      "creationTime" -> contest.creationTime,
      "startTime" -> contest.startTime,
      "expirationTime" -> contest.expirationTime,
      "startingBalance" -> contest.startingBalance,
      "messages" -> contest.messages,
      "participants" -> contest.participants,
      "status" -> contest.status,
      "levelCap" -> contest.levelCap,
      "friendsOnly" -> contest.friendsOnly,
      "invitationOnly" -> contest.invitationOnly,
      "perksAllowed" -> contest.perksAllowed,
      "robotsAllowed" -> contest.robotsAllowed,
      "asOfDate" -> contest.asOfDate,
      "playerCount" -> contest.participants.length
    )
  }

}