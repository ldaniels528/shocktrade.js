package com.shocktrade.common.api

/**
 * Contest API
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait ContestAPI extends BaseAPI {

  ///////////////////////////////////////////////////////////////
  //          Basic C.R.U.D.
  ///////////////////////////////////////////////////////////////

  def createNewGameURL: String = s"$baseURL/api/contest"

  def deleteContestURL(contestID: String) = s"$baseURL/api/contest/$contestID" // TODO do we need this?

  def joinContestURL(contestID: String, userID: String) = s"$baseURL/api/contest/$contestID/user/$userID"

  def quitContestURL(contestID: String, userID: String) = s"$baseURL/api/contest/$contestID/user/$userID"

  def startContestURL(contestID: String) = s"$baseURL/api/contest/$contestID/start" // TODO do we need this?

  ///////////////////////////////////////////////////////////////
  //          Contest Finders
  ///////////////////////////////////////////////////////////////

  def contestSearchURL: String = s"$baseURL/api/contests/search"

  def findContestByIDURL(contestID: String) = s"$baseURL/api/contest/$contestID"

  def findContestRankingsURL(contestID: String) = s"$baseURL/api/contest/$contestID/rankings"

  ///////////////////////////////////////////////////////////////
  //          Contest Messages
  ///////////////////////////////////////////////////////////////

  def findChatMessagesURL(contestID: String) = s"$baseURL/api/contest/$contestID/chat"

  def putChatMessageURL(contestID: String) = s"$baseURL/api/contest/$contestID/chat"

  ///////////////////////////////////////////////////////////////
  //          Administrative Routes
  ///////////////////////////////////////////////////////////////

  def getUpdateUserIconsURL: String = s"$baseURL/api/contest/users/icon/update"

}
