package com.shocktrade.javascript.social

import com.github.ldaniels528.scalascript.Service
import com.github.ldaniels528.scalascript.core.Q
import com.shocktrade.javascript.ScalaJsHelper._
import org.scalajs.dom.console

import scala.beans.BeanProperty
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => JS}

/**
 * Facebook Service
 * @author lawrence.daniels@gmail.com
 */
class Facebook($q: Q) extends Service {
  type CallbackObject = js.Function1[js.Dynamic, Unit]
  type PaginationCallback[T] = js.Function1[FacebookPagination[T], Unit]

  // define the API version and App ID properties
  @BeanProperty val appID: String = FacebookInjector.getShockTradeAppID()
  @BeanProperty val version = "v2.3"

  // define the Facebook state variables
  var FB: js.Dynamic = null
  var facebookID: String = null
  var auth: AuthResponse = null
  var profile: FacebookProfile = null

  /**
   * Initializes the Facebook service
   */
  def init(fbSDK: js.Dynamic): Future[LoginStatusResponse] = {
    // capture the Facebook SDK
    FB = fbSDK

    // attempt to login
    getLoginStatus
  }

  def getLoginStatus: Future[LoginStatusResponse] = {
    val deferred = $q.defer[LoginStatusResponse]()
    if (!isDefined(FB)) deferred.reject("Facebook SDK is not loaded")
    else {
      // get the login status
      FB.getLoginStatus((response: LoginStatusResponse) => {
        if (response.status == "connected") {
          // capture the Facebook login status
          if (isDefined(response.authResponse)) {
            auth = response.authResponse
            facebookID = auth.userID
            deferred.resolve(response)
          }
          else deferred.reject("Facebook response was undefined")
        }
        else deferred.reject(s"Facebook is not connected (status: ${response.status})")
      })
    }
    deferred.promise
  }

  def login(): Future[LoginStatusResponse] = {
    val deferred = $q.defer[LoginStatusResponse]()
    if (!isDefined(FB)) deferred.reject("Facebook SDK is not loaded")
    else {
      FB.login((response: LoginStatusResponse) => {
        if (isDefined(response.authResponse)) {
          auth = response.authResponse
          facebookID = response.authResponse.userID
          deferred.resolve(response)
        } else {
          deferred.reject("Could not login into Facebook")
        }
      })
    }
    deferred.promise
  }

  def createFriendList(friendListId: String) = {
    val deferred = $q.defer[js.Dynamic]()
    if (!isDefined(FB)) deferred.reject("Facebook SDK is not loaded")
    else {
      FB.api(s"/$version/me/$friendListId/members&access_token=${auth.accessToken}", { (response: js.Dynamic) =>
        if (isDefined(response) && !isDefined(response.error)) deferred.resolve(response)
        else deferred.reject("Failed to create friends list")
      })
    }
    deferred.promise
  }

  def getFriends(callback: CallbackObject) {
    if (!isDefined(FB)) die("Facebook SDK is not loaded")
    else {
      FB.api(s"/$version/me/friends?access_token=${auth.accessToken}", (response: js.Dynamic) => callback(response))
    }
  }

  def getFriendList(listType: js.UndefOr[String]) = {
    val deferred = $q.defer[js.Dynamic]()
    if (!isDefined(FB)) deferred.reject("Facebook SDK is not loaded")
    else {
      FB.api(s"/$version/me/friendlists?list_type=${listType getOrElse "close_friends"}&access_token=${auth.accessToken}", (response: js.Dynamic) => {
        if (isDefined(response) && !isDefined(response.error)) deferred.resolve(response)
        else deferred.reject(s"Failed to retrieve friends list (type $listType)")
      })
    }
    deferred.promise
  }

  def getFriendListMembers(friendListId: String) = {
    val deferred = $q.defer[js.Dynamic]()
    if (!isDefined(FB)) deferred.reject("Facebook SDK is not loaded")
    else {
      FB.api(s"/$version/me/$friendListId/members&access_token=${auth.accessToken}", (response: js.Dynamic) => {
        if (isDefined(response) && !isDefined(response.error)) deferred.resolve(response)
        else deferred.reject("Failed to retrieve friend list members")
      })
    }
    deferred.promise
  }

  /**
   * Retrieves all taggable friends for the authenticated user
   * @return the array of [[TaggableFriend taggable friends]]
   */
  def getTaggableFriends: Future[js.Array[TaggableFriend]] = {
    val deferred = $q.defer[js.Array[TaggableFriend]]()
    if (!isDefined(FB)) deferred.reject("Facebook SDK is not loaded")
    else {
      val friends = emptyArray[TaggableFriend]
      val callback: PaginationCallback[TaggableFriend] = (response: FacebookPagination[TaggableFriend]) => {
        val results = response.data.asArray[TaggableFriend]
        friends.push(results: _*)
        console.log(s"${friends.length} friend(s) loaded")
        ()
      }
      FB.api(s"/$version/me/taggable_friends?access_token=${auth.accessToken}", { (response: TaggleFriendsResponse) =>
        paginatedResults(response, callback)
        deferred.resolve(friends)
      })
    }
    deferred.promise
  }

  private def paginatedResults[T](response: FacebookPagination[T], callback: PaginationCallback[T]) {
    // perform the callback for this response
    callback(response)

    // if there are more results, recursively extract them
    if (isDefined(response.paging) && isDefined(response.paging.next)) {
      FB.api(response.paging.next, (response: FacebookPagination[T]) => paginatedResults(response, callback))
    }
  }

  def getUserProfile: Future[FacebookProfile] = {
    val deferred = $q.defer[FacebookProfile]()
    if (!isDefined(FB)) deferred.reject("Facebook SDK is not loaded")
    else {
      FB.api(s"/$version/me?access_token=${auth.accessToken}", (response: js.Dynamic) =>
        if (isDefined(response.error)) deferred.reject(response.error)
        else {
          profile = response.as[FacebookProfile]
          deferred.resolve(profile)
        })
    }
    deferred.promise
  }

  def logout(): Future[LoginStatusResponse] = {
    val deferred = $q.defer[LoginStatusResponse]()
    if (!isDefined(FB)) deferred.reject("Facebook SDK is not loaded")
    else {
      FB.logout((response: LoginStatusResponse) => {
        if (isDefined(response)) {
          auth = null
          deferred.resolve(response)
        } else {
          deferred.reject("Error logging out of Facebook")
        }
      })
    }
    deferred.promise
  }

  def feed(caption: String, link: String) = {
    val deferred = $q.defer[js.Dynamic]()
    FB.ui(JS(
      app_id = appID,
      method = "feed",
      link = link,
      caption = caption
    ), (response: js.Dynamic) => if (isDefined(response.error)) deferred.reject(response.error) else deferred.resolve(response))
    deferred.promise
  }

  def send(message: String, link: String) = {
    val deferred = $q.defer[js.Dynamic]()
    FB.ui(JS(
      app_id = appID,
      method = "send",
      link = link
    ), (response: js.Dynamic) => if (isDefined(response.error)) deferred.reject(response.error) else deferred.resolve(response))
    deferred.promise
  }

  def share(link: String) = {
    val deferred = $q.defer[js.Dynamic]()
    FB.ui(JS(
      app_id = appID,
      method = "share",
      href = link
    ), (response: js.Dynamic) => if (isDefined(response.error)) deferred.reject(response.error) else deferred.resolve(response))
    deferred.promise
  }
}

/*
 * Facebook Login Response
 */
trait LoginStatusResponse extends js.Object {
  var authResponse: AuthResponse = js.native
  var status: String = js.native
}

/*
 * Facebook Login: Auth Response
 */
trait AuthResponse extends js.Object {
  var accessToken: String = js.native
  var signedRequest: String = js.native
  var userID: String = js.native
  var expiresIn: Int = js.native
}

/**
 * Represents a Facebook Profile
 * @author lawrence.daniels@gmail.com
 */
trait FacebookProfile extends js.Object {
  var id: String = js.native
  var first_name: String = js.native
  var last_name: String = js.native
  var name: String = js.native
  var gender: String = js.native
  var link: String = js.native
  var locale: String = js.native
  var updated_time: js.Date = js.native
  var timezone: Int = js.native
  var verified: Boolean = js.native
}

/**
 * Facebook Friend Picture
 */
trait FacebookPicture extends js.Object {
  var data: FacebookPictureData = js.native
}

/**
 * Facebook Friend Picture Data
 */
trait FacebookPictureData extends js.Object {
  var is_silhouette: Boolean = js.native
  var url: String = js.native
}

/**
 * Facebook Taggable Friend
 */
trait TaggableFriend extends js.Object {
  var id: String = js.native
  var name: String = js.native
  var picture: FacebookPicture = js.native
}

/**
 * Facebook Taggable Friends Response
 */
trait TaggleFriendsResponse extends FacebookPagination[TaggableFriend]

/**
 * Facebook Pagination Trait
 */
trait FacebookPagination[T] extends js.Object {
  var data: js.Array[T] = js.native
  var paging: FacebookPaging = js.native
}

/**
 * Facebook Paging Trait
 */
trait FacebookPaging extends js.Object {
  var cursors: FacebookCursor = js.native
  var next: String = js.native
}

/**
 * Facebook Cursor Trait
 */
trait FacebookCursor extends js.Object {
  var before: String = js.native
  var after: String = js.native
}
