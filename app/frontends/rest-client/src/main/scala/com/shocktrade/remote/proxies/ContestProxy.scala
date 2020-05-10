package com.shocktrade.remote.proxies

import com.shocktrade.common.Ok
import com.shocktrade.common.api.ContestAPI
import com.shocktrade.common.forms.{ContestCreationRequest, ContestCreationResponse, ContestSearchOptions}
import com.shocktrade.common.models.contest.{ChatMessage, ContestRanking, ContestSearchResult, PortfolioRef}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * Contest Proxy
 * @param host the given host
 * @param port the given port
 */
class ContestProxy(host: String, port: Int)(implicit ec: ExecutionContext) extends Proxy with ContestAPI {
  override val baseURL = s"http://$host:$port"

  ///////////////////////////////////////////////////////////////
  //          Contest Finders
  ///////////////////////////////////////////////////////////////

  def contestSearch(options: ContestSearchOptions): Future[js.Array[ContestSearchResult]] = get(contestSearchURL(options))

  def findContestByID(contestID: String): Future[js.Array[ContestRanking]] = get(findContestByIDURL(contestID))

  /**
   * Retrieves a collection of contest rankings
   * @param contestID the given contest ID
   * @return the promise of an array of [[ContestRanking contest rankings]]
   */
  def findContestRankings(contestID: String): Future[js.Array[ContestRanking]] = get(findContestRankingsURL(contestID))

  ///////////////////////////////////////////////////////////////
  //          Contest Lifecycle
  ///////////////////////////////////////////////////////////////

  /**
   * Creates a new game
   * @return the promise of the result of creating a new game
   */
  def createContest(request: ContestCreationRequest): Future[ContestCreationResponse] = post(createContestURL, request)

  def deleteContest(contestID: String): Future[Ok] = delete(deleteContestURL(contestID))

  def joinContest(contestID: String, userID: String): Future[PortfolioRef] = put(joinContestURL(contestID, userID))

  ///////////////////////////////////////////////////////////////
  //          Contest Messages
  ///////////////////////////////////////////////////////////////

  def findChatMessages(contestID: String): Future[js.Array[ChatMessage]] = get(findChatMessagesURL(contestID))

  def sendChatMessage(contestID: String, message: ChatMessage): Future[Ok] = post(sendChatMessageURL(contestID), message)

}
