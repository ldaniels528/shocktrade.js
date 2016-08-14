package com.shocktrade.javascript.data

import com.shocktrade.javascript.forms.ContestSearchForm
import org.scalajs.nodejs.mongodb._

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Contest DAO
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ContestDAO extends Collection

/**
  * Contest DAO Companion
  * @author lawrence.daniels@gmail.com
  */
object ContestDAO {

  /**
    * Contest DAO Extensions
    * @param contestDAO the given [[ContestDAO Contest DAO]]
    */
  implicit class ContestDAOExtensions(val contestDAO: ContestDAO) {

    @inline
    def create(contest: ContestData) = contestDAO.insert(contest)

    @inline
    def search(form: ContestSearchForm) = {
      // TODO add search criteria
      contestDAO.find().toArrayFuture[ContestData]
    }

    @inline
    def totalInvestment(playerID: String)(implicit ec: ExecutionContext, mongo: MongoDB) = {
      for {
        contests <- contestDAO.find(selector = "participants._id" $eq playerID.$oid, projection = js.Array("participants").toProjection).toArrayFuture[ContestData]
        results = for {
          contest <- contests
          participants <- contest.participants.toOption.map(_.toSeq).toList
          participant <- participants // TODO filter for only the player we're looking for
          positions <- participant.positions.toOption.map(_.toSeq).toList
          position <- positions
          totalCost <- position.totalCost.toOption.toList
        } yield totalCost
      } yield results.sum
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