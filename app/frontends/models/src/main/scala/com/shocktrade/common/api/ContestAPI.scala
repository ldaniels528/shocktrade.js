package com.shocktrade.common.api

import com.shocktrade.common.forms.ContestSearchOptions

import scala.scalajs.js

/**
 * Contest API
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait ContestAPI extends BaseAPI {

  ///////////////////////////////////////////////////////////////
  //          Contest Management
  ///////////////////////////////////////////////////////////////

  def createContestURL: String = s"$baseURL/api/contest"

  def deleteContestURL(contestID: String) = s"$baseURL/api/contest/$contestID" // TODO do we need this?

  def joinContestURL(contestID: String, userID: String) = s"$baseURL/api/contest/$contestID/user/$userID"

  def quitContestURL(contestID: String, userID: String) = s"$baseURL/api/contest/$contestID/user/$userID"

  def startContestURL(contestID: String) = s"$baseURL/api/contest/$contestID/start" // TODO do we need this?

  ///////////////////////////////////////////////////////////////
  //          Contest Finders
  ///////////////////////////////////////////////////////////////

  def contestSearchURL(options: js.UndefOr[ContestSearchOptions] = js.undefined): String = {
    val uri = s"$baseURL/api/contests/search"
    options.map(opts => s"$uri?${opts.toQueryString}") getOrElse uri
  }

  def findContestByIDURL(contestID: String) = s"$baseURL/api/contest/$contestID"

  def findContestRankingsURL(contestID: String) = s"$baseURL/api/contest/$contestID/rankings"

  ///////////////////////////////////////////////////////////////
  //          Contest Messages
  ///////////////////////////////////////////////////////////////

  def findChatMessagesURL(contestID: String) = s"$baseURL/api/contest/$contestID/chat"

  def sendChatMessageURL(contestID: String) = s"$baseURL/api/contest/$contestID/chat"

}
