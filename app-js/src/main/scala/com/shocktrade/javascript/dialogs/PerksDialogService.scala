package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.core.Http
import com.github.ldaniels528.scalascript.extensions.{Modal, ModalOptions}
import com.github.ldaniels528.scalascript.{Service, injected}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.PerksDialogController._
import com.shocktrade.javascript.models.Contest

import scala.concurrent.{Future, ExecutionContext}
import scala.language.postfixOps
import scala.scalajs.js

/**
 * Perks Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class PerksDialogService($http: Http, $modal: Modal, @injected("MySession") mySession: MySession)
  extends Service {

  /**
   * Perks Modal Dialog
   */
  def popup()(implicit ec: ExecutionContext): Future[PerksDialogResult] = {
    // create an instance of the dialog
    val $modalInstance = $modal.open[PerksDialogResult](ModalOptions(
      templateUrl = "perks_dialog.htm",
      controller = classOf[PerksDialogController].getSimpleName
    ))
    $modalInstance.result
  }

  /**
   * Retrieves the promise of a sequence of perks
   * @return the promise of a sequence of [[Perk perks]]
   */
  def getPerks(contestId: String) = {
    required("contestId", contestId)

    $http.get[js.Array[js.Dynamic]](s"/api/contest/$contestId/perks")
  }

  /**
   * Retrieves the promise of an option of a perks response
   * @return the promise of an option of a [[PerksResponse perks response]]
   */
  def getMyPerks(contestId: String, playerId: String)(implicit ec: ExecutionContext) = {
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
  def purchasePerks(contestId: String, playerId: String, perkCodes: js.Array[String]) = {
    required("contestId", contestId)
    required("playerId", playerId)
    required("perkCodes", perkCodes)

    $http.put[Contest](s"/api/contest/$contestId/perks/$playerId", perkCodes)
  }

}