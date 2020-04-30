package com.shocktrade.client.dialogs

import com.shocktrade.client.contest.{AwardsSupport, AwardsSupportScope, GameLevel}
import com.shocktrade.client.dialogs.PlayerProfileDialogController._
import com.shocktrade.client.users.{OnlineStatusService, UserService}
import com.shocktrade.common.models.user.UserProfile
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
 * Player Profile Dialog Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PlayerProfileDialog($uibModal: Modal) extends Service {

  /**
   * Player Profile Modal Dialog
   */
  def popup(userID: String): js.Promise[PlayerProfileDialogResult] = {
    val $uibModalInstance = $uibModal.open[PlayerProfileDialogResult](new ModalOptions(
      templateUrl = "player_profile.html",
      controller = classOf[PlayerProfileDialogController].getSimpleName,
      resolve = js.Dictionary("userID" -> (() => userID))
    ))
    $uibModalInstance.result
  }

}

/**
 * Player Profile Dialog Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
case class PlayerProfileDialogController($scope: PlayerProfileDialogScope, $uibModalInstance: ModalInstance[PlayerProfileDialogResult],
                                         $timeout: Timeout, toaster: Toaster,
                                         @injected("OnlineStatusService") onlineStatusService: OnlineStatusService,
                                         @injected("UserService") userService: UserService,
                                         @injected("userID") userID: () => String) extends Controller with AwardsSupport {

  $scope.levels = GameLevel.Levels

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization
  ///////////////////////////////////////////////////////////////////////////

  $scope.initPlayerProfile = (aUserID: js.UndefOr[String]) => aUserID map  { userID =>
    console.info(s"Initializing ${getClass.getSimpleName} with '${aUserID.getOrElse("<undefined>")}'...")
    (for {
      a <- initPlayerProfile(userID)
      _ <- initAwards(userID)
    } yield a).toJSPromise
  }

  private def initPlayerProfile(userID: String): js.Promise[HttpResponse[UserProfile]] = {
    val outcome = userService.findUserByID(userID)
    outcome onComplete {
      case Success(userProfile) => $scope.$apply(() => $scope.player = userProfile.data)
      case Failure(e) =>
        e.printStackTrace()
    }
    outcome
  }

  // perform the initialization
  $scope.initPlayerProfile(userID())

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.getNextLevelXP = () => $scope.player.flatMap(_.nextLevelXP)

  $scope.getStars = () => (1 to Math.max(1, $scope.player.flatMap(_.totalXP.map(_ / 1000)).orZero)).toJSArray

  $scope.getTotalXP = () => $scope.player.flatMap(_.totalXP).getOrElse(0)

  $scope.isOnline = (aUserID: js.UndefOr[String]) => aUserID.map(onlineStatusService.getOnlineStatus).map(_.connected)

  $scope.okay = () => $uibModalInstance.close($scope.player)

}

/**
 * Player Profile Dialog Controller Companion
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PlayerProfileDialogController {

  type PlayerProfileDialogResult = js.UndefOr[UserProfile]

  @js.native
  trait PlayerProfileDialogScope extends Scope with AwardsSupportScope {
    // functions
    var initPlayerProfile: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[HttpResponse[UserProfile]]]] = js.native
    var getNextLevelXP: js.Function0[js.UndefOr[Int]] = js.native
    var getStars: js.Function0[js.Array[Int]] = js.native
    var getTotalXP: js.Function0[Int] = js.native
    var isOnline: js.Function1[js.UndefOr[String], js.UndefOr[Boolean]] = js.native
    var okay: js.Function0[Unit] = js.native

    // variables
    var levels: js.Array[GameLevel] = js.native
    var player: js.UndefOr[UserProfile] = js.native

  }

}