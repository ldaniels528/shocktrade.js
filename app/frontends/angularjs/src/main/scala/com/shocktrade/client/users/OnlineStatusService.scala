package com.shocktrade.client.users

import com.shocktrade.client.RootScope
import com.shocktrade.common.models.user.OnlineStatus
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.{Service, Timeout, injected}
import io.scalajs.util.DurationHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.scalajs.concurrent.JSExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Online Status Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class OnlineStatusService($rootScope: RootScope, $timeout: Timeout,
                          @injected("UserService") userService: UserService) extends Service {
  private val onlineStatuses = js.Dictionary[OnlineStatus]()
  private var lastUpdateTime: Double = 0

  // update the online statuses once per minute
  $timeout(() => performStatusUpdates()(JSExecutionContext.queue), 1.minute)

  def getOnlineStatus(userID: String)(implicit ec: ExecutionContext): OnlineStatus = {
    var isTriggered = false
    val onlineStatus = onlineStatuses.getOrElseUpdate(userID, {
      isTriggered = true
      new OnlineStatus(userID)
    })

    // is a service call being triggered?
    if (isTriggered) {
      userService.getOnlineStatus(userID) onComplete {
        case Success(response) => $rootScope.$apply(() => onlineStatuses(userID) = response.data)
        case Failure(e) =>
          e.printStackTrace()
      }
    }

    onlineStatus
  }

  def setIsOnline(userID: String): Unit = {
    onlineStatuses.getOrElseUpdate(userID, new OnlineStatus(userID)).connected = true
  }

  def setIsOffline(userID: String): Unit = onlineStatuses.get(userID).foreach(_.connected = false)

  def performStatusUpdates()(implicit ec: ExecutionContext): js.Promise[HttpResponse[js.Array[OnlineStatus]]] = {
    val startTime = js.Date.now()
    val outcome = userService.getOnlineStatusUpdates(lastUpdateTime)
    outcome onComplete {
      case Success(statuses) =>
        $rootScope.$apply(() => statuses.data.foreach(status => onlineStatuses(status.userID) = status))
        val elapsedTime = js.Date.now() - startTime
        console.info(s"Updated ${statuses.data.length} user statuses in $elapsedTime msec")
      case Failure(e) =>
        console.error(s"Failed to update player statuses: ${e.getMessage}")
    }
    lastUpdateTime = startTime
    outcome
  }

}
