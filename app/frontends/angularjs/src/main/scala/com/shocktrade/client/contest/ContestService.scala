package com.shocktrade.client.contest

import com.shocktrade.common.Ok
import com.shocktrade.common.api.ContestAPI
import com.shocktrade.common.forms.{ContestCreationRequest, ContestCreationResponse, ContestSearchOptions}
import com.shocktrade.common.models.contest.{ChatMessage, ContestRanking, ContestSearchResult}
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.scalajs.js

/**
 * Contest Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class ContestService($http: Http) extends Service with ContestAPI {

  ///////////////////////////////////////////////////////////////
  //          Basic C.R.U.D.
  ///////////////////////////////////////////////////////////////

  /**
   * Creates a new game
   * @return the promise of the result of creating a new game
   */
  def createContest(request: ContestCreationRequest): js.Promise[HttpResponse[ContestCreationResponse]] = $http.post(createContestURL, request)

  def deleteContest(contestID: String): js.Promise[HttpResponse[Ok]] = $http.delete(deleteContestURL(contestID))

  def joinContest(contestID: String, userID: String): js.Promise[HttpResponse[Ok]] = $http.put(joinContestURL(contestID, userID))

  def quitContest(contestID: String, userID: String): js.Promise[HttpResponse[Ok]] = $http.delete(quitContestURL(contestID, userID))

  def startContest(contestID: String): js.Promise[HttpResponse[Ok]] = $http.get(startContestURL(contestID))

  ///////////////////////////////////////////////////////////////
  //          Contest Finders
  ///////////////////////////////////////////////////////////////

  def contestSearch(options: ContestSearchOptions): js.Promise[HttpResponse[js.Array[ContestSearchResult]]] = $http.get(contestSearchURL(options))

  def findContestByID(contestID: String): js.Promise[HttpResponse[ContestSearchResult]] = $http.get(findContestByIDURL(contestID))

  /**
   * Retrieves a collection of contest rankings
   * @param contestID the given contest ID
   * @return the promise of an array of [[ContestRanking contest rankings]]
   */
  def findContestRankings(contestID: String): js.Promise[HttpResponse[js.Array[ContestRanking]]] = $http.get(findContestRankingsURL(contestID))

  ///////////////////////////////////////////////////////////////
  //          Contest Messages
  ///////////////////////////////////////////////////////////////

  def findChatMessages(contestID: String): js.Promise[HttpResponse[js.Array[ChatMessage]]] = $http.get(findChatMessagesURL(contestID))

  def sendChatMessage(contestID: String, message: ChatMessage): js.Promise[HttpResponse[Ok]] = $http.post(sendChatMessageURL(contestID), message)

}
