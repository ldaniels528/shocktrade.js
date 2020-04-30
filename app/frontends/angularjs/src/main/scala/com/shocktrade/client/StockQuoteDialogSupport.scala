package com.shocktrade.client

import com.shocktrade.client.dialogs.StockQuoteDialog
import com.shocktrade.client.dialogs.StockQuoteDialogController.StockQuoteDialogResult
import io.scalajs.npm.angularjs.{Controller, Scope}

import scala.scalajs.js

/**
 * Stock Quote Dialog Support
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait StockQuoteDialogSupport {
  ref: Controller =>

  def $scope: StockQuoteDialogSupportScope

  def stockQuoteDialog: StockQuoteDialog

  $scope.stockQuotePopup = (aSymbol: js.UndefOr[String]) => aSymbol map stockQuoteDialog.lookupSymbol

}

/**
 * Stock Quote Dialog Support Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait StockQuoteDialogSupportScope extends Scope {
  // functions
  var stockQuotePopup: js.Function1[js.UndefOr[String], js.UndefOr[js.Promise[StockQuoteDialogResult]]] = js.native

}