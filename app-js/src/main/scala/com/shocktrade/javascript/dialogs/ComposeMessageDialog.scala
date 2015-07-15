package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.core.{Http, Q}
import com.github.ldaniels528.scalascript.extensions.{Modal, ModalInstance, ModalOptions}
import com.github.ldaniels528.scalascript.{Controller, Scope, Service, injected, scoped}
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.ComposeMessageDialogController.ComposeMessageResult

import scala.concurrent.Future
import scala.scalajs.js

/**
 * Compose Message Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class ComposeMessageDialog($http: Http, $modal: Modal, $q: Q) extends Service {

  def popup(): Future[ComposeMessageResult] = {
    // create an instance of the dialog
    val $modalInstance = $modal.open[ComposeMessageResult](ModalOptions(
      templateUrl = "compose_message.htm",
      controllerClass = classOf[ComposeMessageDialogController]
    ))
    $modalInstance.result
  }
}

/**
 * Compose Message Dialog Controller
 * @author lawrence.daniels@gmail.com
 */
class ComposeMessageDialogController($scope: ComposeMessageScope, $modalInstance: ModalInstance[ComposeMessageResult],
                                     @injected("ComposeMessageDialog") composeMessageDialog: ComposeMessageDialog)
  extends Controller {

  $scope.form = ComposeMessageForm()

  @scoped def ok(form: ComposeMessageForm) = $modalInstance.close(form)

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