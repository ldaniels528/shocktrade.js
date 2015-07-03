package com.shocktrade.javascript.dialogs

import com.ldaniels528.scalascript.core.Http
import com.ldaniels528.scalascript.extensions.{Modal, ModalOptions}
import com.ldaniels528.scalascript.{Service, injected}
import com.shocktrade.javascript.dashboard.ContestService

import scala.scalajs.js

/**
 * New Game Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class NewGameDialogService($http: Http, $modal: Modal, @injected("ContestService") contestService: ContestService)
  extends Service {

  /**
   * Sign-up Modal Dialog
   */
  def popup() = {
    // create an instance of the dialog
    val $modalInstance = $modal.open[js.Dynamic](ModalOptions(
      templateUrl = "new_game_dialog.htm",
      controller = classOf[NewGameDialogController].getSimpleName
    ))
    $modalInstance.result
  }

  /**
   * Creates a new game
   * @return the promise of the result of creating a new game
   */
  def createNewGame(form: js.Dynamic) = contestService.createContest(form)

}
