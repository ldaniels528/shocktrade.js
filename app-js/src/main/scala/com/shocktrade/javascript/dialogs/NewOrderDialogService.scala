package com.shocktrade.javascript.dialogs

import com.github.ldaniels528.scalascript.core.Http
import com.github.ldaniels528.scalascript.extensions.{Modal, ModalOptions}
import com.github.ldaniels528.scalascript.{Service, injected}
import com.shocktrade.javascript.MySession
import com.shocktrade.javascript.ScalaJsHelper._
import com.shocktrade.javascript.dialogs.NewOrderDialogController.NewOrderDialogResult
import com.shocktrade.javascript.models.{Contest, OrderQuote}

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
 * New Order Dialog Service
 * @author lawrence.daniels@gmail.com
 */
class NewOrderDialogService($http: Http, $modal: Modal, @injected("MySession") mySession: MySession)
  extends Service {

  /**
   * Opens a new Order Entry Pop-up Dialog
   */
  def popup(params: NewOrderParams)(implicit ec: ExecutionContext): Future[NewOrderDialogResult] = {
    // create an instance of the dialog
    val $modalInstance = $modal.open[NewOrderDialogResult](ModalOptions(
      templateUrl = "new_order_dialog.htm",
      controller = classOf[NewOrderDialogController].getSimpleName,
      resolve = js.Dictionary("params" -> (() => params))
    ))
    $modalInstance.result
  }

  def createOrder(contestId: String, playerId: String, order: NewOrderForm) = {
    required("contestId", contestId)
    required("playerId", playerId)
    required("order", order)
    $http.put[Contest](s"/api/order/$contestId/$playerId", order)
  }

  def lookupQuote(symbol: String) = $http.get[OrderQuote](s"/api/quotes/cached/$symbol")

}
