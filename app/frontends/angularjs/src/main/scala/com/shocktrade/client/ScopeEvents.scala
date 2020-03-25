package com.shocktrade.client

import com.shocktrade.client.models.UserProfile
import com.shocktrade.client.models.contest.{Contest, Portfolio}
import com.shocktrade.common.events.RemoteEvent._
import com.shocktrade.common.models.contest.ChatMessage
import io.scalajs.dom.Event
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.{Scope, angular}

import scala.scalajs.js

/**
 * ShockTrade Application Events
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object ScopeEvents {

  /**
   * Scope Enrichment
   * @param $scope the given [[Scope scope]]
   * @tparam T the given type
   */
  implicit class ScopeEnrichment[T <: Scope](val $scope: T) extends AnyVal {

    /////////////////////////////////////////////////////////////////////
    //          Emitters
    /////////////////////////////////////////////////////////////////////

    @inline
    def emit(action: String, data: js.Any) {
      console.log(s"Broadcasting action '$action' => ${angular.toJson(data)}...")
      action match {
        case ChatMessagesUpdated =>
        //  $scope.$emit(action, data)
          $scope.$broadcast(action, data)
        case _ =>
          $scope.$emit(action, data)
      }
    }

    @inline
    def emitContestCreated(contest: Contest): Unit = broadcast(ContestCreated, contest)

    @inline
    def emitContestDeleted(contest: Contest): Unit = broadcast(ContestDeleted, contest)

    @inline
    def emitContestSelected(contest: Contest): Unit = broadcast(ContestSelected, contest)

    @inline
    def emitMessagesUpdated(message: ChatMessage): Unit = broadcast(ChatMessagesUpdated, message)

    @inline
    def emitPortfolioChanged(portfolio: Portfolio): Unit = broadcast(PortfolioUpdated, portfolio)

    @inline
    def emitUserProfileUpdated(profile: UserProfile): Unit = broadcast(UserProfileUpdated, profile)

    /////////////////////////////////////////////////////////////////////
    //          Reactors
    /////////////////////////////////////////////////////////////////////

    @inline
    def onContestCreated(callback: (Event, Contest) => Any): Unit = reactTo(ContestCreated, callback)

    @inline
    def onContestDeleted(callback: (Event, Contest) => Any): Unit = reactTo(ContestDeleted, callback)

    @inline
    def onContestSelected(callback: (Event, Contest) => Any): Unit = reactTo(ContestSelected, callback)

    @inline
    def onMessagesUpdated(callback: (Event, String) => Any): Unit = reactTo(ChatMessagesUpdated, callback)

    @inline
    def onOrderUpdated(callback: (Event, String) => Any): Unit = reactTo(OrderUpdated, callback)

    @inline
    def onPortfolioUpdated(callback: (Event, Portfolio) => Any): Unit = reactTo(PortfolioUpdated, callback)

    @inline
    def onUserProfileUpdated(callback: (Event, UserProfile) => Any): Unit = reactTo(UserProfileUpdated, callback)

    /////////////////////////////////////////////////////////////////////
    //          Private Methods
    /////////////////////////////////////////////////////////////////////

    @inline
    private def broadcast(action: String, entity: js.Any): Unit = {
      console.info(s"Broadcasting $action: payload => ${angular.toJson(entity)}")
      $scope.$broadcast(action, entity)
      // $scope.$emit(action, entity)
      ()
    }

    private def reactTo(action: String, callback: js.Function): Unit = {
      console.info(s"Listening for '$action'...")
      $scope.$on(action, callback)
      ()
    }

  }

}
