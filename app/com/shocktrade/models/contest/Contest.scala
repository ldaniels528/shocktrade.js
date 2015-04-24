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

import scala.util.{Failure, Success, Try}

/**
 * Represents a contest
 * @author lawrence.daniels@gmail.com
 */
case class Contest(name: String,
                   creator: Player,
                   creationTime: Date,
                   startTime: Option[Date] = None,
                   processedTime: Option[Date] = None,
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
      (__ \ "creator").read[Player] and
      (__ \ "creationTime").read[Date] and
      (__ \ "startTime").readNullable[Date] and
      (__ \ "processedTime").readNullable[Date] and
      (__ \ "expirationTime").readNullable[Date] and
      (__ \ "startingBalance").read[BigDecimal] and
      (__ \ "maxParticipants").read[Int] and
      (__ \ "messages").readNullable[List[Message]].map(_.getOrElse(Nil)) and
      (__ \ "participants").readNullable[List[Participant]].map(_.getOrElse(Nil)) and
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
      (__ \ "creator").write[Player] and
      (__ \ "creationTime").write[Date] and
      (__ \ "startTime").writeNullable[Date] and
      (__ \ "processedTime").writeNullable[Date] and
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
    def read(doc: BSONDocument) = Try(Contest(
      doc.getAs[String]("name").get,
      doc.getAs[Player]("creator").get,
      doc.getAs[Date]("creationTime").getOrElse(new Date()),
      doc.getAs[Date]("startTime"),
      doc.getAs[Date]("processedTime"),
      doc.getAs[Date]("expirationTime"),
      doc.getAs[BigDecimal]("startingBalance").get,
      doc.getAs[Int]("maxParticipants").getOrElse(MaxPlayers),
      doc.getAs[List[Message]]("messages").getOrElse(Nil),
      doc.getAs[List[Participant]]("participants").getOrElse(Nil),
      doc.getAs[ContestStatus]("status").get,
      doc.getAs[Boolean]("acquaintances").contains(true),
      doc.getAs[Boolean]("friendsOnly").contains(true),
      doc.getAs[Int]("levelCap"),
      doc.getAs[Boolean]("invitationOnly").contains(true),
      doc.getAs[Boolean]("perksAllowed").contains(true),
      doc.getAs[Boolean]("ranked").contains(true),
      doc.getAs[BSONObjectID]("_id").get
    )) match {
      case Success(v) => v
      case Failure(e) =>
        e.printStackTrace()
        throw new IllegalStateException(e)
    }
  }

  implicit object ContestWriter extends BSONDocumentWriter[Contest] {
    def write(contest: Contest) = BSONDocument(
      "_id" -> contest.id,
      "name" -> contest.name,
      "creator" -> contest.creator,
      "creationTime" -> contest.creationTime,
      "startTime" -> contest.startTime,
      "processedTime" -> contest.processedTime,
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