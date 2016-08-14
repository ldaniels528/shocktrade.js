package com.shocktrade.javascript.dialogs

import org.scalajs.angularjs._
import org.scalajs.angularjs.http.Http
import org.scalajs.angularjs.toaster.Toaster
import org.scalajs.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import org.scalajs.nodejs.util.ScalaJsHelper._
import com.shocktrade.javascript.MySessionService
import com.shocktrade.javascript.dialogs.PerksDialogController._
import com.shocktrade.javascript.models.{BSONObjectID, Contest, Perk}
import org.scalajs.dom.console

import scala.concurrent.Future
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Perks Dialog Service
  * @author lawrence.daniels@gmail.com
  */
class PerksDialog($http: Http, $modal: Modal) extends Service {

  /**
    * Perks Modal Dialog
    */
  def popup(): Future[PerksDialogResult] = {
    val $modalInstance = $modal.open[PerksDialogResult](new ModalOptions(
      templateUrl = "perks_dialog.htm",
      controller = classOf[PerksDialogController].getSimpleName
    ))
    $modalInstance.result
  }

  /**
    * Retrieves the promise of a sequence of perks
    * @return the promise of a sequence of [[Perk perks]]
    */
  def getPerks(contestId: BSONObjectID): Future[js.Array[Perk]] = {
    $http.get[js.Array[Perk]](s"/api/contest/${contestId.$oid}/perks")
  }

  /**
    * Retrieves the promise of an option of a perks response
    * @return the promise of an option of a [[PerksResponse perks response]]
    */
  def getMyPerkCodes(contestId: BSONObjectID, playerId: BSONObjectID): Future[PerksResponse] = {
    $http.get[PerksResponse](s"/api/contest/${contestId.$oid}/perks/${playerId.$oid}")
  }

  /**
    * Attempts to purchase the given perk codes
    * @param contestId the given contest ID
    * @param playerId  the given player ID
    * @param perkCodes the given perk codes to purchase
    * @return the promise of a [[Contest contest]]
    */
  def purchasePerks(contestId: BSONObjectID, playerId: BSONObjectID, perkCodes: js.Array[String]): Future[Contest] = {
    $http.put[Contest](s"/api/contest/${contestId.$oid}/perks/${playerId.$oid}", perkCodes)
  }
}

/**
  * Perks Dialog Controller
  * @author lawrence.daniels@gmail.com
  */
class PerksDialogController($scope: PerksDialogScope, $modalInstance: ModalInstance[PerksDialogResult], toaster: Toaster,
                            @injected("MySessionService") mySession: MySessionService,
                            @injected("PerksDialog") perksSvc: PerksDialog)
  extends Controller {

  private var myFunds: js.UndefOr[Double] = mySession.cashAccount_?.map(_.cashFunds).orUndefined
  private var myPerks = emptyArray[Perk]
  private var myPerkCodes = emptyArray[String]
  private var perkMapping = js.Dictionary[Perk]()

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.cancel = () => $modalInstance.dismiss("cancel")

  $scope.countOwnedPerks = () => myPerks.count(_.owned)

  $scope.getFundsAvailable = () => myFunds

  $scope.getPerks = () => myPerks

  $scope.getTotalCost = () => getSelectedPerks map (_.cost) sum

  $scope.hasSufficientFunds = () => $scope.getTotalCost() <= mySession.getFundsAvailable

  $scope.perksSelected = () => myPerks.exists(p => p.selected && !p.owned)

  $scope.getPerkCostClass = (aPerk: js.UndefOr[Perk]) => aPerk map { perk =>
    if (perk.selected || mySession.getFundsAvailable >= perk.cost) "positive"
    else if (mySession.getFundsAvailable < perk.cost) "negative"
    else "null"
  }

  $scope.getPerkNameClass = (aPerk: js.UndefOr[Perk]) => aPerk map { perk =>
    if (perk.selected || mySession.getFundsAvailable >= perk.cost) "st_bkg_color" else "null"
  }

  $scope.getPerkDescClass = (aPerk: js.UndefOr[Perk]) => aPerk map { perk =>
    if (perk.selected || mySession.getFundsAvailable >= perk.cost) "" else "null"
  }

  $scope.loadPerks = () => {
    val outcome = for {
      playerId <- mySession.userProfile._id.toOption
      contestId <- mySession.contest.flatMap(_._id.toOption)
    } yield (playerId, contestId)

    // load the player's perks
    outcome match {
      case Some((playerId, contestId)) =>
        perksSvc.getMyPerkCodes(contestId, playerId) onComplete {
          case Success(response) =>
            console.log(s"loadPerks: response = ${angular.toJson(response)}")
            myFunds = response.fundsAvailable
            myPerkCodes = response.perkCodes
            setupPerks()
          case Failure(e) =>
            toaster.error("Error loading player perks")
            console.error(s"Error loading player perks: ${e.getMessage}")
        }

        // load all contest perks
        perksSvc.getPerks(contestId) onComplete {
          case Success(loadedPerks) =>
            myPerks = loadedPerks
            perkMapping = js.Dictionary(myPerks.map(p => p.code -> p): _*)
            setupPerks()
          case Failure(e) =>
            toaster.error("Error loading available perks")
        }

      case None =>
        toaster.error("User session error")
    }
  }

  $scope.purchasePerks = () => {
    val outcome = for {
      playerId <- mySession.userProfile._id.toOption
      contestId <- mySession.contest.flatMap(_._id.toOption)
    } yield (playerId, contestId)

    outcome match {
      case Some((playerId, contestId)) =>
        val perkCodes = getSelectedPerks map (_.code)

        // send the purchase order
        perksSvc.purchasePerks(contestId, playerId, perkCodes) onComplete {
          case Success(contest) => $modalInstance.close(contest)
          case Failure(e) =>
            toaster.error(s"Failed to purchase ${perkCodes.length} Perk(s)")
            console.error(s"Error: Purchase Perks - ${e.getMessage}")
        }
      case None =>
        toaster.error("No game selected")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def getSelectedPerks = myPerks.filter(perk => perk.selected && !perk.owned)

  /**
    * Setup the perks state; indicating which perks are owned
    */
  private def setupPerks() {
    // pre-set all perks
    myPerks.foreach { perk =>
      perk.owned = false
      perk.selected = false
    }

    // select the owned perks
    myPerkCodes.foreach { perkCode =>
      perkMapping.get(perkCode) foreach { perk =>
        perk.owned = true
        perk.selected = true
      }
    }
  }

}

/**
  * Perks Dialog Controller
  * @author lawrence.daniels@gmail.com
  */
object PerksDialogController {

  type PerksDialogResult = Contest

}

/**
  * Perks Dialog Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait PerksDialogScope extends Scope {
  // functions
  var cancel: js.Function0[Unit] = js.native
  var countOwnedPerks: js.Function0[Int] = js.native
  var getFundsAvailable: js.Function0[js.UndefOr[Double]] = js.native
  var getPerks: js.Function0[js.Array[Perk]] = js.native
  var getTotalCost: js.Function0[Double] = js.native
  var hasSufficientFunds: js.Function0[Boolean] = js.native
  var perksSelected: js.Function0[Boolean] = js.native
  var getPerkCostClass: js.Function1[js.UndefOr[Perk], js.UndefOr[String]] = js.native
  var getPerkNameClass: js.Function1[js.UndefOr[Perk], js.UndefOr[String]] = js.native
  var getPerkDescClass: js.Function1[js.UndefOr[Perk], js.UndefOr[String]] = js.native
  var loadPerks: js.Function0[Unit] = js.native
  var purchasePerks: js.Function0[Unit] = js.native

}

/**
  * Represents a player's perk and available funds
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait PerksResponse extends js.Object {
  var perkCodes: js.Array[String] = js.native
  var fundsAvailable: js.UndefOr[Double] = js.native
}
