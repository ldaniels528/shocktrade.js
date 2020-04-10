package com.shocktrade.client.contest

import com.shocktrade.common.Ok
import com.shocktrade.common.forms.{ContestCreationForm, ContestCreationResponse, ContestSearchForm}
import com.shocktrade.common.models.contest.{ChatMessage, ContestRanking, ContestSearchResult}
import com.shocktrade.common.models.quote.Ticker
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

  def deleteContest(contestID: String): js.Promise[HttpResponse[Ok]] = {
    $http.delete(s"/api/contest/$contestID")
  }

  def joinContest(contestID: String, userID: String): js.Promise[HttpResponse[Ok]] = {
    $http.put(s"/api/contest/$contestID/user/$userID")
  }

  def quitContest(contestID: String, userID: String): js.Promise[HttpResponse[Ok]] = {
    $http.delete(s"/api/contest/$contestID/user/$userID")
  }

  def startContest(contestID: String): js.Promise[HttpResponse[Ok]] = {
    $http.get(s"/api/contest/$contestID/start")
  }

  ///////////////////////////////////////////////////////////////
  //          Contest Finders
  ///////////////////////////////////////////////////////////////

  def findContests(searchOptions: ContestSearchForm): js.Promise[HttpResponse[js.Array[ContestSearchResult]]] = {
    $http.post("/api/contests/search", searchOptions)
  }

  def findContestByID(contestID: String): js.Promise[HttpResponse[ContestSearchResult]] = {
    $http.get(s"/api/contest/$contestID")
  }

  def findHeldSecurities(userID: String): js.Promise[HttpResponse[js.Array[Ticker]]] = {
    $http.get(s"/api/contest/user/$userID/heldSecurities")
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

  def findChatMessages(contestID: String): js.Promise[HttpResponse[js.Array[ChatMessage]]] = {
    $http.get(s"/api/contest/$contestID/chat")
  }

  def sendChatMessage(contestID: String, message: ChatMessage): js.Promise[HttpResponse[Ok]] = {
    $http.post(s"/api/contest/$contestID/chat", message)
  }

}
