package com.shocktrade.common.dao.contest

import com.shocktrade.common.forms.ContestSearchForm
import com.shocktrade.common.models.contest.{ChatMessage, Participant}
import org.scalajs.nodejs.mongodb._
import org.scalajs.nodejs.util.ScalaJsHelper._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Contest DAO
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait ContestDAO extends Collection

/**
  * Contest DAO Companion
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ContestDAO {

  /**
    * Contest DAO Extensions
    * @param dao the given [[ContestDAO Contest DAO]]
    */
  implicit class ContestDAOExtensions(val dao: ContestDAO) {

    @inline
    def create(contest: ContestData) = dao.insert(contest)

    @inline
    def addChatMessage(contestID: String, message: ChatMessage)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.findOneAndUpdate(
        filter = "_id" $eq contestID.$oid,
        update = "messages" $addToSet message,
        options = new FindAndUpdateOptions(returnOriginal = false, upsert = false))
    }

    @inline
    def findChatMessages(contestID: String)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.findOneFuture[ContestData](
        selector = "_id" $eq contestID.$oid,
        fields = js.Array("messages")) map (_ map (_.messages getOrElse emptyArray))
    }

    @inline
    def findOneByID(contestID: String)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.findOneFuture[ContestData]("_id" $eq contestID.$oid)
    }

    @inline
    def findByPlayer(playerID: String)(implicit ec: ExecutionContext) = {
      dao.find("participants._id" $eq playerID).toArrayFuture[ContestData]
    }

    @inline
    def findUnoccupied(playerID: String)(implicit ec: ExecutionContext) = {
      dao.find("participants" $not $elemMatch("_id" $eq playerID)).toArrayFuture[ContestData]
    }

    @inline
    def join(contestID: String, participant: Participant)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      dao.findOneAndUpdate(filter = "_id" $eq contestID.$oid, update = "participants" $addToSet participant)
    }

    @inline
    def search(form: ContestSearchForm) = {
      val query = doc(Seq(
        form.activeOnly.toOption.flatMap(checked => if (checked) Some("status" $eq "ACTIVE") else None),
        form.friendsOnly.toOption.flatMap(checked => if (checked) Some("friendsOnly" $eq true) else None),
        form.perksAllowed.toOption.flatMap(checked => if (checked) Some("perksAllowed" $eq true) else None),
        form.invitationOnly.toOption.flatMap(checked => if (checked) Some("invitationOnly" $eq true) else None),
        (for (allowed <- form.levelCapAllowed.toOption; level <- form.levelCap.toOption) yield (allowed, level)) flatMap {
          case (allowed, levelCap) => if (allowed) Some($or("levelCap" $exists false, "levelCap" $lte levelCap.toInt)) else None
        },
        form.perksAllowed.toOption.flatMap(checked => if (checked) Some("perksAllowed" $eq true) else None),
        form.robotsAllowed.toOption.flatMap(checked => if (checked) Some("robotsAllowed" $eq true) else None)
      ).flatten: _*)

      dao.find(query).toArrayFuture[ContestData]
    }

  }

  /**
    * Contest DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class ContestDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getContestDAO(implicit ec: ExecutionContext) = {
      db.collectionFuture("Contests").mapTo[ContestDAO]
    }
  }

}