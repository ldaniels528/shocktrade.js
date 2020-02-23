package com.shocktrade.client.contest

import com.shocktrade.client.models.contest.{Contest, ContestSearchOptions}
import com.shocktrade.common.forms.{ContestCreateForm, PlayerInfoForm}
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Contest Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ContestService($http: Http) extends Service {

  ///////////////////////////////////////////////////////////////
  //          Basic C.R.U.D.
  ///////////////////////////////////////////////////////////////

  /**
    * Creates a new game
    * @return the promise of the result of creating a new game
    */
  def createNewGame(form: ContestCreateForm)(implicit ec: ExecutionContext): Future[HttpResponse[Contest]] = {
    $http.post[Contest]("/api/contest", form)
  }

  def deleteContest(contestId: String): js.Promise[HttpResponse[js.Dynamic]] = {
    $http.delete[js.Dynamic](s"/api/contest/$contestId")
  }

  def joinContest(contestId: String, playerInfo: PlayerInfoForm): js.Promise[HttpResponse[Contest]] = {
    $http.put[Contest](s"/api/contest/$contestId/player", playerInfo)
  }

  def quitContest(contestId: String, playerId: String): js.Promise[HttpResponse[Contest]] = {
    $http.delete[Contest](s"/api/contest/$contestId/player/$playerId")
  }

  def startContest(contestId: String): js.Promise[HttpResponse[Contest]] = {
    $http.get[Contest](s"/api/contest/$contestId/start")
  }

  ///////////////////////////////////////////////////////////////
  //          Contest Finders
  ///////////////////////////////////////////////////////////////

  def findContests(searchOptions: ContestSearchOptions): js.Promise[HttpResponse[js.Array[Contest]]] = {
    $http.post[js.Array[Contest]]("/api/contests/search", searchOptions)
  }

  def getContestByID(contestId: String): js.Promise[HttpResponse[Contest]] = {
    $http.get[Contest](s"/api/contest/$contestId")
  }

  def getParticipantByID(contestId: String, playerId: String): js.Promise[HttpResponse[Contest]] = {
    $http.get[Contest](s"/api/contest/$contestId/player/$playerId")
  }

  def getContestsByPlayerID(playerId: String): js.Promise[HttpResponse[js.Array[Contest]]] = {
    $http.get[js.Array[Contest]](s"/api/contests/player/$playerId")
  }

}
