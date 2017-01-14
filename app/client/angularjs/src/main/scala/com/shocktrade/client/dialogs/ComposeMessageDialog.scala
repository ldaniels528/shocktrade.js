package com.shocktrade.client.dialogs

import io.scalajs.npm.angularjs.http.Http
import io.scalajs.npm.angularjs.uibootstrap._
import io.scalajs.util.ScalaJsHelper._
import io.scalajs.npm.angularjs.{Controller, Scope, Service, injected}
import com.shocktrade.client.dialogs.ComposeMessageDialogController.ComposeMessageResult

import scala.concurrent.Future
import scala.scalajs.js

/**
  * Compose Message Dialog Service
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
class ComposeMessageDialog($http: Http, $modal: Modal) extends Service {

  def popup(): Future[ComposeMessageResult] = {
    val $modalInstance = $modal.open[ComposeMessageResult](new ModalOptions(
      templateUrl = "compose_message.html",
      controller = classOf[ComposeMessageDialogController].getSimpleName
    ))
    $modalInstance.result
  }
}

/**
  * Compose Message Dialog Controller
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
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
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ComposeMessageDialogController {

  type ComposeMessageResult = ComposeMessageForm
}

/**
  * Compose Message Scope
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
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
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
@js.native
trait ComposeMessageForm extends js.Object

/**
  * Compose Message Form Singleton
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object ComposeMessageForm {

  def apply() = New[ComposeMessageForm]

}