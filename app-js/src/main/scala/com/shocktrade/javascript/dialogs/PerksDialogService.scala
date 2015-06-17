package com.shocktrade.javascript.dialogs

import biz.enef.angulate.core.{HttpPromise, HttpService}
import biz.enef.angulate.{Service, named}
import com.greencatsoft.angularjs.core.Promise
import com.greencatsoft.angularjs.extensions.{ModalOptions, ModalService}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.PerksDialogController._

import scala.language.postfixOps
import scala.scalajs.js

/**
 * Perks Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class PerksDialogService($http: HttpService, $modal: ModalService, @named("MySession") mySession: MySession)
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
  def getMyPerks: js.Function2[String, String, HttpPromise[PerksResponse]] = (contestId: String, playerId: String) => {
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