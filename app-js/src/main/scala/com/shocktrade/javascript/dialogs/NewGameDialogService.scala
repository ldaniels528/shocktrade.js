package com.shocktrade.javascript.dialogs

import biz.enef.angulate.core.{HttpPromise, HttpService}
import biz.enef.angulate.{Service, named}
import com.greencatsoft.angularjs.core.Promise
import com.greencatsoft.angularjs.extensions.{ModalOptions, ModalService}
import com.shocktrade.javascript.dashboard.ContestService

import scala.scalajs.js

/**
 * New Game Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class NewGameDialogService($http: HttpService, $modal: ModalService,
                           @named("ContestService") contestService: ContestService)
  extends Service {

  /**
   * Sign-up Modal Dialog
   */
  def popup: js.Function0[Promise] = () => {
    // create an instance of the dialog
    val options = ModalOptions()
    options.templateUrl = "new_game_dialog.htm"
    options.controller = classOf[NewGameDialogController].getSimpleName

    val $modalInstance = $modal.open(options)
    $modalInstance.result
  }

  /**
   * Creates a new game
   * @return the promise of the result of creating a new game
   */
  def createNewGame: js.Function1[js.Dynamic, HttpPromise[js.Dynamic]] = (form: js.Dynamic) => {
    contestService.createContest(form)
  }

}
