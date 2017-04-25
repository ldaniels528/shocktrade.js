package com.shocktrade.server.dao.contest

import com.shocktrade.common.forms.ContestSearchForm
import com.shocktrade.common.models.contest.{ChatMessage, Participant}
import io.scalajs.npm.mongodb._
import io.scalajs.util.ScalaJsHelper._

import scala.concurrent.{ExecutionContext, Future}
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
    def addChatMessage(contestID: String, message: ChatMessage)(implicit ec: ExecutionContext): js.Promise[FindAndModifyWriteOpResult] = {
      dao.findOneAndUpdate(
        filter = "_id" $eq contestID.$oid,
        update = "messages" $addToSet message,
        options = new FindAndUpdateOptions(returnOriginal = false, upsert = false))
    }

    @inline
    def create(contest: ContestData): js.Promise[InsertWriteOpResult] = dao.insertOne(contest)

    @inline
    def findActiveContests()(implicit ec: ExecutionContext): js.Promise[js.Array[ContestData]] = {
      dao.find[ContestData]("status" $eq "ACTIVE").toArray()
    }

    @inline
    def findChatMessages(contestID: String)(implicit ec: ExecutionContext): Future[Option[js.Array[ChatMessage]]] = {
      dao.findOneFuture[ContestData](
        selector = "_id" $eq contestID.$oid,
        fields = js.Array("messages")) map (_ map (_.messages getOrElse emptyArray))
    }

    @inline
    def findOneByID(contestID: String)(implicit ec: ExecutionContext): Future[Option[ContestData]] = {
      dao.findOneFuture[ContestData]("_id" $eq contestID.$oid)
    }

    @inline
    def findByPlayer(playerID: String)(implicit ec: ExecutionContext): js.Promise[js.Array[ContestData]] = {
      dao.find[ContestData]("participants._id" $eq playerID).toArray()
    }

    @inline
    def findUnoccupied(playerID: String)(implicit ec: ExecutionContext): js.Promise[js.Array[ContestData]] = {
      dao.find[ContestData]("participants" $not $elemMatch("_id" $eq playerID)).toArray()
    }

    @inline
    def join(contestID: String, participant: Participant)(implicit ec: ExecutionContext): js.Promise[FindAndModifyWriteOpResult] = {
      dao.findOneAndUpdate(filter = "_id" $eq contestID.$oid, update = "participants" $addToSet participant)
    }

    @inline
    def search(form: ContestSearchForm): js.Promise[js.Array[ContestData]] = {
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

      dao.find[ContestData](query).toArray()
    }

    @inline
    def updateContest(contest: ContestData): js.Promise[UpdateWriteOpResultObject] = {
      dao.updateOne(filter = "_id" $eq contest._id, update = contest, new UpdateOptions(upsert = false))
    }

    @inline
    def updateContests(contests: Seq[ContestData]): js.Promise[BulkWriteOpResultObject] = {
      dao.bulkWrite(
        js.Array(contests map (contest =>
          updateOne(filter = "_id" $eq contest._id, update = contest, upsert = false)
          ): _*)
      )
    }

  }

  /**
    * Contest DAO Constructors
    * @param db the given [[Db database]]
    */
  implicit class ContestDAOConstructors(val db: Db) extends AnyVal {

    @inline
    def getContestDAO: ContestDAO = {
      db.collection("Contests").asInstanceOf[ContestDAO]
    }
  }

}