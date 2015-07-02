package com.shocktrade.javascript.dialogs

import com.ldaniels528.scalascript.Controller
import com.ldaniels528.scalascript.extensions.ModalInstance

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => JS}

/**
 * Compose Message Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
class ComposeMessageDialogController($scope: js.Dynamic, $modalInstance: ModalInstance[js.Dynamic]) extends Controller {

  $scope.form = JS()

  $scope.ok = (form: js.Dynamic) => $modalInstance.close(form)

  $scope.cancel = () => $modalInstance.dismiss("cancel")

}
