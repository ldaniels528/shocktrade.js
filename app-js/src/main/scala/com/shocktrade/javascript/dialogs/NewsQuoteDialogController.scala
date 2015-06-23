package com.shocktrade.javascript.dialogs

import biz.enef.angulate.{ScopeController, named}
import com.greencatsoft.angularjs.extensions.ModalInstance
import com.shocktrade.javascript.dashboard.ContestService

import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.Dynamic.{global => g, literal => JS}
import scala.util.{Failure, Success}

/**
 * News Quote Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
class NewsQuoteDialogController($scope: js.Dynamic, $modalInstance: ModalInstance,
                                @named("ContestService") contestService: ContestService,
                                @named("symbol") symbol: String)
  extends ScopeController {

  $scope.quote = JS(symbol = symbol)

  $scope.init = (symbol: String) => {
    contestService.orderQuote(symbol) onComplete {
      case Success(data) => $scope.quote = data
      case Failure(e) =>
        g.console.error(s"Error: ${e.getMessage}")
    }
  }

  $scope.cancel = () => $modalInstance.dismiss("cancel")

  $scope.ok = (form: js.Dynamic) => $modalInstance.close(form)

}
