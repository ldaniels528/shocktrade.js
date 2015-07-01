package com.shocktrade.javascript.dialogs

import biz.enef.angulate.named
import com.ldaniels528.javascript.angularjs.{ScalaJsHelper, Service}
import com.ldaniels528.javascript.angularjs.core.{Http, Modal, ModalOptions}
import com.shocktrade.javascript.MySession
import ScalaJsHelper._
import com.shocktrade.javascript.dialogs.PerksDialogController._

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.scalajs.js

/**
 * Perks Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class PerksDialogService($http: Http, $modal: Modal, @named("MySession") mySession: MySession)
  extends Service {

  /**
   * Perks Modal Dialog
   */
  def popup()(implicit ec: ExecutionContext) = {
    // create an instance of the dialog
    val $modalInstance = $modal.open[js.Dynamic](ModalOptions(
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

    $http.put[js.Dynamic](s"/api/contest/$contestId/perks/$playerId", perkCodes)
  }

}