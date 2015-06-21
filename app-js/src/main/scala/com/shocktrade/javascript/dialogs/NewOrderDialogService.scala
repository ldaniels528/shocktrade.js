package com.shocktrade.javascript.dialogs

import com.shocktrade.javascript.ScalaJsHelper._
import biz.enef.angulate.core.{HttpPromise, HttpService}
import biz.enef.angulate.{Service, named}
import com.greencatsoft.angularjs.extensions.{ModalOptions, ModalService}
import com.shocktrade.javascript.MySession

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{literal => JS, global => g}

/**
 * New Order Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class NewOrderDialogService($http: HttpService, $modal: ModalService, @named("MySession") mySession: MySession) extends Service {

  /**
   * Opens a new Order Entry Pop-up Dialog
   */
  def popup(params: js.Dynamic) = {
    val options = ModalOptions()
    options.templateUrl = "new_order_dialog.htm"
    options.controller = classOf[NewOrderDialogController].getSimpleName

    // params: the given input parameters (e.g. { symbol: *, quantity: * })
    options.resolve = js.Dictionary("params" -> (() => params))

    // create an instance of the dialog
    val $modalInstance = $modal.open(options)
    $modalInstance.result
  }

  def lookupQuote: js.Function1[String, HttpPromise[js.Dynamic]] = (symbol: String) => {
    $http.get[js.Dynamic]("/api/quotes/cached/" + symbol)
  }

}
