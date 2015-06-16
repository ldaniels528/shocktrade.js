package com.shocktrade.javascript.dialogs

import biz.enef.angulate.core.{HttpPromise, HttpService}
import biz.enef.angulate.{ScopeController, Service, named}
import com.greencatsoft.angularjs.core.Promise
import com.greencatsoft.angularjs.extensions.{ModalInstance, ModalOptions, ModalService}
import com.ldaniels528.angularjs.Toaster
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.PerksDialog._

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.util.{Failure, Success}

/**
 * Perks Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class PerksDialog($http: HttpService, $modal: ModalService, @named("MySession") mySession: MySession)
  extends Service {

  /**
   * Perks Modal Dialog
   */
  def popup(): Promise = {
    val options = ModalOptions()
    options.templateUrl = "perks_dialog.htm"
    options.controller = classOf[PerksDialogController].getSimpleName

    // create an instance of the dialog
    val $modalInstance = $modal.open(options)
    $modalInstance.result
  }

  /**
   * Retrieves the promise of a sequence of perks
   * @return the promise of a sequence of [[Perk perks]]
   */
  def getPerks(contestId: String): HttpPromise[js.Array[js.Dynamic]] = {
    required("contestId", contestId)

    $http.get[js.Array[js.Dynamic]](s"/api/contest/$contestId/perks")
  }

  /**
   * Retrieves the promise of an option of a perks response
   * @return the promise of an option of a [[PerksResponse perks response]]
   */
  def getMyPerks(contestId: String, playerId: String): HttpPromise[PerksResponse] = {
    required("contestId", contestId)
    required("userId", playerId)

    val task = $http.get[js.Dynamic](s"/api/contest/$contestId/perks/$playerId")
    task.map(_.toPerksResponse)
  }

  /**
   * Attempts to purchase the given perk codes
   * @param contestId the given contest ID
   * @param playerId the given player ID
   * @param perkCodes the given perk codes to purchase
   * @return {*}
   */
  def purchasePerks(contestId: String, playerId: String, perkCodes: js.Array[String]): HttpPromise[js.Dynamic] = {
    required("contestId", contestId)
    required("playerId", playerId)
    required("perkCodes", perkCodes)

    $http.put[js.Dynamic](s"/api/contest/$contestId/perks/$playerId", perkCodes)
  }

}

/**
 * Perks Dialog Singleton
 * @author lawrence.daniels@gmail.com
 */
object PerksDialog {

  /**
   * Perks Dialog Controller
   * @author lawrence.daniels@gmail.com
   */
  class PerksDialogController($scope: js.Dynamic, $modalInstance: ModalInstance, toaster: Toaster,
                              @named("MySession") mySession: MySession,
                              @named("PerksDialog") dialog: PerksDialog)
    extends ScopeController {

    private var perks = emptyArray[js.Dynamic]
    private var perkMapping = js.Dictionary[js.Dynamic]()
    private var myPerks = emptyArray[String]
    private var myFunds = mySession.getCashAccount().cashFunds.as[Double]

    $scope.cancel = () => $modalInstance.dismiss("cancel")

    $scope.countOwnedPerks = () => perks.count(_.owned.as[Boolean])

    $scope.getPerks = () => perks

    $scope.getTotalCost = () => getSelectedPerksCost

    $scope.hasSufficientFunds = () => getSelectedPerksCost <= mySession.getFundsAvailable()

    $scope.perksSelected = () => perks.exists(p => p.selected.as[Boolean] && !p.owned.as[Boolean])

    $scope.purchasePerks = () => purchasePerks()

    $scope.getPerkCostClass = (perkJs: js.Dynamic) => {
      perkJs.toPerk map { perk =>
        if (perk.selected || mySession.getFundsAvailable() >= perk.cost) "positive"
        else if (mySession.getFundsAvailable() < perk.cost) "negative"
        else "null"
      } getOrElse "null"
    }

    $scope.getPerkNameClass = (perkJs: js.Dynamic) => {
      perkJs.toPerk map { perk =>
        if (perk.selected || mySession.getFundsAvailable() >= perk.cost) "st_bkg_color" else "null"
      } getOrElse "null"
    }

    $scope.getPerkDescClass = (perkJs: js.Dynamic) => {
      perkJs.toPerk map { perk =>
        if (perk.selected || mySession.getFundsAvailable() >= perk.cost) "" else "null"
      } getOrElse "null"
    }

    $scope.loadPerks = () => loadPerks()

    $scope.setupPerks = () => setupPerks()

    ///////////////////////////////////////////////////////////////////////////
    //          Private Functions
    ///////////////////////////////////////////////////////////////////////////

    private def getSelectedPerkCodes = getSelectedPerks map (_.code.as[String])

    private def getSelectedPerks = perks.filter(perk => perk.selected.as[Boolean] && !perk.owned.as[Boolean])

    private def getSelectedPerksCost = getSelectedPerks map (_.cost.as[Double]) sum

    private def loadPerks() {
      // load the player's perks
      dialog.getMyPerks(mySession.getContestID(), mySession.getUserID()) onComplete {
        case Success(response) =>
          g.console.log(s"loadPerks: response = $response")
          myFunds = response.fundsAvailable
          myPerks = response.perkCodes
          setupPerks()
        case Failure(e) =>
          toaster.error("Error loading player perks")
          g.console.error(s"Error loading player perks: ${e.getMessage}")
      }

      // load all contest perks
      dialog.getPerks(mySession.getContestID()) onComplete {
        case Success(loadedPerks) =>
          perks = loadedPerks
          perkMapping = js.Dictionary(perks.map(p => p.code.as[String] -> p): _*)
          setupPerks()
        case Failure(e) =>
          toaster.error("Error loading available perks")
      }
    }

    private def purchasePerks() {
      // the contest must be defined
      if (mySession.contestIsEmpty()) toaster.error("No game selected")
      else {
        // build the list of perks to purchase
        val perkCodes = getSelectedPerkCodes
        val totalCost = getSelectedPerksCost

        // send the purchase order
        dialog.purchasePerks(mySession.getContestID(), mySession.getUserID(), perkCodes) onComplete {
          case Success(contest) => $modalInstance.close(contest)
          case Failure(e) =>
            toaster.error(s"Failed to purchase ${perkCodes.length} Perk(s)")
            g.console.error(s"Error: Purchase Perks - ${e.getMessage}")
        }
      }
    }

    /**
     * Setup the perks state; indicating which perks are owned
     */
    private def setupPerks() {
      // pre-set all perks
      perks.foreach { perk =>
        perk.owned = false
        perk.selected = false
      }

      // select the owned perks
      myPerks.foreach { perkCode =>
        perkMapping.get(perkCode) foreach { perk =>
          perk.owned = true
          perk.selected = true
        }
      }
    }

  }

  /**
   * Perk Extensions
   * @param response the given [[js.Dynamic response]]
   */
  implicit class PerkExtensions(val response: js.Dynamic) extends AnyVal {

    def toPerk: Option[Perk] = {
      //g.console.log(s"toPerks: ${JSON.stringify(response)}")
      for {
        name <- response.name.asOpt[String]
        code <- response.code.asOpt[String]
        description <- response.description.asOpt[String]
        cost <- response.cost.asOpt[Double]
      } yield Perk(name, code, description, cost)
    }

    def toPerksResponse: PerksResponse = {
      //g.console.log(s"toPerksResponse: ${JSON.stringify(response)}")
      val perkCodes = if (isDefined(response.perks)) response.perks.asArray[String] else emptyArray[String]
      val fundsAvailable = if (isDefined(response.fundsAvailable)) response.fundsAvailable.as[Double] else 0.0d
      PerksResponse(perkCodes, fundsAvailable)
    }
  }

  /**
   * Represents a perk
   * @param name the name of the perk
   * @param code the code representing the perk
   * @param description the perk's description
   * @param cost the perk's cost
   */
  case class Perk(name: String, code: String, description: String, cost: Double, var owned: Boolean = false, var selected: Boolean = false) {

    def toJson = JS(name = name, code = code, description = description, cost = cost, owned = owned, selected = selected)
  }

  /**
   * Represents a player's perk and available funds
   * @param perkCodes the player's perk codes
   * @param fundsAvailable the player's available funds
   */
  case class PerksResponse(perkCodes: js.Array[String], fundsAvailable: Double)

}