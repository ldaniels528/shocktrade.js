package com.shocktrade.client.contest

import com.shocktrade.client.models.contest.{Contest, ContestSearchOptions}
import com.shocktrade.common.forms.{ContestCreateForm, PlayerInfoForm}
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

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
  def createNewGame(form: ContestCreateForm): js.Promise[HttpResponse[Contest]] = {
    $http.post[Contest]("/api/contest", form)
  }

  def deleteContest(contestID: String): js.Promise[HttpResponse[js.Dynamic]] = {
    $http.delete[js.Dynamic](s"/api/contest/$contestID")
  }

  def joinContest(contestID: String, playerInfo: PlayerInfoForm): js.Promise[HttpResponse[Contest]] = {
    $http.put[Contest](s"/api/contest/$contestID/portfolio", playerInfo)
  }

  def quitContest(contestID: String, portfolioID: String): js.Promise[HttpResponse[Contest]] = {
    $http.delete[Contest](s"/api/contest/$contestID/portfolio/$portfolioID")
  }

  def startContest(contestID: String): js.Promise[HttpResponse[Contest]] = {
    $http.get[Contest](s"/api/contest/$contestID/start")
  }

  ///////////////////////////////////////////////////////////////
  //          Contest Finders
  ///////////////////////////////////////////////////////////////

  def findContests(searchOptions: ContestSearchOptions): js.Promise[HttpResponse[js.Array[Contest]]] = {
    $http.post[js.Array[Contest]]("/api/contests/search", searchOptions)
  }

  def findContestByID(contestID: String): js.Promise[HttpResponse[Contest]] = {
    $http.get[Contest](s"/api/contest/$contestID")
  }

  def findPortfolioByID(contestID: String, userID: String): js.Promise[HttpResponse[Contest]] = {
    $http.get[Contest](s"/api/contest/$contestID/user/$userID")
  }

  def findContestsByUserID(userID: String): js.Promise[HttpResponse[js.Array[Contest]]] = {
    $http.get[js.Array[Contest]](s"/api/contests/user/$userID")
  }

}
