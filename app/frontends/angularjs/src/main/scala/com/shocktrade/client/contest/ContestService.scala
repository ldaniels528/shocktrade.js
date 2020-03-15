package com.shocktrade.client.contest

import com.shocktrade.client.models.contest.ContestSearchResultUI
import com.shocktrade.common.forms.{ContestCreationForm, ContestCreationResponse, ContestSearchForm, PlayerInfoForm}
import com.shocktrade.common.models.contest.{ChatMessage, ContestRanking, ContestSearchResult, MyContest}
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
  def createNewGame(form: ContestCreationForm): js.Promise[HttpResponse[ContestCreationResponse]] = {
    $http.post("/api/contest", form)
  }

  def deleteContest(contestID: String): js.Promise[HttpResponse[ContestSearchResultUI]] = {
    $http.delete(s"/api/contest/$contestID")
  }

  def joinContest(contestID: String, playerInfo: PlayerInfoForm): js.Promise[HttpResponse[ContestSearchResultUI]] = {
    $http.put(s"/api/contest/$contestID/portfolio", playerInfo)
  }

  def quitContest(contestID: String, portfolioID: String): js.Promise[HttpResponse[ContestSearchResultUI]] = {
    $http.delete(s"/api/contest/$contestID/portfolio/$portfolioID")
  }

  def startContest(contestID: String): js.Promise[HttpResponse[ContestSearchResultUI]] = {
    $http.get(s"/api/contest/$contestID/start")
  }

  ///////////////////////////////////////////////////////////////
  //          Contest Finders
  ///////////////////////////////////////////////////////////////

  def findContests(searchOptions: ContestSearchForm): js.Promise[HttpResponse[js.Array[ContestSearchResult]]] = {
    $http.post("/api/contests/search", searchOptions)
  }

  def findContestByID(contestID: String): js.Promise[HttpResponse[ContestSearchResultUI]] = {
    $http.get(s"/api/contest/$contestID")
  }

  def findHeldSecurities(userID: String): js.Promise[HttpResponse[js.Array[String]]] = {
    $http.get(s"/api/contest/user/$userID/heldSecurities")
  }

  def findPortfolioByID(contestID: String, userID: String): js.Promise[HttpResponse[ContestSearchResultUI]] = {
    $http.get(s"/api/contest/$contestID/user/$userID")
  }

  def findMyContests(userID: String): js.Promise[HttpResponse[js.Array[MyContest]]] = {
    $http.get(s"/api/contests/user/$userID")
  }

  /**
   * Retrieves a collection of contest rankings
   * @param contestID the given contest ID
   * @return the promise of an array of [[ContestRanking contest rankings]]
   */
  def findRankingsByContest(contestID: String): js.Promise[HttpResponse[js.Array[ContestRanking]]] = {
    $http.get(s"/api/contest/$contestID/rankings")
  }

  ///////////////////////////////////////////////////////////////
  //          Contest Messages
  ///////////////////////////////////////////////////////////////

  def getMessages(contestID: String): js.Promise[HttpResponse[js.Array[ChatMessage]]] = {
    $http.get[js.Array[ChatMessage]](s"/api/contest/$contestID/chat")
  }

  def sendChatMessage(contestID: String, userID: String, message: ChatMessage): js.Promise[HttpResponse[js.Array[ChatMessage]]] = {
    $http.post[js.Array[ChatMessage]](s"/api/contest/$contestID/user/$userID/chat", message)
  }

}
