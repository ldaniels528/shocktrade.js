package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.extensions.{ModalInstance, Toaster}
import com.github.ldaniels528.scalascript.{Controller, Scope, injected, scoped}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.PerksDialogController._
import com.shocktrade.javascript.models.{Contest, Perk}
import org.scalajs.dom.console

import scala.language.postfixOps
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
 * Perks Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
class PerksDialogController($scope: PerksDialogScope, $modalInstance: ModalInstance[PerksDialogResult], toaster: Toaster,
                            @injected("MySession") mySession: MySession,
                            @injected("PerksDialog") perksSvc: PerksDialogService)
  extends Controller {

  private var perks = emptyArray[Perk]
  private var perkMapping = js.Dictionary[Perk]()
  private var myPerks = emptyArray[String]
  private var myFunds = mySession.cashAccount_?.map(_.cashFunds).getOrElse(0.0d)

  ///////////////////////////////////////////////////////////////////////////
  //          Public Functions
  ///////////////////////////////////////////////////////////////////////////

  @scoped def cancel(): Unit = $modalInstance.dismiss("cancel")

  @scoped def countOwnedPerks: Int = perks.count(_.owned)

  @scoped def getPerks: js.Array[Perk] = perks

  @scoped def getTotalCost: Double = getSelectedPerksCost

  @scoped def hasSufficientFunds: Boolean = getSelectedPerksCost <= mySession.getFundsAvailable

  @scoped def perksSelected: Boolean = perks.exists(p => p.selected && !p.owned)

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

  private def initPerks() = {
    val outcome = for {
      playerId <- mySession.userProfile.OID_?
      contestId <- mySession.contest.flatMap(_.OID_?)
    } yield (playerId, contestId)

    // load the player's perks
    outcome match {
      case Some((playerId, contestId)) =>
        perksSvc.getMyPerks(contestId, playerId) onComplete {
          case Success(response) =>
            console.log(s"loadPerks: response = $response")
            myFunds = response.fundsAvailable
            myPerks = response.perkCodes
            setupPerks()
          case Failure(e) =>
            toaster.error("Error loading player perks")
            console.error(s"Error loading player perks: ${e.getMessage}")
        }

        // load all contest perks
        perksSvc.getPerks(contestId) onComplete {
          case Success(loadedPerks) =>
            perks = loadedPerks
            perkMapping = js.Dictionary(perks.map(p => p.code -> p): _*)
            setupPerks()
          case Failure(e) =>
            toaster.error("Error loading available perks")
        }

      case None =>
        toaster.error("User session error")
    }
  }

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

  private def getSelectedPerkCodes = getSelectedPerks map (_.code)

  private def getSelectedPerks = perks.filter(perk => perk.selected && !perk.owned)

  private def getSelectedPerksCost = getSelectedPerks map (_.cost) sum

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