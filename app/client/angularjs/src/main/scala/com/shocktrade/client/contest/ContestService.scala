package com.shocktrade.client.contest

import com.shocktrade.common.forms.PlayerInfoForm
import com.shocktrade.common.models.contest._
import com.shocktrade.client.models.contest.{Contest, ContestSearchOptions}
import org.scalajs.angularjs.Service
import org.scalajs.angularjs.http.Http

import scala.scalajs.js

/**
  * Contest Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ContestService($http: Http) extends Service {

  ///////////////////////////////////////////////////////////////
  //          Basic C.R.U.D.
  ///////////////////////////////////////////////////////////////

  def deleteContest(contestId: String) = {
    $http.delete[js.Dynamic](s"/api/contest/$contestId")
  }

  def joinContest(contestId: String, playerInfo: PlayerInfoForm) = {
    $http.put[Contest](s"/api/contest/$contestId/player", playerInfo)
  }

  def quitContest(contestId: String, playerId: String) = {
    $http.delete[Contest](s"/api/contest/$contestId/player/$playerId")
  }

  def startContest(contestId: String) = {
    $http.get[Contest](s"/api/contest/$contestId/start")
  }

  ///////////////////////////////////////////////////////////////
  //          Contest Finders
  ///////////////////////////////////////////////////////////////

  def findContests(searchOptions: ContestSearchOptions) = {
    $http.post[js.Array[Contest]]("/api/contests/search", searchOptions)
  }

  def getContestByID(contestId: String) = {
    $http.get[Contest](s"/api/contest/$contestId")
  }

  def getParticipantByID(contestId: String, playerId: String) = {
    $http.get[Contest](s"/api/contest/$contestId/player/$playerId")
  }

  def getContestsByPlayerID(playerId: String) = {
    $http.get[js.Array[Contest]](s"/api/contests/player/$playerId")
  }

}
