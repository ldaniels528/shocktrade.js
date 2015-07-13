package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.extensions.ModalInstance
import com.github.ldaniels528.scalascript.{Controller, Scope, injected, scoped}
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.ComposeMessageDialogController.ComposeMessageResult

import scala.scalajs.js

/**
 * Compose Message Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
class ComposeMessageDialogController($scope: ComposeMessageScope, $modalInstance: ModalInstance[ComposeMessageResult],
                                     @injected("ComposeMessageDialog") composeMessageDialog: ComposeMessageDialogService)
  extends Controller {

  $scope.form = ComposeMessageForm()

  @scoped def ok(form: ComposeMessageForm) = {
    $modalInstance.close(form)
  }

  @scoped def cancel() = $modalInstance.dismiss("cancel")

}

/**
 * Compose Message Dialog Controller Singleton
 * @author lawrence.daniels@gmail.com
 */
object ComposeMessageDialogController {

  type ComposeMessageResult = ComposeMessageForm

}

/**
 * Compose Message Scope
 * @author lawrence.daniels@gmail.com
 */
trait ComposeMessageScope extends Scope {
  var form: ComposeMessageForm = js.native
}

/**
 * Compose Message Form
 * @author lawrence.daniels@gmail.com
 */
trait ComposeMessageForm extends js.Object {

}

/**
 * Compose Message Form Singleton
 * @author lawrence.daniels@gmail.com
 */
object ComposeMessageForm {

  def apply() = makeNew[ComposeMessageForm]
}