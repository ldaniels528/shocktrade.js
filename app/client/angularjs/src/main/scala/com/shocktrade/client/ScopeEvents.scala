package com.shocktrade.client

import com.shocktrade.common.events.RemoteEvent._
import com.shocktrade.common.models.contest.{ChatMessage, Participant}
import com.shocktrade.client.models.Profile
import com.shocktrade.client.models.contest.Contest
import org.scalajs.angularjs.{Scope, angular}
import org.scalajs.dom
import org.scalajs.dom.browser.console

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
    def emit(action: String, data: js.Any) = {
      console.log(s"Broadcasting action '$action' => ${angular.toJson(data)}...")
      action match {
        case ChatMessagesUpdated =>
          $scope.$emit(action, data)
          $scope.$broadcast(action, data)
        case _ =>
          $scope.$broadcast(action, data)
      }
    }

    @inline
    def emitContestCreated(contest: Contest) = broadcast(ContestCreated, contest)

    @inline
    def emitContestDeleted(contest: Contest) = broadcast(ContestDeleted, contest)

    @inline
    def emitContestSelected(contest: Contest) = broadcast(ContestSelected, contest)

    @inline
    def emitMessagesUpdated(message: ChatMessage) = broadcast(ChatMessagesUpdated, message)

    @inline
    def emitUserProfileChanged(profile: Profile) = broadcast(UserProfileChanged, profile)

    @inline
    def emitUserProfileUpdated(profile: Profile) = broadcast(UserProfileUpdated, profile)

    @inline
    def emitUserStatusChanged(status: String) = broadcast(UserStatusChanged, status)

    /////////////////////////////////////////////////////////////////////
    //          Reactors
    /////////////////////////////////////////////////////////////////////

    @inline
    def onContestCreated(callback: (dom.Event, Contest) => Any) = reactTo(ContestCreated, callback)

    @inline
    def onContestDeleted(callback: (dom.Event, Contest) => Any) = reactTo(ContestDeleted, callback)

    @inline
    def onContestSelected(callback: (dom.Event, Contest) => Any) = reactTo(ContestSelected, callback)

    @inline
    def onContestUpdated(callback: (dom.Event, Contest) => Any) = reactTo(ContestUpdated, callback)

    @inline
    def onMessagesUpdated(callback: (dom.Event, String) => Any) = reactTo(ChatMessagesUpdated, callback)

    @inline
    def onOrderUpdated(callback: (dom.Event, String) => Any) = reactTo(OrderUpdated, callback)

    @inline
    def onParticipantUpdated(callback: (dom.Event, Participant) => Any) = reactTo(ParticipantUpdated, callback)

    @inline
    def onUserProfileChanged(callback: (dom.Event, Profile) => Any) = reactTo(UserProfileChanged, callback)

    @inline
    def onUserProfileUpdated(callback: (dom.Event, Profile) => Any) = reactTo(UserProfileUpdated, callback)

    @inline
    def onUserStatusChanged(callback: (dom.Event, String) => Any) = reactTo(UserStatusChanged, callback)

    /////////////////////////////////////////////////////////////////////
    //          Private Methods
    /////////////////////////////////////////////////////////////////////

    @inline
    private def broadcast(action: String, entity: js.Any) = {
      console.info(s"Broadcasting $action: payload => ${angular.toJson(entity)}")
      $scope.$broadcast(ContestCreated, entity)
    }

    private def reactTo(action: String, callback: js.Function) = {
      console.info(s"Listening for '$action'...")
      $scope.$on(action, callback)
    }

  }

}
