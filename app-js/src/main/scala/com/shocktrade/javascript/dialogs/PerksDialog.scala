package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.core.Http
import com.github.ldaniels528.scalascript.extensions.{Modal, ModalInstance, ModalOptions, Toaster}
import com.github.ldaniels528.scalascript.{Controller, Scope, Service, angular, injected, scoped}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.PerksDialogController._
import com.shocktrade.javascript.models.{Contest, Perk}
import org.scalajs.dom.console

import scala.concurrent.Future
import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.UndefOr
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
    // create an instance of the dialog
    val $modalInstance = $modal.open[PerksDialogResult](ModalOptions(
      templateUrl = "perks_dialog.htm",
      controllerClass = classOf[PerksDialogController]
    ))
    $modalInstance.result
  }

  /**
   * Retrieves the promise of a sequence of perks
   * @return the promise of a sequence of [[Perk perks]]
   */
  def getPerks(contestId: String): Future[js.Array[Perk]] = {
    required("contestId", contestId)
    $http.get[js.Array[Perk]](s"/api/contest/$contestId/perks")
  }

  /**
   * Retrieves the promise of an option of a perks response
   * @return the promise of an option of a [[PerksResponse perks response]]
   */
  def getMyPerkCodes(contestId: String, playerId: String): Future[PerksResponse] = {
    required("contestId", contestId)
    required("userId", playerId)
    $http.get[PerksResponse](s"/api/contest/$contestId/perks/$playerId")
  }

  /**
   * Attempts to purchase the given perk codes
   * @param contestId the given contest ID
   * @param playerId the given player ID
   * @param perkCodes the given perk codes to purchase
   * @return the promise of a [[Contest contest]]
   */
  def purchasePerks(contestId: String, playerId: String, perkCodes: js.Array[String]): Future[Contest] = {
    required("contestId", contestId)
    required("playerId", playerId)
    required("perkCodes", perkCodes)
    $http.put[Contest](s"/api/contest/$contestId/perks/$playerId", perkCodes)
  }
}

/**
 * Perks Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
class PerksDialogController($scope: PerksDialogScope, $modalInstance: ModalInstance[PerksDialogResult], toaster: Toaster,
                            @injected("MySession") mySession: MySession,
                            @injected("PerksDialog") perksSvc: PerksDialog)
  extends Controller {

  private var myFunds: UndefOr[Double] = mySession.cashAccount_?.map(_.cashFunds).orUndefined
  private var myPerks = emptyArray[Perk]
  private var myPerkCodes = emptyArray[String]
  private var perkMapping = js.Dictionary[Perk]()

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  @scoped def cancel(): Unit = $modalInstance.dismiss("cancel")

  @scoped def countOwnedPerks: Int = myPerks.count(_.owned)

  @scoped def getFundsAvailable: UndefOr[Double] = myFunds

  @scoped def getPerks: js.Array[Perk] = myPerks

  @scoped def getTotalCost: Double = computeSelectedPerksCost

  @scoped def hasSufficientFunds: Boolean = computeSelectedPerksCost <= mySession.getFundsAvailable

  @scoped def perksSelected: Boolean = myPerks.exists(p => p.selected && !p.owned)

  @scoped
  def getPerkCostClass(perk: Perk): String = {
    if (perk.selected || mySession.getFundsAvailable >= perk.cost) "positive"
    else if (mySession.getFundsAvailable < perk.cost) "negative"
    else "null"
  }

  @scoped
  def getPerkNameClass(perk: Perk): String = {
    if (perk.selected || mySession.getFundsAvailable >= perk.cost) "st_bkg_color" else "null"
  }

  @scoped
  def getPerkDescClass(perk: Perk): String = {
    if (perk.selected || mySession.getFundsAvailable >= perk.cost) "" else "null"
  }

  @scoped
  def loadPerks() = initPerks()

  @scoped
  def purchasePerks(): Unit = {
    val outcome = for {
      playerId <- mySession.userProfile.OID_?
      contestId <- mySession.contest.flatMap(_.OID_?)
    } yield (playerId, contestId)

    outcome match {
      case Some((playerId, contestId)) =>
        val perkCodes = getSelectedPerkCodes

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

  private def getSelectedPerkCodes: js.Array[String] = getSelectedPerks map (_.code)

  private def getSelectedPerks: js.Array[Perk] = myPerks.filter(perk => perk.selected && !perk.owned)

  private def computeSelectedPerksCost: Double = getSelectedPerks map (_.cost) sum

  private def initPerks() = {
    val outcome = for {
      playerId <- mySession.userProfile.OID_?
      contestId <- mySession.contest.flatMap(_.OID_?)
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
 */
trait PerksDialogScope extends Scope

/**
 * Represents a player's perk and available funds
 */
trait PerksResponse extends js.Object {
  var perkCodes: js.Array[String] = js.native
  var fundsAvailable: js.UndefOr[Double] = js.native
}
