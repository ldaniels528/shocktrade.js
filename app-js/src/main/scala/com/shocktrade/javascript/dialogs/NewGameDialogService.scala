package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.Service
import com.github.ldaniels528.scalascript.core.Http
import com.github.ldaniels528.scalascript.extensions.{Modal, ModalOptions}
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.NewGameDialogController.NewGameDialogResult
import com.shocktrade.javascript.models.Contest

import scala.concurrent.Future

/**
 * New Game Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class NewGameDialogService($http: Http, $modal: Modal) extends Service {

  /**
   * Sign-up Modal Dialog
   */
  def popup(): Future[NewGameDialogResult] = {
    // create an instance of the dialog
    val $modalInstance = $modal.open[NewGameDialogResult](ModalOptions(
      templateUrl = "new_game_dialog.htm",
      controller = classOf[NewGameDialogController].getSimpleName
    ))
    $modalInstance.result
  }

  /**
   * Creates a new game
   * @return the promise of the result of creating a new game
   */
  def createNewGame(form: NewGameForm): Future[Contest] = {
    required("form", form)
    $http.put[Contest]("/api/contest", form)
  }

}
