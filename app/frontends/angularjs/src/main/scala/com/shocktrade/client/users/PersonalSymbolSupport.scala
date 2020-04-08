package com.shocktrade.client.users

import com.shocktrade.client.contest.ContestService
import com.shocktrade.common.models.OperationResult
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.AngularJsHelper._
import io.scalajs.npm.angularjs.http.HttpResponse
import io.scalajs.npm.angularjs.toaster.Toaster
import io.scalajs.npm.angularjs.{Controller, Q, Scope}
import io.scalajs.util.PromiseHelper.Implicits._

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
 * Personal Symbol Support
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
trait PersonalSymbolSupport {
  ref: Controller =>

  $scope.favoriteSymbols = js.Array()
  $scope.heldSymbols = js.Array()
  $scope.recentSymbols = js.Array()

  ///////////////////////////////////////////////////////////////////////////
  //          Injected Variables
  ///////////////////////////////////////////////////////////////////////////

  def $q: Q

  def $scope: PersonalSymbolSupportScope

  def contestService: ContestService

  def toaster: Toaster

  def userService: UserService

  ///////////////////////////////////////////////////////////////////////////
  //          Ticker CRUD Functions
  ///////////////////////////////////////////////////////////////////////////

  $scope.addFavoriteSymbol = (aUserID: js.UndefOr[String], aSymbol: js.UndefOr[String]) =>
    tickerUpdate(aUserID, aSymbol)(userService.addFavoriteSymbol)

  $scope.isFavorite = (aUserID: js.UndefOr[String], aSymbol: js.UndefOr[String]) => aSymbol.exists($scope.favoriteSymbols.contains)

  $scope.isHeldSymbol = (aUserID: js.UndefOr[String], aSymbol: js.UndefOr[String]) => aSymbol.exists($scope.favoriteSymbols.contains)

  $scope.removeFavoriteSymbol = (aUserID: js.UndefOr[String], aSymbol: js.UndefOr[String]) =>
    tickerUpdate(aUserID, aSymbol)(userService.removeFavoriteSymbol)

  $scope.addRecentSymbol = (aUserID: js.UndefOr[String], aSymbol: js.UndefOr[String]) =>
    tickerUpdate(aUserID, aSymbol)(userService.addRecentSymbol)

  $scope.isRecentSymbol = (aUserID: js.UndefOr[String], aSymbol: js.UndefOr[String]) => aSymbol.exists($scope.recentSymbols.contains)

  $scope.removeRecentSymbol = (aUserID: js.UndefOr[String], aSymbol: js.UndefOr[String]) =>
    tickerUpdate(aUserID, aSymbol)(userService.removeRecentSymbol)

  ///////////////////////////////////////////////////////////////////////////
  //          Private Functions
  ///////////////////////////////////////////////////////////////////////////

  def refreshMySymbols(userID: String): Unit = {
    // refresh the favorite symbols
    userService.findFavoriteSymbols(userID) onComplete {
      case Success(tickers) =>
        console.info(s"favoriteSymbols = ${JSON.stringify($scope.favoriteSymbols)}")
        $scope.favoriteSymbols = tickers.data.flatMap(_.symbol.toOption)
      case Failure(e) => toaster.error("Error", e.displayMessage)
    }

    // refresh the held symbols
    contestService.findHeldSecurities(userID) onComplete {
      case Success(tickers) =>
        console.info(s"heldSymbols = ${JSON.stringify($scope.heldSymbols)}")
        $scope.heldSymbols = tickers.data.flatMap(_.symbol.toOption)
      case Failure(e) => toaster.error("Error", e.displayMessage)
    }

    // refresh the recent symbols
    userService.findRecentSymbols(userID) onComplete {
      case Success(tickers) =>
        console.info(s"recentSymbols = ${JSON.stringify($scope.recentSymbols)}")
        $scope.recentSymbols = tickers.data.flatMap(_.symbol.toOption)
      case Failure(e) => toaster.error("Error", e.displayMessage)
    }
  }

  def tickerUpdate(aUserID: js.UndefOr[String], aSymbol: js.UndefOr[String])(update: (String, String) => js.Promise[HttpResponse[OperationResult]]): js.Promise[OperationResult] = {
    val params = (for {userID <- aUserID; symbol <- aSymbol} yield (symbol, userID)).toOption
    val outcome = $q.defer[OperationResult]()
    params match {
      case Some((symbol, userId)) =>
        update(userId, symbol) onComplete {
          case Success(result) => outcome.resolve(result.data)
          case Failure(e) => outcome.reject(e.getMessage)
        }
      case None =>
        toaster.error("Error", "Invalid parameters")
        outcome.reject("Invalid parameters")
    }
    outcome.promise
  }

}

/**
 * Personal Symbol Support Scope
 * @author Lawrence Daniels <lawrence.daniels@gmail.com>
 */
@js.native
trait PersonalSymbolSupportScope extends Scope {

  // variables
  var favoriteSymbols: js.Array[String] = js.native
  var heldSymbols: js.Array[String] = js.native
  var recentSymbols: js.Array[String] = js.native

  // favorite symbols functions
  var addFavoriteSymbol: js.Function2[js.UndefOr[String], js.UndefOr[String], js.UndefOr[js.Promise[OperationResult]]] = js.native
  var isFavorite: js.Function2[js.UndefOr[String], js.UndefOr[String], Boolean] = js.native
  var removeFavoriteSymbol: js.Function2[js.UndefOr[String], js.UndefOr[String], js.UndefOr[js.Promise[OperationResult]]] = js.native

  // held symbols functions
  var isHeldSymbol: js.Function2[js.UndefOr[String], js.UndefOr[String], Boolean] = js.native

  // recently-viewed symbols functions
  var addRecentSymbol: js.Function2[js.UndefOr[String], js.UndefOr[String], js.UndefOr[js.Promise[OperationResult]]] = js.native
  var isRecentSymbol: js.Function2[js.UndefOr[String], js.UndefOr[String], Boolean] = js.native
  var removeRecentSymbol: js.Function2[js.UndefOr[String], js.UndefOr[String], js.UndefOr[js.Promise[OperationResult]]] = js.native

}