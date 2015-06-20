package com.shocktrade.javascript.profile

import biz.enef.angulate._
import biz.enef.angulate.core.Timeout
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._

import scala.scalajs.js

/**
 * Statistics Controller
 * @author lawrence.daniels@gmail.com
 */
class StatisticsController($scope: js.Dynamic, $timeout: Timeout, toaster: Toaster,
                           @named("MySession") mySession: MySession) extends ScopeController {

  $scope.getAwards = () => mySession.userProfile.awards.asArray[js.Dynamic]

  $scope.getFriends = () => mySession.fbFriends

  $scope.getNextLevelXP = () => mySession.userProfile.nextLevelXP.asOpt[Double].getOrElse(0d)

  $scope.getStars = () => js.Array(1 to mySession.userProfile.rep.asOpt[Int].getOrElse(3): _*)

  $scope.getTotalXP = () => mySession.userProfile.totalXP.asOpt[Double].getOrElse(0d)


}
