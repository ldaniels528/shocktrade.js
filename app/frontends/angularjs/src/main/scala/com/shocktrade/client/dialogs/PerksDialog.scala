package com.shocktrade.client.dialogs

import com.shocktrade.client.contest.PortfolioService
import com.shocktrade.client.dialogs.PerksDialogController._
import com.shocktrade.client.models.contest.{Perk, Portfolio}
import com.shocktrade.client.users.UserService
import com.shocktrade.common.forms.PerksResponse
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs._
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.uibootstrap.{Modal, ModalInstance, ModalOptions}
import io.scalajs.util.JsUnderOrHelper._
import io.scalajs.util.PromiseHelper.Implicits._
import io.scalajs.util.ScalaJsHelper._


import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Perks Dialog Service
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PerksDialog($http: Http, $uibModal: Modal) extends Service {

  /**
   * Perks Modal Dialog
   */
  def popup(contestID: String, userID: String): js.Promise[PerksDialogResult] = {
    val $uibModalInstance = $uibModal.open[PerksDialogResult](new ModalOptions(
      templateUrl = "perks_dialog.html",
      controller = classOf[PerksDialogController].getSimpleName,
      resolve = js.Dictionary("contestID" -> (() => contestID), "userID" -> (() => userID))
    ))
    $uibModalInstance.result
  }

  /**
   * Retrieves the promise of a sequence of available perks
   * @return the promise of a sequence of available [[Perk perk]]s
   */
  def getAvailablePerks: js.Promise[HttpResponse[js.Array[Perk]]] = {
    $http.get[js.Array[Perk]]("/api/contests/perks")
  }

  /**
   * Retrieves the promise of a sequence of perks
   * @param portfolioID the given portfolio ID
   * @return the promise of a sequence of [[Perk perks]]
   */
  def getPerks(portfolioID: String): js.Promise[HttpResponse[js.Array[Perk]]] = {
    $http.get[js.Array[Perk]]("/api/contests/perks")
  }

  /**
   * Retrieves the promise of an option of a perks response
   * @param portfolioID the given portfolio ID
   * @return the promise of an option of a [[PerksResponse perks response]]
   */
  def getMyPerkCodes(portfolioID: String): js.Promise[HttpResponse[PerksResponse]] = {
    $http.get[PerksResponse](s"/api/portfolio/$portfolioID/perks")
  }

  /**
   * Attempts to purchase the given perk codes
   * @param portfolioID the given portfolio ID
   * @param perkCodes   the given perk codes to purchase
   * @return the promise of a [[PerksDialogResult contest]]
   */
  def purchasePerks(portfolioID: String, perkCodes: js.Array[String]): js.Promise[HttpResponse[PerksDialogResult]] = {
    $http.post[Portfolio](s"/api/portfolio/$portfolioID/perks", perkCodes)
  }

}

/**
 * Perks Dialog Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
class PerksDialogController($scope: PerksDialogScope, $uibModalInstance: ModalInstance[PerksDialogResult], toaster: Toaster,
                            @injected("PerksDialog") perksDialog: PerksDialog,
                            @injected("PortfolioService") portfolioService: PortfolioService,
                            @injected("UserService") userService: UserService,
                            @injected("contestID") contestID: () => String,
                            @injected("userID") userID: () => String)
  extends Controller {

  private var myPerkCodes = emptyArray[String]
  private var perkMapping = js.Dictionary[Perk]()

  $scope.availablePerks = emptyArray[Perk]
  $scope.errors = emptyArray[String]
  $scope.fundsAvailable = js.undefined

  ///////////////////////////////////////////////////////////////////////////
  //          Initialization Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.init = () => {
    console.info(s"Loading portfolio for contest ${contestID()} user ${userID()}...")
    val outcome = for {
      portfolio <- portfolioService.findPortfolio(contestID(), userID())
      perks <- perksDialog.getPerks(portfolio.data.portfolioID.orNull)
    } yield (perks, portfolio)

    outcome onComplete {
      case Success((perks, portfolio)) =>
        $scope.$apply { () =>
          $scope.availablePerks = perks.data
          $scope.portfolio = portfolio.data
          $scope.fundsAvailable = portfolio.data.funds
        }
      case Failure(e) =>
        toaster.error(e.displayMessage)
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.cancel = () => $uibModalInstance.dismiss("cancel")

  $scope.countOwnedPerks = () => $scope.availablePerks.count(_.owned.isTrue)

  $scope.getTotalCost = () => getSelectedPerks map (_.cost) sum

  $scope.hasSufficientFunds = () => $scope.fundsAvailable.exists($scope.getTotalCost() <= _)

  $scope.isPerksSelected = () => $scope.availablePerks.exists(p => p.selected.isTrue && !p.owned.isTrue)

  $scope.getPerkCostClass = (aPerk: js.UndefOr[Perk]) => aPerk map { perk =>
    if (perk.selected.isTrue || $scope.fundsAvailable.exists(_ >= perk.cost)) "positive"
    else if ($scope.fundsAvailable.exists(_ < perk.cost)) "negative"
    else "null"
  }

  $scope.getPerkNameClass = (aPerk: js.UndefOr[Perk]) => aPerk map { perk =>
    if (perk.selected.isTrue || $scope.fundsAvailable.exists(_ >= perk.cost)) "st_bkg_color" else "null"
  }

  $scope.getPerkDescClass = (aPerk: js.UndefOr[Perk]) => aPerk map { perk =>
    if (perk.selected.isTrue || $scope.fundsAvailable.exists(_ >= perk.cost)) "" else "null"
  }

  $scope.loadPerks = () => {
    // load the player's perks
    $scope.portfolio.flatMap(_.portfolioID).toOption match {
      case Some(portfolioId) =>
        val outcome = for {
          thePerks <- perksDialog.getPerks(portfolioId).map(_.data)
          perksResponse <- perksDialog.getMyPerkCodes(portfolioId).map(_.data)
        } yield (thePerks, perksResponse)

        outcome onComplete {
          case Success((thePerks, perksResponse)) =>
            // create a mapping from the available perks
            $scope.availablePerks = thePerks
            this.perkMapping = js.Dictionary(thePerks.map(p => p.code -> p): _*)

            // capture the owned perk codes
            //$scope.fundsAvailable = perksResponse.fundsAvailable
            this.myPerkCodes = perksResponse.perkCodes

            $scope.$apply(() => setupPerks())
          case Failure(e) =>
            toaster.error("Error loading perks from the portfolio")
            console.error(s"Error loading player perks: ${e.displayMessage}")
        }

      case None =>
        toaster.error("Portfolio is not loaded")
    }
  }

  $scope.purchasePerks = () => {
    $scope.errors.removeAll()
    $scope.portfolio.flatMap(_.portfolioID).toOption match {
      case Some(portfolioId) =>
        val perkCodes = getSelectedPerks map (_.code)

        // send the purchase order
        perksDialog.purchasePerks(portfolioId, perkCodes) onComplete {
          case Success(response) => $uibModalInstance.close(response.data)
          case Failure(e) =>
            $scope.$apply(() => $scope.errors.push(s"Failed to purchase ${perkCodes.length} Perk(s)"))
            console.error(s"Error: Purchase Perks - ${e.getMessage}")
        }
      case None =>
        toaster.error("No contest selected")
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  private def getSelectedPerks = $scope.availablePerks.filter(perk => perk.selected.isTrue && !perk.owned.isTrue)

  /**
   * Setup the perks state; indicating which perks are owned
   */
  private def setupPerks() {
    $scope.availablePerks.foreach { perk =>
      perk.owned = myPerkCodes.contains(perk.code)
      perk.selected = perk.owned
    }
  }

}

/**
 * Perks Dialog Controller
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
object PerksDialogController {

  type PerksDialogResult = Portfolio

  /**
   * Perks Dialog Scope
   * @author Lawrence Daniels <lawrence.daniels@gmail.com>
   */
  @js.native
  trait PerksDialogScope extends Scope {
    // variables
    var availablePerks: js.Array[Perk] = js.native
    var errors: js.Array[String] = js.native
    var fundsAvailable: js.UndefOr[Double] = js.native
    var portfolio: js.UndefOr[Portfolio] = js.native

    // functions
    var cancel: js.Function0[Unit] = js.native
    var getTotalCost: js.Function0[Double] = js.native
    var hasSufficientFunds: js.Function0[Boolean] = js.native

    var init: js.Function0[Unit] = js.native
    var countOwnedPerks: js.Function0[Int] = js.native
    var isPerksSelected: js.Function0[Boolean] = js.native
    var getPerkCostClass: js.Function1[js.UndefOr[Perk], js.UndefOr[String]] = js.native
    var getPerkNameClass: js.Function1[js.UndefOr[Perk], js.UndefOr[String]] = js.native
    var getPerkDescClass: js.Function1[js.UndefOr[Perk], js.UndefOr[String]] = js.native
    var loadPerks: js.Function0[Unit] = js.native
    var purchasePerks: js.Function0[Unit] = js.native

  }

}

