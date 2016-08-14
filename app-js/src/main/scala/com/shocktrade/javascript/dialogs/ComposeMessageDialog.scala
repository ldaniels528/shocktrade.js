package com.shocktrade.javascript.dialogs

import org.scalajs.angularjs.http.Http
import org.scalajs.angularjs.uibootstrap._
import org.scalajs.nodejs.util.ScalaJsHelper._
import org.scalajs.angularjs.{Controller, Scope, Service, injected}
import com.shocktrade.javascript.dialogs.ComposeMessageDialogController.ComposeMessageResult

import scala.concurrent.Future
import scala.scalajs.js

/**
  * Compose Message Dialog Service
  * @author lawrence.daniels@gmail.com
  */
class ComposeMessageDialog($http: Http, $modal: Modal) extends Service {

  def popup(): Future[ComposeMessageResult] = {
    val $modalInstance = $modal.open[ComposeMessageResult](new ModalOptions(
      templateUrl = "compose_message.htm",
      controller = classOf[ComposeMessageDialogController].getSimpleName
    ))
    $modalInstance.result
  }
}

/**
  * Compose Message Dialog Controller
  * @author lawrence.daniels@gmail.com
  */
class ComposeMessageDialogController($scope: ComposeMessageScope,
                                     $modalInstance: ModalInstance[ComposeMessageResult],
                                     @injected("ComposeMessageDialog") composeMessageDialog: ComposeMessageDialog)
  extends Controller {

  $scope.form = ComposeMessageForm()

  $scope.ok = (aForm: js.UndefOr[ComposeMessageForm]) => aForm.foreach($modalInstance.close)

  $scope.cancel = () => $modalInstance.dismiss("cancel")

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
@js.native
trait ComposeMessageScope extends Scope {
  // variables
  var form: js.UndefOr[ComposeMessageForm]

  // functions
  var cancel: js.Function0[Unit]
  var ok: js.Function1[js.UndefOr[ComposeMessageForm], Unit]

}

/**
  * Compose Message Form
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait ComposeMessageForm extends js.Object

/**
  * Compose Message Form Singleton
  * @author lawrence.daniels@gmail.com
  */
object ComposeMessageForm {

  def apply() = New[ComposeMessageForm]

}