package com.shocktrade.javascript.dialogs

import com.ldaniels528.scalascript.core.Http
import com.ldaniels528.scalascript.extensions.{Modal, ModalOptions}
import com.ldaniels528.scalascript.{Service, injected}
import com.shocktrade.javascript.MySession

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
 * New Order Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class NewOrderDialogService($http: Http, $modal: Modal, @injected("MySession") mySession: MySession) extends Service {

  /**
   * Opens a new Order Entry Pop-up Dialog
   */
  def popup(params: js.Dynamic)(implicit ec: ExecutionContext) = {
    // create an instance of the dialog
    val $modalInstance = $modal.open(ModalOptions(
      templateUrl = "new_order_dialog.htm",
      controller = classOf[NewOrderDialogController].getSimpleName,
      resolve = js.Dictionary("params" -> (() => params))
    ))
    $modalInstance.result
  }

  def lookupQuote(symbol: String) = $http.get[js.Dynamic](s"/api/quotes/cached/$symbol")

}
