package com.shocktrade.remote.proxies

import com.shocktrade.common.api.UserAPI
import com.shocktrade.common.auth.{AuthenticationCode, AuthenticationForm, AuthenticationResponse}
import com.shocktrade.common.forms.SignUpForm
import com.shocktrade.common.models.user.{OnlineStatus, UserProfile}
import io.scalajs.nodejs.stream.Readable
import io.scalajs.npm.request.Request

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * User Proxy
 * @param host the given host
 * @param port the given port
 */
class UserProxy(host: String, port: Int)(implicit ec: ExecutionContext) extends Proxy with UserAPI {
  override val baseURL = s"http://$host:$port"

  //////////////////////////////////////////////////////////////////////
  //              Basic Functions
  //////////////////////////////////////////////////////////////////////

  /**
   * Creates a new user account
   * @param form the given [[SignUpForm]]
   * @return the promise of a user profile
   */
  def createAccount(form: SignUpForm): Future[UserProfile] = post(createAccountURL, form)

  /**
   * Retrieves a user by ID
   * @param userID the given user ID
   */
  def findUserByID(userID: String): Future[UserProfile] = get(findUserByIDURL(userID))

  /**
   * Retrieves a user by username
   * @param username the given username
   */
  def findUserByName(username: String): Future[UserProfile] = get(findUserByNameURL(username))

  /**
   * Retrieves a user image by ID
   * @param userID the given user ID
   */
  def findUserIcon(userID: String): Readable = Request(findUserIconURL(userID))

  /**
   * Retrieves an array of users by ID
   * @param userIDs the given user IDs
   */
  def findUsers(userIDs: js.Array[String]): Future[js.Array[UserProfile]] = get(findUsersURL(userIDs.mkString("+")))

  //////////////////////////////////////////////////////////////////////
  //              Awards Functions
  //////////////////////////////////////////////////////////////////////

  /**
   * Retrieves the collection of awards by ID
   * @param userID the given user ID
   */
  def findMyAwards(userID: String): Future[js.Array[String]] = get(findMyAwardsURL(userID))

  //////////////////////////////////////////////////////////////////////
  //              Authentication Functions
  //////////////////////////////////////////////////////////////////////

  def getCode: Future[AuthenticationCode] = get(getCodeURL)

  def login(form: AuthenticationForm): Future[UserProfile] = post(url = loginURL, data = form)

  def logout(): Future[AuthenticationResponse] = post(logoutURL)

  //////////////////////////////////////////////////////////////////////
  //              Online Status Functions
  //////////////////////////////////////////////////////////////////////

  def getOnlineStatus(userID: String): Future[OnlineStatus] = get(getOnlineStatusURL(userID))

  def getOnlineStatusUpdates(since: Double): Future[js.Array[OnlineStatus]] = get(getOnlineStatusUpdatesURL(since.toString))

  def setIsOnline(userID: String): Future[OnlineStatus] = put(setIsOnlineURL(userID))

  def setIsOffline(userID: String): Future[OnlineStatus] = delete(setIsOfflineURL(userID))

}
