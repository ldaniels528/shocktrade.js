package com.shocktrade.models.contest

import java.util.Date

import com.shocktrade.models.contest.Contest.{MaxPlayers, Modifiers}
import com.shocktrade.util.BSONHelper
import com.shocktrade.util.BSONHelper._
import play.api.libs.json.JsArray
import play.api.libs.json.Json.{obj => JS}
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
                   modifiers: Modifiers,
                   participants: List[Participant] = Nil,
                   status: ContestStatus = ContestStatus.ACTIVE,
                   id: Option[BSONObjectID] = None) {

  def toJson = JS(
    "_id" -> id.toBSID,
    "name" -> name,
    "creator" -> participants.headOption.map(_.name),
    "creationTime" -> creationTime,
    "expirationTime" -> expirationTime,
    "maxParticipants" -> maxParticipants,
    "startingBalance" -> startingBalance,
    "messages" -> JsArray(messages.map(_.toJson)),
    //"modifiers" -> modifiers.toJson,
    "participants" -> JsArray(participants.map(_.toJson)),
    "playerCount" -> participants.size,
    "status" -> status.name
  ) ++ modifiers.toJson

}

/**
 * Contest Singleton
 * @author lawrence.daniels@gmail.com
 */
object Contest {
  val MaxPlayers = 14

  /**
   * Represents contest options
   * @author lawrence.daniels@gmail.com
   */
  case class Modifiers(acquaintances: Boolean = false,
                       friendsOnly: Boolean = false,
                       levelCap: Option[Int] = None,
                       invitationOnly: Boolean = false,
                       perksAllowed: Boolean = false,
                       ranked: Boolean = false) {

    def toJson = JS(
      "acquaintances" -> acquaintances,
      "friendsOnly" -> friendsOnly,
      "invitationOnly" -> invitationOnly,
      "levelCap" -> levelCap,
      "perksAllowed" -> perksAllowed,
      "ranked" -> ranked
    )

  }

}