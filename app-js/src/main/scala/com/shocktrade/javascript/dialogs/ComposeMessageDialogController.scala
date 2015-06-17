package com.shocktrade.javascript.dialogs

import biz.enef.angulate.ScopeController
import com.greencatsoft.angularjs.extensions.ModalInstance

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => JS}

/**
 * Compose Message Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
class ComposeMessageDialogController($scope: js.Dynamic, $modalInstance: ModalInstance) extends ScopeController {

  $scope.form = JS()

  $scope.ok = (form: js.Dynamic) => $modalInstance.close(form)

  $scope.cancel = () => $modalInstance.dismiss("cancel")

}
